FROM maven:3.9.6-eclipse-temurin-17-alpine

RUN mkdir dlms

WORKDIR dlms

COPY . .

RUN mvn clean install

RUN mvn package -f "mandjet-dlms-web-app"

ENV DLMS_PASSWORD=password
ENV JWT_SECRET_KEY=fd7a9f2c96f679a3f0aece924f53a27e589a19698e4ab6dd9dfea5a2b76956c4

EXPOSE 8080 4060

ENTRYPOINT ["java", "-jar", "./mandjet-dlms-web-app/target/mandjet-dlms-web-app-0.0.1-SNAPSHOT.jar"]