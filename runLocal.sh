#!/bin/bash
mvn clean spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED"
