# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM tomcat:10.1.35-jdk17

# Prepare system packages
RUN apt-get update && apt-get install -y gettext-base && rm -rf /var/lib/apt/lists/*

ENV LC_ALL=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

WORKDIR /usr/local/tomcat

# Copy the built WAR file from the builder stage and extract it into the webapp directory
COPY --from=builder /build/target/*.war webapp/ROOT.war
RUN jar xf webapp/ROOT.war \
    && rm webapp/ROOT.war

# Copy setenv.sh configuration
COPY ./config/bin/setenv.sh bin/setenv.sh
RUN chmod +x bin/setenv.sh

# Copy Tomcat server.xml configuration
COPY ./config/conf/server.xml conf/server.xml

# Copy and prepare start script
COPY ./config/bin/start.sh bin/start.sh
RUN chmod +x bin/start.sh

# Create necessary OpenOlat directories
RUN mkdir -p lib conf/Catalina/localhost logs olatdata webapp

# Generate olat.local.properties from template
COPY ./config/lib/olat.local.properties.template lib/olat.local.properties.template
RUN envsubst < lib/olat.local.properties.template > lib/olat.local.properties && rm lib/olat.local.properties.template

# Generate ROOT.xml from template
COPY ./config/conf/Catalina/localhost/ROOT.template.xml conf/Catalina/localhost/
RUN envsubst < conf/Catalina/localhost/ROOT.template.xml > conf/Catalina/localhost/ROOT.xml \
    && rm conf/Catalina/localhost/ROOT.template.xml

# Copy log4j2 configuration
COPY ./config/lib/log4j2.xml lib/log4j2.xml

# Create non-root user and set appropriate permissions for OpenOLAT runtime
RUN useradd -m -U -d /home/openolat -s /bin/bash openolat \
    && chgrp -R 0 /usr/local/tomcat \
    && chmod -R g+rwX /usr/local/tomcat

# Switch to non-root user for security
USER openolat

EXPOSE 8080
CMD ["./bin/start.sh"]