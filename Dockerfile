# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests


# Runtime stage
FROM tomcat:10.1-jdk17
WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*
COPY --from=builder /build/target/*.war webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]