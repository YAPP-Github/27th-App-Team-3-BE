# General Variables
variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "ap-northeast-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "teamtwix"
}

# Network Variables
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

# ECS Variables
variable "ecs_task_cpu" {
  description = "ECS task CPU units"
  type        = string
  default     = "512" # 0.5 vCPU
}

variable "ecs_task_memory" {
  description = "ECS task memory in MB"
  type        = string
  default     = "1024" # 1GB
}

variable "ecs_desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
}

variable "ecs_max_count" {
  description = "Maximum number of ECS tasks"
  type        = number
  default     = 2
}

variable "ecs_container_port" {
  description = "Container port for the application"
  type        = number
  default     = 8080
}

variable "ecs_health_check_path" {
  description = "Health check path for ECS service"
  type        = string
  default     = "/actuator/health"
}

# RDS Variables
variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "rds_engine_version" {
  description = "RDS MySQL engine version"
  type        = string
  default     = "8.0"
}

variable "rds_database_name" {
  description = "RDS database name"
  type        = string
  default     = "teamtwixdb"
}

variable "rds_master_username" {
  description = "RDS master username"
  type        = string
  default     = "teamtwixadmin"
  sensitive   = true
}

variable "rds_master_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}

variable "rds_backup_retention_period" {
  description = "RDS backup retention period in days"
  type        = number
  default     = 7
}

# Domain Variables
variable "domain_name" {
  description = "Domain name for the application"
  type        = string
  default     = "dev.teamtwix.com"
}

variable "api_subdomain" {
  description = "API subdomain"
  type        = string
  default     = "api"
}

variable "cdn_subdomain" {
  description = "CDN subdomain"
  type        = string
  default     = "cdn"
}

# S3 Variables
variable "s3_bucket_name" {
  description = "S3 bucket name for static files"
  type        = string
  default     = "teamtwix-dev-static"
}

# CloudWatch Variables
variable "cloudwatch_log_retention_days" {
  description = "CloudWatch Logs retention period in days"
  type        = number
  default     = 7
}

# Auto Scaling Variables
variable "autoscaling_target_cpu_utilization" {
  description = "Target CPU utilization for auto scaling"
  type        = number
  default     = 70
}

# Local IP for RDS access
variable "developer_ip" {
  description = "Developer IP address for RDS access (CIDR format)"
  type        = string
  default     = "0.0.0.0/0"
}

# Docker Image
variable "docker_image" {
  description = "Docker image URI for ECS task"
  type        = string
  default     = ""
}
