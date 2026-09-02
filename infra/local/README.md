# Local backend infrastructure

`docker-compose` stack for running the backend entirely locally: Postgres (one instance, one database per service), Redis, LocalStack (SQS/SNS/Secrets Manager mocked), and Mailpit (local SMTP catcher standing in for SES). Nothing in `backend/` talks to real AWS until real infrastructure is provisioned separately, under `infra/terraform`.
