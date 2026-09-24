# Render deployment with 512 MB RAM

The backend image reserves memory outside the Java heap for class metadata, compiled
code, thread stacks, direct buffers, PDF rendering, and native JVM allocations. Its
default heap ceiling is 45% of the container memory.

For a 512 MB instance, start with these defaults:

- `DB_MAX_POOL_SIZE=5`
- `DB_MIN_IDLE=1`
- `SERVER_TOMCAT_THREADS_MAX=40`
- `SERVER_TOMCAT_THREADS_MIN_SPARE=2`
- `SERVER_TOMCAT_MAX_CONNECTIONS=100`
- `SERVER_TOMCAT_ACCEPT_COUNT=50`

Render should use `/health` as its HTTP health check path. A `503` from Render's edge
usually means that no healthy instance was available; inspect the service events and
logs for an out-of-memory restart or a failed health check at the same timestamp.

Override `JAVA_OPTS` only as a complete value because an environment value replaces
the Docker default. Increase heap, thread, or connection limits only after measuring
RSS, garbage collection, database wait time, and PDF concurrency under representative
load.
