#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_BASE="http://localhost:8080/api/v1"
FRONTEND_BASE="http://localhost:3000"
RUN_ID="docker-verify-$(date +%s)"

cd "$ROOT_DIR"

echo "==> Validating docker compose configuration"
docker compose config

echo "==> Building images"
docker compose build

echo "==> Starting stack"
docker compose up -d

echo "==> Waiting for services to become healthy"
for _ in $(seq 1 60); do
  POSTGRES_HEALTH="$(docker inspect --format='{{.State.Health.Status}}' support-ticket-postgres 2>/dev/null || echo starting)"
  BACKEND_HEALTH="$(docker inspect --format='{{.State.Health.Status}}' support-ticket-backend 2>/dev/null || echo starting)"
  FRONTEND_HEALTH="$(docker inspect --format='{{.State.Health.Status}}' support-ticket-frontend 2>/dev/null || echo starting)"

  if [[ "$POSTGRES_HEALTH" == "healthy" && "$BACKEND_HEALTH" == "healthy" && "$FRONTEND_HEALTH" == "healthy" ]]; then
    break
  fi
  sleep 5
done

echo "Postgres health: $(docker inspect --format='{{.State.Health.Status}}' support-ticket-postgres)"
echo "Backend health:  $(docker inspect --format='{{.State.Health.Status}}' support-ticket-backend)"
echo "Frontend health: $(docker inspect --format='{{.State.Health.Status}}' support-ticket-frontend)"

echo "==> Verifying Flyway migration in backend logs"
docker compose logs backend | grep -E "Flyway|Successfully applied|Schema.*is up to date" | tail -5

echo "==> Verifying frontend is reachable"
curl -fsS "$FRONTEND_BASE/" | grep -q "Support Ticket Management"

echo "==> Verifying proxied API through frontend"
curl -fsS "$FRONTEND_BASE/api/v1/tickets" | grep -q '\['

echo "==> Creating ticket via API"
CREATE_RESPONSE="$(curl -fsS -X POST "$API_BASE/tickets" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$RUN_ID title\",\"description\":\"$RUN_ID description\",\"priority\":\"HIGH\",\"assignee\":\"docker.verify\"}")"
TICKET_ID="$(printf '%s' "$CREATE_RESPONSE" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')"
echo "Created ticket id: $TICKET_ID"

echo "==> Listing tickets"
curl -fsS "$API_BASE/tickets" | grep -q "$RUN_ID title"

echo "==> Getting ticket details"
curl -fsS "$API_BASE/tickets/$TICKET_ID" | grep -q "$RUN_ID description"

echo "==> Updating ticket"
curl -fsS -X PATCH "$API_BASE/tickets/$TICKET_ID" \
  -H 'Content-Type: application/json' \
  -d '{"title":"'"$RUN_ID updated"'","description":"'"$RUN_ID updated description"'","priority":"MEDIUM","assignee":"docker.verify"}' \
  | grep -q "$RUN_ID updated"

echo "==> Adding comment"
curl -fsS -X POST "$API_BASE/tickets/$TICKET_ID/comments" \
  -H 'Content-Type: application/json' \
  -d '{"content":"'"$RUN_ID comment"'","author":"docker.verify"}' \
  | grep -q "$RUN_ID comment"

echo "==> Transitioning status OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED"
curl -fsS -X PATCH "$API_BASE/tickets/$TICKET_ID/status" \
  -H 'Content-Type: application/json' \
  -d '{"status":"IN_PROGRESS"}' | grep -q IN_PROGRESS
curl -fsS -X PATCH "$API_BASE/tickets/$TICKET_ID/status" \
  -H 'Content-Type: application/json' \
  -d '{"status":"RESOLVED"}' | grep -q RESOLVED
curl -fsS -X PATCH "$API_BASE/tickets/$TICKET_ID/status" \
  -H 'Content-Type: application/json' \
  -d '{"status":"CLOSED"}' | grep -q CLOSED

echo "==> Verifying search/filter"
curl -fsS "$API_BASE/tickets?keyword=$RUN_ID" | grep -q "$RUN_ID updated"
curl -fsS "$API_BASE/tickets?status=CLOSED" | grep -q "$RUN_ID updated"

echo "==> Restarting application without deleting volume"
docker compose restart backend frontend
sleep 20

echo "==> Verifying persisted data after restart"
curl -fsS "$API_BASE/tickets/$TICKET_ID" | grep -q "$RUN_ID updated"
curl -fsS "$API_BASE/tickets/$TICKET_ID" | grep -q "$RUN_ID comment"
curl -fsS "$API_BASE/tickets/$TICKET_ID" | grep -q CLOSED

echo "==> Docker stack verification completed successfully"
