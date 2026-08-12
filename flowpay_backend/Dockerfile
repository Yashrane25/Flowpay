#Stage 1: Build

#Full JDK + Maven used only to compile the application.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

#Copy pom.xml first so Docker can cache dependency downloads.
COPY pom.xml .
RUN mvn dependency:go-offline -B

#Copy the source code and build the application.
COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2: Run

#Lightweight JRE image used only to run the compiled application.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

#Copy only the final JAR from the build stage.
COPY --from=build /app/target/*.jar app.jar

#Expose the Spring Boot application port.
EXPOSE 8080

#Start the Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]