# Engineering Guidelines

## Backend data access

- Prefer DTO/projection queries for read endpoints.
- Do not return or fetch full JPA entities for list, grid, report, dashboard, or autocomplete views unless mutation logic requires it.
- Avoid N+1 queries. Batch-load related data by IDs when related fields are needed.
- Keep grid filtering and sorting in the backend when the grid is paginated.
- Select only the fields required by the UI/API contract.
- Use entities mainly for create, update, delete, and domain mutation flows.
- Before adding a query, check whether an existing projection/query can be extended without overfetching.
- Keep repository methods purpose-specific and avoid broad "fetch everything" queries for screens that need a narrow DTO.

## Frontend grids

- Paginated grids must request sorting from the backend so the whole filtered dataset is ordered, not only the visible page.
- Column resize behavior should be implemented consistently and persisted per grid when the surrounding UI already supports it.
- Do not add fields to grids unless they are explicitly useful for scanning or action; detail-only fields should stay in detail/edit views.
