FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml aot-jar.properties ./
RUN mvn dependency:go-offline -B

COPY src/ src/
RUN mvn package -DskipTests -B

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/wikiaim-backend-0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
