#!/bin/bash

set -e

ENV="${ENV:-test}"

export ENV

# Function to wait for service with timeout
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    echo "Checking $service_name health..."
    while [ $attempt -le $max_attempts ]; do
        if curl -f "$url" > /dev/null 2>&1; then
            echo "$service_name is ready!"
            return 0
        fi
        echo "Waiting for $service_name... (attempt $attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done

    echo "❌ $service_name failed to start within timeout"
    return 1
}

# Create network first if it doesn't exist
echo "Creating Docker network..."
docker network create github_repos_observer_network || true

# Start backend service
echo "Starting GitHub Repos Observer Backend..."
docker compose -f backend/docker-compose.yml up -d --build

# Wait for backend service to be ready
echo "Waiting for backend service to be ready..."
sleep 10

# Health check for backend
if ! wait_for_service "http://localhost:8080/actuator/health" "Backend"; then
    echo "❌ Backend failed to start. Checking logs:"
    docker compose -f backend/docker-compose.yml logs
    exit 1
fi

# Start frontend service
echo "Starting GitHub Repos Observer Frontend..."
docker compose -f frontend/docker-compose.yml up -d --build

# Wait for frontend service to be ready
echo "Waiting for frontend service to be ready..."
sleep 5

# Health check for frontend
if ! wait_for_service "http://localhost:4200/health" "Frontend"; then
    echo "❌ Frontend failed to start. Checking logs:"
    docker compose -f frontend/docker-compose.yml logs
    exit 1
fi

echo "✅ Both services are running successfully!"
echo "🌐 Backend available at: http://localhost:8080"
echo "🌐 Frontend available at: http://localhost:4200"
echo "📊 Backend health: http://localhost:8080/actuator/health"
echo ""
echo "📋 Tailing logs for all containers. Press Ctrl+C to stop and shut down both services."

# Trap Ctrl+C and stop both stacks on exit
trap 'echo "\n🛑 Stopping both services..."; docker compose -f frontend/docker-compose.yml down; docker compose -f backend/docker-compose.yml down; exit 0' SIGINT

# Tail logs for both stacks (interleaved)
docker compose -f backend/docker-compose.yml logs -f &
LOGS_PID_BACKEND=$!
docker compose -f frontend/docker-compose.yml logs -f &
LOGS_PID_FRONTEND=$!

wait $LOGS_PID_BACKEND $LOGS_PID_FRONTEND
