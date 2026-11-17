# For documentation see https://jboss-container-images.github.io/openjdk/
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest

# The ODF Toolkit uses AWT font metrics when writing cell values.
RUN microdnf install -y fontconfig dejavu-sans-fonts \
    && microdnf clean all

COPY target/*.jar /deployments/spring-boot-application.jar
