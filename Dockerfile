FROM maven:3.8.4-openjdk-17 AS build
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml dependency:resolve
COPY src /usr/src/app/src
RUN mvn -f /usr/src/app/pom.xml clean package
FROM openjdk:17-jdk-slim
COPY --from=build /usr/src/app/target/BIPS_WS23-0.0.1-SNAPSHOT.jar /usr/app/myapp.jar
CMD ["java", "-jar", "/usr/app/BIPS_WS23-0.0.1-SNAPSHOT.jar"]
