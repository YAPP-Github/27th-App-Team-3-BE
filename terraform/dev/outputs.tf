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
  description = "Blue Target Group ARN"
  value       = aws_lb_target_group.blue.arn
}

output "green_target_group_arn" {
  description = "Green Target Group ARN"
  value       = aws_lb_target_group.green.arn
}

# ECS Outputs
output "ecs_cluster_name" {
  description = "ECS Cluster name"
  value       = aws_ecs_cluster.main.name
}

output "ecs_cluster_arn" {
  description = "ECS Cluster ARN"
  value       = aws_ecs_cluster.main.arn
}

output "ecs_service_name" {
  description = "ECS Service name"
  value       = aws_ecs_service.main.name
}

output "ecs_task_definition_arn" {
  description = "ECS Task Definition ARN"
  value       = aws_ecs_task_definition.main.arn
}

# RDS Outputs
output "rds_endpoint" {
  description = "RDS endpoint"
  value       = aws_db_instance.main.endpoint
}

output "rds_address" {
  description = "RDS address"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "RDS port"
  value       = aws_db_instance.main.port
}

output "rds_database_name" {
  description = "RDS database name"
  value       = aws_db_instance.main.db_name
}

output "rds_instance_id" {
  description = "RDS instance ID"
  value       = aws_db_instance.main.id
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
output "ecs_log_group_name" {
  description = "ECS CloudWatch Log Group name"
  value       = aws_cloudwatch_log_group.ecs.name
}

# Secrets Manager Outputs
output "rds_credentials_secret_arn" {
  description = "RDS credentials secret ARN"
  value       = aws_secretsmanager_secret.rds_credentials.arn
  sensitive   = true
}

# Security Group Outputs
output "alb_security_group_id" {
  description = "ALB Security Group ID"
  value       = aws_security_group.alb.id
}

output "ecs_task_security_group_id" {
  description = "ECS Task Security Group ID"
  value       = aws_security_group.ecs_task.id
}

output "rds_security_group_id" {
  description = "RDS Security Group ID"
  value       = aws_security_group.rds.id
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
    - ECS Cluster: ${aws_ecs_cluster.main.name}
    - ECS Service: ${aws_ecs_service.main.name}

    Database:
    - RDS Endpoint: ${aws_db_instance.main.endpoint}
    - Database Name: ${aws_db_instance.main.db_name}

    Storage:
    - S3 Bucket: ${aws_s3_bucket.static.id}
    - CloudFront: ${aws_cloudfront_distribution.main.domain_name}

    Container Registry:
    - ECR: ${aws_ecr_repository.main.repository_url}

    Deployment:
    - CodeDeploy App: ${aws_codedeploy_app.main.name}

    Next Steps:
    1. Push Docker image to ECR: ${aws_ecr_repository.main.repository_url}
    2. Access application at: http://${aws_lb.main.dns_name}
    3. Configure domain in Route53 (if applicable)
    4. Update RDS Security Group with your IP for local access
    ========================================
  EOT
}