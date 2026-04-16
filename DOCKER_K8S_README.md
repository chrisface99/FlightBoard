# FlightBoard - Docker & Kubernetes Deployment Guide

## Overview

This guide covers deploying FlightBoard using Docker and Kubernetes (K8s).

## 📦 Docker Deployment

### Prerequisites

- Docker installed and running
- Docker Compose v2.0+ (optional but recommended)

### Quick Start with Docker Compose

```bash
cd FlightBoard
docker-compose up -d
```

Access the application at: `http://localhost:8080`

### Build Custom Docker Image

```bash
# Build image with tag and push to registry
chmod +x docker-build.sh
./docker-build.sh latest

# Or push to a registry
./docker-build.sh v1.0 myregistry.azurecr.io
```

### Docker Environment Variables

Configure via docker-compose.yml:

```yaml
environment:
  OPENSKY_CLIENT_ID: your-client-id
  OPENSKY_CLIENT_SECRET: your-client-secret
  JAVA_OPTS: "-Xmx768m -Xms256m"
```

### Docker Logs

```bash
# View logs
docker-compose logs -f flight-board

# Specific container
docker logs <container_id> -f
```

### Stop & Clean Up

```bash
# Stop services
docker-compose down

# Remove volumes (careful!)
docker-compose down -v
```

---

## ☸️ Kubernetes Deployment

### Prerequisites

- `kubectl` configured and connected to a K8s cluster
- Container image pushed to a registry or locally available
- `kustomize` installed (optional but recommended)

### File Structure

```
k8s/
├── deployment.yaml      # Pod deployment spec
├── service.yaml         # Service (LoadBalancer + ClusterIP)
├── configmap.yaml       # Non-sensitive config
├── secret.yaml          # Credentials (⚠️ base64 encoded only)
├── rbac.yaml            # Service account & permissions
├── ingress.yaml         # HTTP(S) routing
├── hpa.yaml             # Auto-scaling policy
├── kustomization.yaml   # Resource organization
└── deploy.sh            # Helper script
```

### Quick Deploy with Kustomize

```bash
# Deploy to dev environment
./k8s/deploy.sh deploy dev

# Check status
./k8s/deploy.sh check dev

# View logs
./k8s/deploy.sh logs dev

# Port forward
./k8s/deploy.sh port-forward dev 8080
```

### Manual kubectl Commands

```bash
# Deploy everything
kubectl apply -k k8s/

# Specific namespace
kubectl apply -k k8s/ -n production

# Check deployment status
kubectl rollout status deployment/flight-board

# View pods
kubectl get pods -l app=flight-board

# View logs
kubectl logs -f deployment/flight-board

# Port forward
kubectl port-forward svc/flight-board 8080:8080
```

### Configuration Management

#### ConfigMap (Non-Sensitive Data)

Edit `k8s/configmap.yaml`:

```yaml
data:
  OPENSKY_OAUTH_URL: "https://opensky-network.org/api/v2/oauth/token"
  SCHEDULER_INTERVAL: "10000"
```

#### Secret (Credentials - ⚠️ Important!)

Edit `k8s/secret.yaml`:

```yaml
stringData:
  OPENSKY_CLIENT_ID: "your-client-id"
  OPENSKY_CLIENT_SECRET: "your-client-secret"
```

**⚠️ Security Warning**: 
- Secrets are only base64 encoded (NOT encrypted)
- For production, use:
  - **HashiCorp Vault**
  - **AWS Secrets Manager**
  - **Azure Key Vault**
  - **sealed-secrets** operator
  - **external-secrets** operator

```bash
# Encode a secret
echo -n "secret-value" | base64

# Decode
echo "c2VjcmV0LXZhbHVl" | base64 -d
```

### Scaling

#### Manual Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment flight-board --replicas=5

# Check HPA status
kubectl get hpa flight-board
kubectl describe hpa flight-board
```

#### Auto-Scaling (HPA)

HPA automatically scales based on CPU/memory:

```yaml
- Min replicas: 3
- Max replicas: 10
- CPU threshold: 70%
- Memory threshold: 80%
```

View HPA status:

```bash
kubectl get hpa
kubectl describe hpa flight-board
```

### Ingress Configuration

The deployment includes Ingress setup for external access:

```bash
# Check ingress
kubectl get ingress

# Describe ingress
kubectl describe ingress flight-board
```

For HTTPS with Let's Encrypt:

```bash
# Install cert-manager first
helm repo add jetstack https://charts.jetstack.io
helm install cert-manager jetstack/cert-manager --create-namespace -n cert-manager
```

Then update `host` and `issuer` in `k8s/ingress.yaml`.

### Healthchecks

The deployment includes three health probes:

1. **Startup Probe** - Allows time for app startup
2. **Liveness Probe** - Restarts unhealthy pods
3. **Readiness Probe** - Removes from load balancer if not ready

Endpoints:

```
GET /actuator/health              # Overall health
GET /actuator/health/liveness     # Liveness probe
GET /actuator/health/readiness    # Readiness probe
```

### Monitoring & Logs

#### View Logs

```bash
# Current logs
kubectl logs deployment/flight-board

# Follow logs
kubectl logs -f deployment/flight-board

# Last 100 lines with timestamps
kubectl logs deployment/flight-board --tail=100 --timestamps=true

# All pods in deployment
kubectl logs -f deployment/flight-board --all-containers=true
```

#### Metrics

```bash
# View resource usage
kubectl top pods -l app=flight-board
kubectl top nodes

# Prometheus metrics available at
GET /actuator/prometheus
```

### Troubleshooting

#### Pod not starting

```bash
# Check pod status
kubectl describe pod <pod-name>

# Check events
kubectl get events --sort-by='.lastTimestamp'

# Check logs
kubectl logs <pod-name> --previous  # Previous attempt logs
```

#### Connection issues

```bash
# Test from within cluster
kubectl exec -it <pod-name> -- curl http://localhost:8080/actuator/health

# Port forward and test
kubectl port-forward svc/flight-board 8080:8080
curl http://localhost:8080
```

#### High resource usage

```bash
# Check resource requests/limits
kubectl describe deployment flight-board

# Adjust in deployment.yaml resources section
```

### Updates & Rollouts

```bash
# Rolling update with new image
kubectl set image deployment/flight-board \
  flight-board=flight-board:v2.0

# Check rollout status
kubectl rollout status deployment/flight-board

# Rollback to previous version
kubectl rollout undo deployment/flight-board

# View rollout history
kubectl rollout history deployment/flight-board
```

### Delete Deployment

```bash
# Delete all resources
kubectl delete -k k8s/

# Or specific resource
kubectl delete deployment flight-board
```

---

## 🔒 Security Best Practices

1. **Never commit secrets** to Git
2. **Use environment variables** for credentials
3. **Implement RBAC** (included in rbac.yaml)
4. **Enable Pod Security Policies**
5. **Use private container registry**
6. **Scan images for vulnerabilities**
7. **Use network policies** for pod-to-pod communication
8. **Enable audit logging**

---

## 📊 Multi-Environment Setup

Create separate overlays for different environments:

```
k8s/
├── base/                  # Common resources
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/              # Development
    ├── staging/          # Staging
    └── prod/             # Production
```

Deploy specific environment:

```bash
kubectl apply -k k8s/overlays/production
```

---

## 🚀 CI/CD Integration

### GitHub Actions Example (`.github/workflows/deploy.yml`)

```yaml
name: Deploy to K8s

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build Docker image
        run: ./docker-build.sh ${{ github.sha }} myregistry.azurecr.io
      - name: Deploy to K8s
        run: |
          kubectl set image deployment/flight-board \
            flight-board=myregistry.azurecr.io/flight-board:${{ github.sha }}
```

---

## 🆘 Support & Documentation

- [Spring Boot on Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Deploy Spring Boot on K8s](https://kubernetes.io/docs/tutorials/stateless-application/run-stateless-application-deployment/)
- [Kustomize Guide](https://kustomize.io/)
- [K8s Documentation](https://kubernetes.io/docs/)
