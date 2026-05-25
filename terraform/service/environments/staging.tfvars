project_name             = "ppi-cobre-wh"
environment              = "staging"
container_cpu_request    = "100m"
container_memory_request = "128Mi"
container_cpu_limit      = "500m"
container_memory_limit   = "512Mi"
service_url              = "ppi-cobre-wh.api.staging.topup.com.co"
waf_arn                  = "arn:aws:wafv2:us-east-1:819143704806:regional/webacl/java-spring-hardening/3e332bf2-b4ea-4826-beae-0b6ce4fb3cc5"
alb_logs_bucket_name     = "tumipay-logs-bucket-staging"

# HPA Configuration (optimized for Java reactive I/O bound application)
hpa_min_replicas                     = 1
hpa_max_replicas                     = 3
hpa_target_cpu_utilization           = 85
hpa_target_memory_utilization        = 80
hpa_scale_down_stabilization_seconds = 300