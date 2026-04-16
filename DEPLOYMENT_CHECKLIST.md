# FlightBoard Deployment Checklist

## 🐳 Docker Deployment Checklist

### Pre-Deployment
- [ ] Docker installed (`docker --version`)
- [ ] Docker daemon running
- [ ] Copy `.env.example` to `.env` and fill in credentials
- [ ] Sufficient disk space for images

### Build & Test
- [ ] Build Docker image: `./docker-build.sh latest`
- [ ] Test locally with `docker-compose up`
- [ ] Application accessible at `http://localhost:8080`
- [ ] Check logs: `docker-compose logs -f flight-board`
- [ ] Health check passes: `curl http://localhost:8080/actuator/health`
- [ ] Verify API works: Check `/api/flights` endpoint

### Production Deployment
- [ ] Push image to registry: `./docker-build.sh v1.0 myregistry.azurecr.io`
- [ ] Verify image in registry
- [ ] Configure external storage if needed (volumes)
- [ ] Set up container restart policy
- [ ] Configure environment variables for production
- [ ] Set up monitoring and logging

### Post-Deployment
- [ ] Container running: `docker ps`
- [ ] No error logs: `docker logs`
- [ ] Health checks passing
- [ ] Application responding to requests
- [ ] Memory/CPU within limits

---

## ☸️ Kubernetes Deployment Checklist

### Pre-Deployment
- [ ] `kubectl` installed and configured
- [ ] Connected to K8s cluster: `kubectl cluster-info`
- [ ] Container image available (in registry or local)
- [ ] Check cluster has sufficient resources: `kubectl top nodes`

### Preparation
- [ ] Update `k8s/secret.yaml` with actual credentials
- [ ] Configure `k8s/configmap.yaml` for your environment
- [ ] Verify image tag in deployment matches your registry
- [ ] Review resource limits in `k8s/deployment.yaml`

### Deployment
- [ ] Deploy: `./k8s/deploy.sh deploy dev` (or your environment)
- [ ] Check rollout: `kubectl rollout status deployment/flight-board`
- [ ] Verify pods running: `kubectl get pods -l app=flight-board`
- [ ] Check services: `kubectl get svc flight-board`

### Validation
- [ ] All pods are Ready (1/1): `kubectl get pods`
- [ ] No crash loops: Check pod events `kubectl describe pod <pod-name>`
- [ ] Health endpoints responding: 
  ```bash
  kubectl port-forward svc/flight-board 8080:8080
  curl http://localhost:8080/actuator/health
  ```
- [ ] Application logs look good: `kubectl logs -f deployment/flight-board`
- [ ] Metrics available: `kubectl get --raw /metrics`

### Post-Deployment  
- [ ] Set up ingress for external access (if needed)
- [ ] Configure auto-scaling: Check HPA status `kubectl get hpa`
- [ ] Set up monitoring (Prometheus, etc.)
- [ ] Configure log aggregation (ELK, Splunk, etc.)
- [ ] Backup secrets location noted
- [ ] Disaster recovery plan documented

---

## 🔒 Security Checklist

### Secrets Management
- [ ] Credentials NOT in Git repository
- [ ] `.env` file in `.gitignore`
- [ ] Use external secret manager for production (Vault, Sealed Secrets, etc.)
- [ ] Rotate credentials regularly
- [ ] Audit secret access logs

### Container Security
- [ ] Container runs as non-root user
- [ ] Read-only root filesystem (where possible)
- [ ] Resource limits set (CPU, memory)
- [ ] Security context configured (`runAsNonRoot=true`)

### Kubernetes Security
- [ ] RBAC configured (roles, role bindings)
- [ ] Network policies defined
- [ ] Pod security policies enforced
- [ ] Service account has minimal permissions
- [ ] Audit logging enabled

### Network Security
- [ ] TLS/HTTPS enabled (ingress with SSL certificate)
- [ ] Ingress controller configured
- [ ] Network policies restrict pod communication
- [ ] Firewall rules reviewed

---

## 📊 Monitoring Checklist

### Application Metrics
- [ ] Actuator endpoints exposed: `/actuator/health`, `/actuator/metrics`
- [ ] Prometheus metrics enabled: `/actuator/prometheus`
- [ ] Custom metrics exported (if applicable)
- [ ] Alert thresholds defined

### Kubernetes Metrics
- [ ] Pod metrics visible: `kubectl top pods`
- [ ] Node metrics visible: `kubectl top nodes`
- [ ] HPA metrics working (if using autoscaling)
- [ ] Monitoring stack installed (Prometheus, Grafana, etc.)

### Logging
- [ ] Logs aggregated to central location
- [ ] Log levels appropriate for environment
- [ ] Access logs captured
- [ ] Error logs monitored and alerted
- [ ] Log retention policy defined

### Alerts
- [ ] Application down alert configured
- [ ] High memory usage alert
- [ ] High CPU usage alert
- [ ] Failed health check alert
- [ ] Deployment failure alert

---

## 🚀 Scaling Checklist

### Manual Scaling
- [ ] Verified deployment can scale: `kubectl scale deployment flight-board --replicas=5`
- [ ] Load balancer distributes traffic properly
- [ ] No data loss on scale events

### Auto-Scaling
- [ ] HPA resource configured
- [ ] Min/max replicas set appropriately
- [ ] CPU target threshold appropriate
- [ ] Memory target threshold appropriate
- [ ] Tested scaling up and down

---

## 🔄 Updates & Maintenance

### Before Update
- [ ] Backup current state
- [ ] Document current version
- [ ] Review update notes
- [ ] Test in staging first

### During Update
- [ ] Rolling update configured in deployment
- [ ] Readiness probes working properly
- [ ] Health checks passing during update
- [ ] No traffic errors observed

### After Update
- [ ] New version running: `kubectl get deployment`
- [ ] Pods healthy: `kubectl get pods`
- [ ] Application functionality verified
- [ ] Performance metrics normal
- [ ] No increase in errors: `kubectl logs`

---

## 🆘 Troubleshooting Checklist

### Deployment Issues
- [ ] Check pod events: `kubectl describe pod <pod-name>`
- [ ] View logs: `kubectl logs <pod-name>`
- [ ] Check resource availability: `kubectl top nodes`
- [ ] Verify image exists and is accessible
- [ ] Check resource requests/limits

### Performance Issues
- [ ] Check CPU usage: `kubectl top pods`
- [ ] Check memory usage: `kubectl top pods`
- [ ] Review application logs for errors
- [ ] Check external API calls (OpenSky)
- [ ] Monitor network latency

### Connectivity Issues
- [ ] Port forward and test locally:
  ```bash
  kubectl port-forward svc/flight-board 8080:8080
  curl http://localhost:8080
  ```
- [ ] Check service endpoints: `kubectl get endpoints`
- [ ] Verify ingress configuration
- [ ] Check network policies
- [ ] Verify DNS resolution

---

## 📋 Documentation

- [ ] Architecture document created
- [ ] Runbook for common operations created
- [ ] Disaster recovery procedure documented
- [ ] Team trained on deployment process
- [ ] Credentials management procedure documented

---

## ✅ Final Verification

- [ ] Application accessible and responding
- [ ] All endpoints working
- [ ] Monitoring and alerting active
- [ ] Backup strategy in place
- [ ] Team aware of deployment
- [ ] Documentation up to date
- [ ] Ready for production traffic

---

**Deployment Date:** ________________  
**Deployed By:** ________________  
**Approval:** ________________  

