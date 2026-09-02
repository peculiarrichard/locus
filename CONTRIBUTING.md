# Contributing to Locus

Practical, condensed engineering rules for this repo.

## Code style

- One formatter per language, enforced in CI, no exceptions.
- Lint failures are build failures.
- 100–120 char line length.
- Naming: intent-revealing, nouns for types, verbs for functions. Short over clever.
- One responsibility per class/function.

## Comments

- Every code file carries **at most one** short, single-sentence comment (a file-level description). No other inline comment blocks.
- Longer rationale (why, not what) belongs in the project's internal notes, not in code.
- Code must never reference or link to the internal `docs/` folder — it's private and git-ignored, and a reference to it would break for anyone without that folder.

## Commits & branches

- Conventional Commits (`type(scope): subject`).
- Branches: `feature/<ticket>-short-desc`, `fix/<ticket>-short-desc`, `chore/<desc>`, branched off `dev`.
- Small, atomic commits. No agent/bot identity in commit messages.
- `main` is production only — never branch off it directly, never push to it directly. `dev` is the active integration branch; `main` only advances via a `dev` → `main` PR when a release is being cut.

## Pull requests

- PR target is always `dev`, never `main` directly.
- Keep PRs under ~400 LOC where practical. Describe intent and testing in the body.
- Review criteria: correctness, tests, security, readability, backwards compatibility, performance.
- This is presently a single-operator project — self-review plus the automated CI gate (tests, lint, secret/vulnerability scans) substitutes for a second reviewer.

## Testing

- Every public function/endpoint gets a unit test. Critical paths get integration tests.
- CI runs tests on every PR; failures block merge.

## CI

- Every PR: format + lint, secret scan, unit tests, integration tests, image vulnerability scan.
- Fail fast — lint/format errors and test failures block merges.

## Error handling & logging

- Don't swallow errors — handle, wrap with context, or return them.
- Structured (JSON) logs in every service. Never log secrets or sensitive user data.

## Security & secrets

- Never commit secrets. Validate all input at service boundaries.
- Dependencies kept current; CVEs monitored (Dependabot).

## Repo layout

```
/backend/services/{auth,session,distraction,goal,analytics,accountability,notification}
/backend/gateway
/frontend
/infra/local        — docker-compose for local backend dev
/infra/terraform    — real AWS provisioning
/helm
```

`backend/` and `frontend/` are independently buildable — `backend/` needs only Java/Maven, `frontend/` only Node. Neither depends on the other's toolchain; they communicate over HTTP once both are running.

## Local dev checklist

- [ ] JDK 25 on `JAVA_HOME` (the backend build targets Java 25 — see `backend/pom.xml`)
- [ ] Node.js installed
- [ ] Docker Desktop running
- [ ] `docker compose -f infra/local/docker-compose.yml up -d` (Postgres, Redis, LocalStack, Mailpit) before running any backend service
- [ ] VS Code with the Extension Pack for Java + Spring Boot Extension Pack (backend), ESLint/Prettier/Tailwind CSS IntelliSense (frontend)

## Accessibility & i18n

- Keyboard-navigable, contrast-checked UI. Strings externalized for future localization.

---

This file is the tracked, public-facing summary of the project's internal engineering rules — the full detailed spec lives in a private, git-ignored `docs/` directory local to the project owner.
