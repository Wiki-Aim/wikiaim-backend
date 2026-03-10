FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml aot-jar.properties ./
RUN mvn dependency:go-offline -B

COPY src/ src/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:25-jre
RUN groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app
WORKDIR /app

COPY --from=build --chown=app:app /app/target/wikiaim-backend-0.1.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
