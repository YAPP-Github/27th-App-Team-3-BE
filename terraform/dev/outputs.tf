# VPC Outputs
output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public Subnet IDs"
  value       = aws_subnet.public[*].id
}

# ALB Outputs
output "alb_dns_name" {
  description = "ALB DNS name"
  value       = aws_lb.main.dns_name
}

output "alb_zone_id" {
  description = "ALB Zone ID"
  value       = aws_lb.main.zone_id
}

output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.main.arn
}

# Target Group Outputs
output "blue_target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.blue.arn
}

# EC2 Outputs
output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.main.id
}

output "ec2_public_ip" {
  description = "EC2 public IP"
  value       = aws_instance.main.public_ip
}

# S3 Outputs
output "s3_bucket_name" {
  description = "S3 bucket name"
  value       = aws_s3_bucket.static.id
}

output "s3_bucket_arn" {
  description = "S3 bucket ARN"
  value       = aws_s3_bucket.static.arn
}

output "s3_bucket_domain_name" {
  description = "S3 bucket domain name"
  value       = aws_s3_bucket.static.bucket_regional_domain_name
}

# CloudFront Outputs
output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = aws_cloudfront_distribution.main.id
}

output "cloudfront_domain_name" {
  description = "CloudFront domain name"
  value       = aws_cloudfront_distribution.main.domain_name
}

# ECR Outputs
output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = aws_ecr_repository.main.repository_url
}

output "ecr_repository_arn" {
  description = "ECR repository ARN"
  value       = aws_ecr_repository.main.arn
}

# CodeDeploy Outputs
output "codedeploy_app_name" {
  description = "CodeDeploy application name"
  value       = aws_codedeploy_app.main.name
}

output "codedeploy_deployment_group_name" {
  description = "CodeDeploy deployment group name"
  value       = aws_codedeploy_deployment_group.main.deployment_group_name
}

# CloudWatch Outputs
output "log_group_name" {
  description = "CloudWatch Log Group name"
  value       = aws_cloudwatch_log_group.ecs.name
}

# Secrets Manager Outputs
output "db_credentials_secret_arn" {
  description = "DB credentials secret ARN"
  value       = aws_secretsmanager_secret.rds_credentials.arn
  sensitive   = true
}

# Redis Outputs
output "redis_endpoint" {
  description = "Redis endpoint"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].address
}

output "redis_port" {
  description = "Redis port"
  value       = aws_elasticache_cluster.redis.cache_nodes[0].port
}

# Security Group Outputs
output "alb_security_group_id" {
  description = "ALB Security Group ID"
  value       = aws_security_group.alb.id
}

output "ec2_security_group_id" {
  description = "EC2 Security Group ID"
  value       = aws_security_group.ec2.id
}

# Summary Output
output "deployment_summary" {
  description = "Deployment summary"
  value = <<-EOT
    ========================================
    Deployment Summary
    ========================================
    Environment: ${var.environment}
    Region: ${var.aws_region}

    Application:
    - ALB DNS: ${aws_lb.main.dns_name}
    - EC2 Instance: ${aws_instance.main.id}
    - EC2 Public IP: ${aws_instance.main.public_ip}

    Database:
    - Supabase Host: ${var.supabase_db_host}
    - Supabase DB: ${var.supabase_db_name}

    Cache:
    - Redis: ${aws_elasticache_cluster.redis.cache_nodes[0].address}

    Storage:
    - S3 Bucket: ${aws_s3_bucket.static.id}
    - CloudFront: ${aws_cloudfront_distribution.main.domain_name}

    Container Registry:
    - ECR: ${aws_ecr_repository.main.repository_url}

    Deployment:
    - CodeDeploy App: ${aws_codedeploy_app.main.name}
    ========================================
  EOT
}