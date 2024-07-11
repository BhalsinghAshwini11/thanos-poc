# Getting Started
Please note this is maven project with java 21 and spring boot 3.3 and latest proto 3 !!
There are many ways you can configure your app to expose prometheus metrics, see few option mentioned below.

# Disclaimer
The POC is purely for learning/exploration purpose locally and can have bugs or misconfigurations for diff env or
machines. Recommend proper testing correct optimizations as per your case.
Few options have been tried under this POC

### Option 1, APP(Springboot-promMetrics) -> PromSever -> thanos receiver
See option 1 at TBA project link

### Option 2, APP(Springboot-otelMetrics) -> OTEL collector -> thanos receiver
See option 3 at TBA project link

### Option 3, APP(Springboot-promMetrics) -> ThanosExporterLib -> thanos receiver
Explored here as Library POC in this code base

### Option 4, APP(Springboot-promMetrics) -> telegraph -> thanos receiver
TBA

# How to run project
* Build the Spring Boot application -> _./mvnw clean package_
* Build Docker container -> _docker build -t thanos-exporter ._
* Run the Docker container ->  _docker run -p 8080:8080 thanos-exporter_
* OR  _docker-compose up -d_ 
* Cleanup -> _docker-compose down -v_

# TODOs
* Add an extra label i.e. exporterName, reference of senderApp
* Add more logging when got error responses from thanos receiver 