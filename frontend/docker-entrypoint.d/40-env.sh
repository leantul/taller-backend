#!/bin/sh
set -eu

API_URL_VALUE="${API_URL:-http://localhost:8080}"
AUTH_URL_VALUE="${AUTH_URL:-${API_URL_VALUE%/}/auth}"

cat > /usr/share/nginx/html/assets/env.js <<EOC
window.__env = window.__env || {
  API_URL: '${API_URL_VALUE}',
  AUTH_URL: '${AUTH_URL_VALUE}'
};
EOC
