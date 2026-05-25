provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Terraform  = "true"
      repository = "tp-ppi-cobre-wh-svc"
    }
  }
}

data "aws_eks_cluster_auth" "main" {
  name = data.terraform_remote_state.eks_module_state.outputs.name
}

provider "kubernetes" {
  host                   = data.terraform_remote_state.eks_module_state.outputs.endpoint
  cluster_ca_certificate = data.terraform_remote_state.eks_module_state.outputs.cluster_ca_certificate
  token                  = data.aws_eks_cluster_auth.main.token
}

provider "helm" {
  kubernetes {
    host                   = data.terraform_remote_state.eks_module_state.outputs.endpoint
    cluster_ca_certificate = data.terraform_remote_state.eks_module_state.outputs.cluster_ca_certificate
    token                  = data.aws_eks_cluster_auth.main.token
  }
}

terraform {
  backend "s3" {
    region = "us-east-1"
    key    = "ppi-cobre-wh/service/terraform.tfstate"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.43.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 3.1.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 3.1.0"
    }
  }
}

data "terraform_remote_state" "base_module_state" {
  backend = "s3"
  config = {
    bucket = "tumipay-terraform-state-${var.environment}"
    key    = "ppi-cobre-wh/base/terraform.tfstate"
    region = var.aws_region
  }
}

data "terraform_remote_state" "eks_module_state" {
  backend = "s3"
  config = {
    bucket = "tumipay-terraform-state-${var.environment}"
    key    = "aws/terraform.tfstate"
    region = var.aws_region
  }
}
