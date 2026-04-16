#!/bin/bash

# FlightBoard Kubernetes Deployment Script
# Usage: ./deploy.sh [command] [environment]
# Commands: deploy, check, delete, logs, port-forward, rollout
# Environments: dev, staging, prod (default: dev)

set -e

ENVIRONMENT=${2:-dev}
K8S_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${ENVIRONMENT}"
APP_NAME="flight-board"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Check kubectl is installed
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        echo_error "kubectl not found. Please install kubectl."
        exit 1
    fi
    echo_info "kubectl version: $(kubectl version --short 2>/dev/null || echo 'connected')"
}

# Create namespace if it doesn't exist
create_namespace() {
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        echo_info "Creating namespace: $NAMESPACE"
        kubectl create namespace "$NAMESPACE"
    else
        echo_info "Namespace already exists: $NAMESPACE"
    fi
}

# Deploy using kustomize
deploy() {
    check_kubectl
    create_namespace
    
    echo_info "Deploying to $ENVIRONMENT environment..."
    # Apply kustomize with explicit namespace flag
    kubectl apply -n "$NAMESPACE" -k "$K8S_DIR"
    
    echo_info "Deployment complete!"
    echo_info "Checking rollout status..."
    kubectl rollout status deployment/$APP_NAME -n "$NAMESPACE" --timeout=5m
}

# Check deployment status
check_status() {
    check_kubectl
    
    echo_info "Checking deployment status in namespace: $NAMESPACE"
    echo ""
    
    echo_info "Deployments:"
    kubectl get deployments -n "$NAMESPACE"
    echo ""
    
    echo_info "Pods:"
    kubectl get pods -n "$NAMESPACE"
    echo ""
    
    echo_info "Services:"
    kubectl get services -n "$NAMESPACE"
    echo ""
    
    echo_info "HPA Status:"
    kubectl get hpa -n "$NAMESPACE"
}

# Delete deployment
delete_deployment() {
    check_kubectl
    
    echo_warning "Deleting deployment from $ENVIRONMENT environment..."
    read -p "Are you sure? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        echo_info "Deletion cancelled."
        exit 0
    fi
    
    kubectl delete -k "$K8S_DIR" --namespace="$NAMESPACE" || true
    echo_info "Deployment deleted."
}

# View logs
view_logs() {
    check_kubectl
    
    TAIL=${3:-100}
    
    echo_info "Fetching logs from namespace: $NAMESPACE (last $TAIL lines)"
    kubectl logs -f deployment/$APP_NAME -n "$NAMESPACE" --tail=$TAIL --timestamps=true
}

# Port forward to local machine
port_forward() {
    check_kubectl
    
    LOCAL_PORT=${3:-8080}
    
    echo_info "Setting up port-forward: localhost:$LOCAL_PORT -> $NAMESPACE/$APP_NAME:8080"
    echo_warning "Press Ctrl+C to stop"
    kubectl port-forward -n "$NAMESPACE" svc/$APP_NAME $LOCAL_PORT:8080
}

# Rollout status
rollout_status() {
    check_kubectl
    
    echo_info "Checking rollout status..."
    kubectl rollout status deployment/$APP_NAME -n "$NAMESPACE" --timeout=5m
}

# Restart deployment
restart_deployment() {
    check_kubectl
    
    echo_info "Restarting deployment..."
    kubectl rollout restart deployment/$APP_NAME -n "$NAMESPACE"
    kubectl rollout status deployment/$APP_NAME -n "$NAMESPACE" --timeout=5m
}

# Show help
show_help() {
    cat << EOF
FlightBoard Kubernetes Deployment Script

Usage: $0 [command] [environment]

Commands:
  deploy       - Deploy application (default)
  check        - Check deployment status
  delete       - Delete deployment
  logs         - View application logs
  port-forward - Forward local port to service
  rollout      - Check rollout status
  restart      - Restart deployment
  help         - Show this help message

Environments:
  dev          - Development (default)
  staging      - Staging
  prod         - Production

Examples:
  $0 deploy dev
  $0 check prod
  $0 logs staging
  $0 port-forward prod 9000

EOF
}

# Main script logic
COMMAND=${1:-deploy}

case "$COMMAND" in
    deploy)
        deploy
        ;;
    check)
        check_status
        ;;
    delete)
        delete_deployment
        ;;
    logs)
        view_logs
        ;;
    port-forward)
        port_forward
        ;;
    rollout)
        rollout_status
        ;;
    restart)
        restart_deployment
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo_error "Unknown command: $COMMAND"
        show_help
        exit 1
        ;;
esac
