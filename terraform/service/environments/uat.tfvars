project_name             = "ppi-cobre-wh"
environment              = "uat"
container_cpu_request    = "100m"
container_memory_request = "128Mi"
container_cpu_limit      = "500m"
container_memory_limit   = "512Mi"
service_url              = "ppi-cobre-wh.api.uat.topup.com.co"
waf_arn                  = "arn:aws:wafv2:us-east-1:512795166910:regional/webacl/java-spring-hardening/9047fa2d-20be-45f3-b703-9ba806060c1f"
alb_logs_bucket_name     = "tumipay-logs-bucket-uat"   

# HPA Configuration (optimized for Java reactive I/O bound application)
hpa_min_replicas                     = 1
hpa_max_replicas                     = 3
hpa_target_cpu_utilization           = 85
hpa_target_memory_utilization        = 80
hpa_scale_down_stabilization_seconds = 300