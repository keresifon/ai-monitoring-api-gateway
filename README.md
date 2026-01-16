# API Gateway Service

The API Gateway serves as the single entry point for all client requests to the AI Log Monitoring System. It provides routing, load balancing, rate limiting, circuit breaking, and CORS configuration.

## Features

- **Intelligent Routing**: Routes requests to appropriate backend services
- **Rate Limiting**: Redis-backed rate limiting (10 requests/sec, burst 20)
- **Circuit Breaker**: Resilience4j-based circuit breaker for fault tolerance
- **CORS Support**: Configured for frontend applications
- **Health Monitoring**: Actuator endpoints for service health
- **Fallback Handling**: Graceful degradation when services are unavailable

## Architecture

```
Client → API Gateway (8080) → Backend Services
                              ├─ Log Ingestion (8081)
                              └─ Log Processor (8082)
```

## Configuration

### Port
- **8080** - API Gateway HTTP port

### Routes

| Path | Target Service | Port | Features |
|------|---------------|------|----------|
| `/api/v1/logs/**` | Log Ingestion | 8081 | Rate Limiting, Circuit Breaker |
| `/api/v1/processor/**` | Log Processor | 8082 | Circuit Breaker |

### Rate Limiting
- **Replenish Rate**: 10 requests/second
- **Burst Capacity**: 20 requests
- **Backend**: Redis

### Circuit Breaker Settings
- **Sliding Window**: 10 calls
- **Failure Threshold**: 50%
- **Wait Duration**: 5 seconds
- **Timeout**: 3 seconds

## Prerequisites

- Java 17+
- Maven 3.6+
- Redis (running on localhost:6379)
- Backend services running:
  - Log Ingestion Service (port 8081)
  - Log Processor Service (port 8082)

## Running the Service

### 1. Start Redis
```bash
# Redis should already be running from docker-compose
docker ps | grep redis
```

### 2. Start Backend Services
```bash
# Terminal 1 - Log Ingestion
cd backend/log-ingestion
export DB_USERNAME=admin DB_PASSWORD=admin123 RABBITMQ_USERNAME=admin RABBITMQ_PASSWORD=admin123
./mvnw spring-boot:run

# Terminal 2 - Log Processor
cd backend/log-processor
export DB_USERNAME=admin DB_PASSWORD=admin123 RABBITMQ_USERNAME=admin RABBITMQ_PASSWORD=admin123
./mvnw spring-boot:run
```

### 3. Start API Gateway
```bash
cd backend/api-gateway
./mvnw spring-boot:run
```

The gateway will start on **http://localhost:8080**

## Testing

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Submit Log via Gateway
```bash
curl -X POST http://localhost:8080/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{
    "level": "ERROR",
    "message": "Database connection timeout",
    "service": "user-service",
    "host": "prod-server-01"
  }'
```

### 3. Test Rate Limiting
```bash
# Send 25 requests rapidly (should see rate limit after 20)
for i in {1..25}; do
  curl -X POST http://localhost:8080/api/v1/logs \
    -H "Content-Type: application/json" \
    -d '{"level":"INFO","message":"Test '$i'","service":"test","host":"localhost"}' &
done
wait
```

### 4. Test Circuit Breaker
```bash
# Stop the Log Ingestion Service, then try:
curl -X POST http://localhost:8080/api/v1/logs \
  -H "Content-Type: application/json" \
  -d '{"level":"ERROR","message":"Test","service":"test","host":"localhost"}'

# Should receive fallback response:
# {
#   "status": "SERVICE_UNAVAILABLE",
#   "message": "Log Ingestion Service is temporarily unavailable...",
#   "timestamp": "...",
#   "service": "log-ingestion"
# }
```

### 5. Monitor Circuit Breaker Status
```bash
curl http://localhost:8080/actuator/health | jq
```

## Gateway Endpoints

### Actuator Endpoints
- `GET /actuator/health` - Health status
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics
- `GET /actuator/gateway/routes` - Configured routes

### Fallback Endpoints
- `POST /fallback/log-ingestion` - Log Ingestion fallback
- `GET /fallback/log-processor` - Log Processor fallback
- `GET /fallback/generic` - Generic fallback

## Configuration Files

### application.yml
Main configuration including:
- Server port
- Route definitions
- Rate limiting settings
- Circuit breaker configuration
- CORS settings
- Redis connection

## Dependencies

- **Spring Cloud Gateway**: Reactive gateway
- **Spring Boot Actuator**: Health monitoring
- **Spring Data Redis Reactive**: Rate limiting
- **Resilience4j**: Circuit breaker
- **Lombok**: Code generation

## Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Circuit Breaker Metrics
```bash
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls
```

### Gateway Routes
```bash
curl http://localhost:8080/actuator/gateway/routes | jq
```

## Troubleshooting

### Gateway Won't Start
- **Check Redis**: Ensure Redis is running on port 6379
- **Check Port**: Ensure port 8080 is not in use
- **Check Dependencies**: Run `./mvnw clean install`

### Rate Limiting Not Working
- **Check Redis Connection**: Verify Redis is accessible
- **Check Configuration**: Review `spring.data.redis` settings

### Circuit Breaker Not Triggering
- **Check Backend Services**: Ensure they're actually down
- **Check Thresholds**: Review `resilience4j.circuitbreaker` settings
- **Monitor Health**: Use `/actuator/health` to see circuit breaker state

### CORS Issues
- **Check Origin**: Verify your frontend URL is in `allowedOrigins`
- **Check Methods**: Ensure required HTTP methods are allowed
- **Check Headers**: Review `allowedHeaders` configuration

## Architecture Decisions

### Why Spring Cloud Gateway?
- **Reactive**: Non-blocking, high performance
- **Flexible**: Easy route configuration
- **Integrated**: Works seamlessly with Spring ecosystem
- **Feature-Rich**: Built-in rate limiting, circuit breaking, filters

### Why Redis for Rate Limiting?
- **Distributed**: Works across multiple gateway instances
- **Fast**: In-memory performance
- **Reliable**: Proven solution for rate limiting

### Why Resilience4j?
- **Lightweight**: No external dependencies
- **Reactive**: Works with Spring WebFlux
- **Configurable**: Fine-grained control over circuit breaker behavior

## Next Steps

1. **Add Authentication**: Implement JWT-based authentication
2. **Add Authorization**: Role-based access control
3. **Add Logging**: Request/response logging
4. **Add Metrics**: Custom metrics for monitoring
5. **Add Caching**: Response caching for read-heavy endpoints
6. **Add Load Balancing**: Multiple instances of backend services

## Made with Bob