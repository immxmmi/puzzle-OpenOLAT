# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM tomcat:10.1.35-jre17
RUN apt-get update && apt-get install -y gettext && rm -rf /var/lib/apt/lists/*

ENV LC_ALL=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

WORKDIR /usr/local/tomcat


COPY  config/tomcat/setenv.sh bin/
COPY  config/tomcat/server.xml conf/
COPY  olat.local.properties lib/
COPY  config/tomcat/ROOT.template.xml conf/Catalina/localhost/
COPY  config/tomcat/log4j2.xml lib/


COPY config/tomcat/start.sh /bin/
RUN chmod +x /bin/start.sh

RUN rm -rf webapps/*
COPY --from=builder /build/target/*.war webapps/ROOT.war

EXPOSE 8080
CMD ["start.sh"]