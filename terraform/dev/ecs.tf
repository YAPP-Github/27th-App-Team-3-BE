# EC2 IAM Role
resource "aws_iam_role" "ec2_role" {
  name = "${var.project_name}-${var.environment}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-ec2-role"
  }
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_cloudwatch" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy" "ec2_policy" {
  name = "${var.project_name}-${var.environment}-ec2-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchCheckLayerAvailability"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = [
          aws_secretsmanager_secret.rds_credentials.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.static.arn,
          "${aws_s3_bucket.static.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams"
        ]
        Resource = "*"
      },
      {
        # CodeDeploy 배포 번들 다운로드용
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::${var.s3_bucket_name}",
          "arn:aws:s3:::${var.s3_bucket_name}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.project_name}-${var.environment}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# Amazon Linux 2023 최신 AMI
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Instance
resource "aws_instance" "main" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = var.ec2_instance_type
  subnet_id              = aws_subnet.public[1].id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  user_data = base64encode(<<-USERDATA
    #!/bin/bash
    set -e

    # 시스템 업데이트 및 패키지 설치
    dnf update -y
    dnf install -y docker ruby wget jq
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user

    # CodeDeploy 에이전트 설치
    cd /tmp
    wget https://aws-codedeploy-${var.aws_region}.s3.${var.aws_region}.amazonaws.com/latest/install
    chmod +x ./install
    ./install auto
    systemctl enable codedeploy-agent
    systemctl start codedeploy-agent

    # 앱 환경 설정 파일 생성 (시크릿 제외)
    mkdir -p /etc/app
    printf "AWS_REGION=${var.aws_region}\n" > /etc/app/config.env
    printf "ECR_REGISTRY=${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com\n" >> /etc/app/config.env
    printf "ECR_REPOSITORY=${var.project_name}-${var.environment}\n" >> /etc/app/config.env
    printf "CONTAINER_NAME=${var.project_name}-${var.environment}-app\n" >> /etc/app/config.env
    printf "CONTAINER_PORT=${var.ecs_container_port}\n" >> /etc/app/config.env
    printf "SECRET_NAME=${var.project_name}-${var.environment}-rds-credentials\n" >> /etc/app/config.env
    printf "DB_HOST=${var.supabase_db_host}\n" >> /etc/app/config.env
    printf "DB_PORT=${var.supabase_db_port}\n" >> /etc/app/config.env
    printf "DB_NAME=${var.supabase_db_name}\n" >> /etc/app/config.env
    printf "REDIS_HOST=${aws_elasticache_cluster.redis.cache_nodes[0].address}\n" >> /etc/app/config.env
    printf "REDIS_PORT=6379\n" >> /etc/app/config.env
  USERDATA
  )

  tags = {
    Name        = "${var.project_name}-${var.environment}"
    Environment = var.environment
  }
}

# EC2 인스턴스를 ALB 타겟 그룹에 등록
resource "aws_lb_target_group_attachment" "main" {
  target_group_arn = aws_lb_target_group.blue.arn
  target_id        = aws_instance.main.id
  port             = var.ecs_container_port
}

# Secrets Manager for DB Credentials
resource "aws_secretsmanager_secret" "rds_credentials" {
  name                    = "${var.project_name}-${var.environment}-rds-credentials"
  description             = "Supabase PostgreSQL credentials"
  recovery_window_in_days = 0

  tags = {
    Name = "${var.project_name}-${var.environment}-rds-credentials"
  }
}

resource "aws_secretsmanager_secret_version" "rds_credentials" {
  secret_id = aws_secretsmanager_secret.rds_credentials.id
  secret_string = jsonencode({
    username = var.supabase_db_username
    password = var.supabase_db_password
  })
}