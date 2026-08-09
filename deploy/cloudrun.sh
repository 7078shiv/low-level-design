#!/usr/bin/env bash
#
# Deploys the chess app to Cloud Run from the Dockerfile at the repo root.
#
#   ./deploy/cloudrun.sh my-project-id [region]
#
# The project is required rather than taken from `gcloud config`, so a deploy can
# never land in whatever project happened to be active.

set -euo pipefail

PROJECT="${1:-}"
REGION="${2:-asia-south1}"          # Mumbai, the closest region to India
SERVICE="chess"

if [[ -z "$PROJECT" ]]; then
    echo "usage: $0 <project-id> [region]" >&2
    echo "       gcloud projects list   # to see what you can deploy into" >&2
    exit 1
fi

cd "$(dirname "$0")/.."

echo "==> Deploying $SERVICE to $PROJECT / $REGION"

echo "==> Enabling the APIs Cloud Build and Cloud Run need"
gcloud services enable \
    run.googleapis.com \
    cloudbuild.googleapis.com \
    artifactregistry.googleapis.com \
    --project "$PROJECT"

# --max-instances 1 is not a cost control, it is a correctness requirement:
# games are held in memory, so a second instance would 404 a game it never saw.
echo "==> Building and deploying"
gcloud run deploy "$SERVICE" \
    --project "$PROJECT" \
    --region "$REGION" \
    --source . \
    --allow-unauthenticated \
    --max-instances 1 \
    --min-instances 0 \
    --cpu 2 \
    --memory 1Gi \
    --concurrency 40 \
    --timeout 60s \
    --set-env-vars "^##^JAVA_OPTS=-XX:MaxRAMPercentage=70##CHESS_BOT_TIME_SCALE=1"

URL=$(gcloud run services describe "$SERVICE" \
    --project "$PROJECT" --region "$REGION" --format 'value(status.url)')

echo
echo "==> Live at $URL"
echo "==> Health: $(curl -fsS "$URL/api/chess/health" || echo 'probe failed')"