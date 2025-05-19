# For documentation see https://jboss-container-images.github.io/openjdk/
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest@sha256:9414aacc4fca4c7e55df94c58f66f6f91b9fd0ed88f7110642c815db92172819

COPY target/*.jar /deployments/spring-boot-application.jar
