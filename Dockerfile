FROM node:16 AS frontend

WORKDIR /app

COPY . .

RUN cd src/main/frontend/ && \
    npm install && \
    npm run build

FROM maven:3.8.5-openjdk-17 AS backend

WORKDIR /app

COPY --from=frontend /app/ .

RUN mvn dependency:go-offline && \
    mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine

RUN apk add --no-cache libxext libxrender libxtst libxi && \
    adduser -D myuser

USER myuser

WORKDIR /app

COPY --from=backend /app/target/epl_librarian-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
