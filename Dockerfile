FROM maven:3.9.6-eclipse-temurin-17-alpine

RUN mkdir dlms

WORKDIR dlms

COPY . .

RUN mvn clean install

RUN mvn package -f "mandjet-dlms-web-app"

EXPOSE 8080 4060

ENTRYPOINT ["java", "-jar", "./mandjet-dlms-web-app/target/mandjet-dlms-web-app-0.0.1-SNAPSHOT.jar"]