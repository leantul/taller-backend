# Render deployment with 512 MB RAM

The Docker image is tuned to leave memory outside the Java heap for class metadata,
compiled code, thread stacks, direct buffers, and native JVM allocations. The default
`JAVA_OPTS` uses at most 45% of the container memory for heap and caps the largest
off-heap regions.

For a 512 MB instance, keep the defaults unless measurements show a specific need:

- `DB_MAX_POOL_SIZE=5`
- `DB_MIN_IDLE=1`
- `SERVER_TOMCAT_THREADS_MAX=40`
- `SERVER_TOMCAT_THREADS_MIN_SPARE=2`
- `SERVER_TOMCAT_MAX_CONNECTIONS=100`
- `SERVER_TOMCAT_ACCEPT_COUNT=50`

Render should use `/health` as the HTTP health check path. A `503` returned by
Render's edge, including for an `OPTIONS` request, usually means there is no healthy
instance available; it is not by itself a CORS response. Check the service Events and
Logs for an out-of-memory restart or a failed health check at the same timestamp.

If the instance still reaches its memory limit, capture the Render memory graph and
the log lines immediately before the restart. Override `JAVA_OPTS` only as a complete
value, because an environment value replaces the Docker default.
