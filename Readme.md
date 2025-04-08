digital-app/
├── api-gateway/ # Spring Cloud Gateway
├── cart-service/ # Cart management (Firebase)
├── order-service/ # Order management (Firebase)
├── product-service/ # Product catalog (Elasticsearch)
├── user-service/ # User authentication (Auth0)
├── config-server/ # Spring Cloud Config Server
├── event-publisher-service/ # For publishing events (Kafka)
├── flink-jobs/ # Flink stream processing
├── pom.xml # Parent POM


docker-compose up -d

docker-compose down

docker-compose logs -f

