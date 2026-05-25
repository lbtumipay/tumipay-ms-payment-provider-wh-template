provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Terraform  = "true"
      repository = "tp-ppi-cobre-wh-svc"
    }
  }
}

terraform {
  backend "s3" {
    region = "us-east-1"
    key    = "ppi-cobre-wh/base/terraform.tfstate"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.43.0"
    }
  }
}

data "terraform_remote_state" "infrastructure" {
  backend = "s3"
  config = {
    bucket = "tumipay-terraform-state-${var.environment}"
    key    = "aws/terraform.tfstate"
    region = var.aws_region
  }
}