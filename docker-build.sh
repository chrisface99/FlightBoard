#!/bin/bash

# Docker Build Script for FlightBoard
# Usage: ./docker-build.sh [tag] [registry]
# Example: ./docker-build.sh latest myregistry.azurecr.io

set -e

TAG=${1:-latest}
REGISTRY=${2:-}
IMAGE_NAME="flight-board"
FULL_IMAGE_NAME="${REGISTRY:+$REGISTRY/}${IMAGE_NAME}:${TAG}"

echo "Building Docker image: $FULL_IMAGE_NAME"

# Build the image
docker build -t "$FULL_IMAGE_NAME" -f Dockerfile .

if [ $? -eq 0 ]; then
    echo "✓ Docker image built successfully: $FULL_IMAGE_NAME"
    
    if [ -n "$REGISTRY" ]; then
        echo "Pushing to registry..."
        docker push "$FULL_IMAGE_NAME"
        if [ $? -eq 0 ]; then
            echo "✓ Image pushed successfully: $FULL_IMAGE_NAME"
        else
            echo "✗ Failed to push image"
            exit 1
        fi
    fi
else
    echo "✗ Failed to build Docker image"
    exit 1
fi
