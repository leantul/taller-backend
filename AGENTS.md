# Engineering Guidelines

## Required workflow

- Read this file before inspecting, planning, or modifying the repository.
- Start every implementation from the latest `origin/master` on a dedicated `agent/{description}` branch; never reuse a branch that belongs to another change.
- After validation, commit only the intended files, push the branch, and open or update a draft pull request for user approval.

## Backend data access

- Never add native SQL for application data access. Use JPQL/HQL so persistence logic remains portable and is validated against the entity model; existing native infrastructure or legacy queries are not precedent for new code.
- Prefer DTO/projection queries for read endpoints.
- Do not return or fetch full JPA entities for list, grid, report, dashboard, or autocomplete views unless mutation logic requires it.
- Avoid N+1 queries. Batch-load related data by IDs when related fields are needed.
- Keep grid filtering and sorting in the backend when the grid is paginated.
- Compute summaries and aggregates in the database; do not load an entire filtered dataset into application memory to derive dashboard totals.
- Select only the fields required by the UI/API contract.
- Use entities mainly for create, update, delete, and domain mutation flows.
- Before adding a query, check whether an existing projection/query can be extended without overfetching.
- Keep repository methods purpose-specific and avoid broad "fetch everything" queries for screens that need a narrow DTO.

## Frontend grids

- Paginated grids must request sorting from the backend so the whole filtered dataset is ordered, not only the visible page.
- Never fetch a complete dataset and paginate it in the frontend with `slice()` or equivalent logic.
- Send page, size, filters, sort field, and sort direction to the backend; keep only the returned page content in frontend state.
- Paginated endpoints must return page metadata including the current page, page size, total elements, and total pages.
- Column resize behavior should be implemented consistently and persisted per grid when the surrounding UI already supports it.
- Do not add fields to grids unless they are explicitly useful for scanning or action; detail-only fields should stay in detail/edit views.
