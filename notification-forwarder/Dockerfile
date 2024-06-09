FROM maven:3.9.6-eclipse-temurin-17-alpine

RUN mkdir dlms-forwarder

WORKDIR dlms-forwarder

COPY . .

RUN mvn clean package

EXPOSE ${FORWARDER_PORT}

ENTRYPOINT ["java", "-jar", "./target/notification-forwarder-0.0.1-SNAPSHOT.jar"]