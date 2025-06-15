# For documentation see https://jboss-container-images.github.io/openjdk/
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest@sha256:1f7f617740249699889895f6678a9a9048a4f94af5060349fe1aadd37e1fd6a6

COPY target/*.jar /deployments/spring-boot-application.jar
