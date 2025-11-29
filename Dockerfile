FROM eclipse-temurin:24-jre

LABEL description="Backend service for medical report computing"
EXPOSE 8084

WORKDIR /app

COPY ./target/abernathyclinic-report-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar" ]