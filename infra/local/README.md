# Local backend infrastructure

`docker-compose` stack for running the backend entirely locally: Postgres (one instance, one least-privilege database/user per service), Redis, LocalStack (SQS/SNS/Secrets Manager mocked), and Mailpit (local SMTP catcher standing in for SES). Nothing in `backend/` talks to real AWS until real infrastructure is provisioned separately, under `infra/terraform`.

## One-time setup: LocalStack account + auth token

As of March 2026, LocalStack requires a free account and an auth token to run at all — even its non-commercial "Hobby" tier, which is what this project uses. There's no paid plan involved for local dev:

1. Sign up at https://app.localstack.cloud (free).
2. Copy your auth token from the web app.
3. Copy `.env.example` to `.env` in this folder and paste the token in as `LOCALSTACK_AUTH_TOKEN=...`. `.env` is git-ignored — never commit the real token.

## Running the stack

```
docker compose up -d
```

- **Postgres**: `localhost:5433` (mapped off the default 5432 to avoid clashing with any other local Postgres instance), superuser `postgres`/`postgres`. Each service gets its own database + owning user, created by `postgres/init/01-databases.sql` on first boot.
- **Redis**: `localhost:6379`.
- **LocalStack**: `localhost:4566`. SNS topics, SQS queues, and DLQs for every event in `technical-spec.md` §7's catalog are created automatically by `localstack/init/01-create-topics-and-queues.sh` once LocalStack reports ready.
- **Mailpit**: SMTP on `localhost:1025`, web UI at `http://localhost:8025` to see captured emails.

Tear down with `docker compose down` (add `-v` to also wipe the Postgres volume and LocalStack's mocked state).
