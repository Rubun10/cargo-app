FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/CargoAssign-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "if [ -n \"$DB_URL\" ]; then case \"$DB_URL\" in postgresql://*|postgres://*) export DB_URL=\"jdbc:$DB_URL\" ;; esac; fi; java -Dserver.port=${PORT:-8080} -jar app.jar"]
