# Terraform — real AWS infrastructure

Provisions the actual AWS resources backing staging and production: VPC, EKS, RDS, ElastiCache, security groups, WAF, SNS/SQS, ECR, and the S3+CloudFront feed serving the frontend's signed installer updates. Not needed to build or run the product locally — see `infra/local` for that.
