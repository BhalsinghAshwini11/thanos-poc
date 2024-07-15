# Getting Started
Please note this is maven project with java 21 and spring boot 3.3 and latest proto 3 !!
There are many ways you can configure your app to expose prometheus metrics, see few option mentioned below.

# Disclaimer
The POC is purely for learning/exploration purpose locally and can have bugs or misconfigurations for diff env or
machines. Recommend proper testing correct optimizations as per your case.
Few options have been tried under this POC

### Option 1, APP(Springboot-promMetrics) -> PromSever -> thanos receiver
See Prometheus.yml for configuration like labels and such. Enable from Docker compose file. 

### Option 2, APP(Springboot-otelMetrics) -> OTEL collector -> thanos receiver
See option 3 at TBA project link

### Option 3, APP(Springboot-promMetrics) -> ThanosExporterLib -> thanos receiver
Explored here as Library POC in this code base. Configuration in docker compose, see specific label related. Enables disable from application.properties file.

### Option 4, APP(Springboot-promMetrics) -> telegraph -> thanos receiver
TBA

# How to run project
* Build the Spring Boot application -> _./mvnw clean package_
* Build Docker container ->  _docker-compose up --build_ 
* Cleanup -> _docker-compose down -v_
* Verify metrics coming locally both from Thanos and Prometheus from following endpoints 
  1. Local Thanos Querier  http://localhost:9091
  2. Local PromQl http://localhost:9090

# TODOs
* configure S3 locally using minio
* explore other 2, 4 options 