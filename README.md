# Locus

A desktop app for structured focus sessions — Pomodoro, deep-work, and exam-countdown timers, distraction logging, goal tracking, analytics, and optional accountability pairing. An Electron client backed by seven Spring Boot microservices behind an API Gateway.

## Repo layout

```
/backend    — 7 Spring Boot microservices + API Gateway (Java/Maven)
/frontend   — Electron + React client (Node)
/infra      — local dev infra (docker-compose) and real AWS provisioning (Terraform)
/helm       — Kubernetes deploy charts
```

`backend/` and `frontend/` build and run independently — `backend/` needs only Java/Maven, `frontend/` only Node. They talk to each other over HTTP once both are running; neither is a build-time dependency of the other. See `CONTRIBUTING.md` for local setup.

## Status

Early scaffolding. Services are being built one at a time against a fully local stack (Postgres, Redis, LocalStack) before any real cloud infrastructure is introduced.

## Contributing

See `CONTRIBUTING.md`.

---

Internal design/architecture notes live in a private, git-ignored `docs/` directory local to the project owner — not part of this repo's tracked content.
