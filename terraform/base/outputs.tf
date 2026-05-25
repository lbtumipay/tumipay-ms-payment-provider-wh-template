output "ecr_repository_url" {
  value = aws_ecr_repository.main.repository_url
}

output "secrets_manager_arn" {
  value = aws_secretsmanager_secret.main.arn
}

output "secrets_manager_name" {
  value = aws_secretsmanager_secret.main.name
}