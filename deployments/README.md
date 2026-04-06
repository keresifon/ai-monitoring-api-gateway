# API Gateway Deployment

The api-gateway is deployed to the shared `ai-monitoring` namespace in AKS. It routes traffic to backend services and requires Redis for rate limiting.

## Prerequisites

1. **Shared infrastructure** – Run `install-dependencies.sh` from `ai-monitoring-alert-service/deployments/` first. This creates:
   - `ai-monitoring-secrets` (includes `jwt-secret`, `redis-password`, `rabbitmq-password`, DB credentials)
   - PostgreSQL, Elasticsearch, Redis, RabbitMQ

2. **GitHub secrets** (for CI/CD deploy on main):
   - `AZURE_CREDENTIALS` – Azure service principal JSON
   - `AKS_RESOURCE_GROUP` – AKS resource group
   - `AKS_CLUSTER_NAME` – AKS cluster name

## Manual deployment

```bash
# Ensure secret exists (run install-dependencies.sh from alert-service first)
helm upgrade --install api-gateway ./charts \
  --namespace ai-monitoring \
  -f charts/values.yaml
```

## Backend service URLs

The chart uses K8s DNS for backend services. Ensure these exist in `ai-monitoring`:

| Service       | URL                       |
|---------------|---------------------------|
| auth-service  | http://auth-service:8084  |
| log-processor | http://log-processor:8082  |
| log-ingestion | http://log-ingestion:8081 |
| alert-service | http://alert-service:8083 |
| ml-service    | http://ml-service:8000    |

If a service is missing, the api-gateway will start but route calls will fail (circuit breaker will open).

## Redis

Uses `redis-master` (Bitnami Redis standalone). Requires `redis-password` in `ai-monitoring-secrets`. The install script adds Redis and the secret keys.
