# Database access security

## Production on Render

- Keep `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` only in the backend service environment.
- Prefer Render's internal PostgreSQL URL when the backend and database are in the same region.
- Do not put database credentials in GitHub, Vercel, frontend assets, Docker images, or documentation.
- In the PostgreSQL service's **Networking** settings, disable external access when it is not required. Otherwise, restrict the IP allow list; do not leave `0.0.0.0/0` enabled.
- After changing a database credential, deploy the backend and verify it before revoking the old credential.

The application also requires a runtime `JWT_SECRET`. It belongs in the Render backend environment and must not be exposed to the frontend.

## Local development

Copy `.env.example` to `.env`, provide unique development-only values, and run Docker Compose. The `.env` file is ignored by Git.

The Compose PostgreSQL service is reachable by the backend only through the private Compose network. It intentionally does not publish port `5432` to the host. If temporary host access is needed for a database client, use an explicit local-only override binding such as `127.0.0.1:5432:5432`; never publish the database port on a public server.

Running Spring Boot outside Compose requires `DB_PASSWORD` to be set explicitly. No database password fallback is included in the application configuration.
