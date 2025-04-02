# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
COPY olat.local.properties /usr/local/tomcat/lib/
RUN mvn clean package -DskipTests

# Runtime stage
FROM tomcat:10.1-jdk17
WORKDIR /usr/local/tomcat

# Copy the OLAT configuration files
COPY config/tomcat/log4j2.xml /usr/local/tomcat/lib/
COPY config/tomcat/setenv.sh /usr/local/tomcat/bin/
COPY config/tomcat/server.xml /usr/local/tomcat/conf/
COPY config/tomcat/ROOT.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY config/tomcat/start.sh /usr/local/tomcat/

RUN rm -rf webapps/*
COPY --from=builder /build/target/*.war webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]