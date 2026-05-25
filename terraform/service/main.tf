resource "kubernetes_manifest" "deployment" {
  manifest = yamldecode(templatefile("${path.module}/argocd-application.tftpl.yaml", {
    region                   = var.aws_region
    name                     = var.project_name
    namespace                = "applications"
    environment              = var.environment
    secrets_name             = "${var.project_name}-${var.environment}"
    ecr_repository_url       = data.terraform_remote_state.base_module_state.outputs.ecr_repository_url
    ecr_tag                  = var.ecr_tag
    port                     = 8000
    argocd_target_branch     = var.argocd_target_branch
    container_cpu_request    = var.container_cpu_request
    container_memory_request = var.container_memory_request
    container_cpu_limit      = var.container_cpu_limit
    container_memory_limit   = var.container_memory_limit
    domain                   = var.service_url
    waf_arn                  = var.waf_arn
    logs_bucket              = var.alb_logs_bucket_name
    hpa_min_replicas                     = var.hpa_min_replicas
    hpa_max_replicas                     = var.hpa_max_replicas
    hpa_target_cpu_utilization           = var.hpa_target_cpu_utilization
    hpa_target_memory_utilization        = var.hpa_target_memory_utilization
    hpa_scale_down_stabilization_seconds = var.hpa_scale_down_stabilization_seconds
  }))
}