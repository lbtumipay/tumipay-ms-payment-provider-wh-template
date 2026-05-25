resource "aws_ecr_repository" "main" {
  name = "${var.project_name}-${var.environment}"
}

resource "aws_secretsmanager_secret" "main" {
  name = "${var.project_name}-${var.environment}"
}