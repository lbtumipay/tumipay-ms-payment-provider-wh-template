variable "project_name" {
  description = "The name of the project"
  type        = string
}

variable "environment" {
  description = "The environment of the project"
  type        = string
}

variable "aws_region" {
  description = "The AWS region"
  type        = string
  default     = "us-east-1"
}

variable "ecr_tag" {
  description = "The ECR tag to use for the container image"
  type        = string
}

variable "argocd_target_branch" {
  description = "The branch to target for ArgoCD"
  type        = string
}

variable "container_cpu_request" {
  description = "The CPU request for the container"
  type        = string
}

variable "container_memory_request" {
  description = "The memory request for the container"
  type        = string
}

variable "container_cpu_limit" {
  description = "The CPU limit for the container"
  type        = string
}

variable "container_memory_limit" {
  description = "The memory limit for the container"
  type        = string
}

variable "service_url" {
  description = "The service URL (domain)"
  type        = string
}

variable "waf_arn" {
  description = "The WAF ARN for the ALB"
  type        = string
}

variable "alb_logs_bucket_name" {
  description = "The bucket name to use for ALB logs"
  type        = string
}

variable "hpa_min_replicas" {
  description = "Minimum number of replicas for HPA"
  type        = number
  default     = 1
}

variable "hpa_max_replicas" {
  description = "Maximum number of replicas for HPA"
  type        = number
  default     = 10
}

variable "hpa_target_cpu_utilization" {
  description = "Target CPU utilization percentage for HPA"
  type        = number
  default     = 70
}

variable "hpa_target_memory_utilization" {
  description = "Target memory utilization percentage for HPA"
  type        = number
  default     = 80
}

variable "hpa_scale_down_stabilization_seconds" {
  description = "Stabilization window in seconds before scaling down"
  type        = number
  default     = 300
}
