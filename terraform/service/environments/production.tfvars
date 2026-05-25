project_name             = "ppi-cobre-wh"
environment              = "production"
container_cpu_request    = "500m"
container_memory_request = "512Mi"
container_cpu_limit      = "1000m"
container_memory_limit   = "1024Mi"
service_url              = "ppi-cobre-wh.api.v2.topup.com.co"
waf_arn                  = "arn:aws:wafv2:us-east-1:345594573245:regional/webacl/java-spring-hardening/d5849da4-b495-424f-a1ee-e46cbafc96a4"
alb_logs_bucket_name     = "tumipay-logs-bucket-production"

# HPA Configuration (optimized for Java reactive I/O bound application)
hpa_min_replicas                     = 2
hpa_max_replicas                     = 10
hpa_target_cpu_utilization           = 80
hpa_target_memory_utilization        = 75
hpa_scale_down_stabilization_seconds = 300