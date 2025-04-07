# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY scripts ./scripts
COPY src ./src
COPY .git .git
COPY NOTICE.TXT NOTICE.TXT
RUN mvn --no-transfer-progress clean package -DskipTests \
    && rm -rf /root/.m2/repository
RUN ls -la /build/target

# Runtime stage
FROM tomcat:10.1-jre17

# Prepare system packages
RUN apt-get update \
    && apt-get install -y --no-install-recommends gettext-base unzip \
    && apt-get purge -y && apt-get autoremove -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV LC_ALL=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

WORKDIR /usr/local/tomcat
RUN chmod -R 777 /usr/local/tomcat

# Copy the built WAR file from the builder stage and extract it into the webapp directory
COPY --from=builder /build/target/*.war webapp/ROOT.war
RUN unzip webapp/ROOT.war -d webapp && rm webapp/ROOT.war

# Copy setenv.sh configuration
COPY ./config/bin/setenv.sh bin/setenv.sh
RUN chmod +x bin/setenv.sh

# Copy Tomcat server.xml configuration
COPY ./config/conf/server.xml conf/server.xml

# Copy and prepare start script
COPY ./config/bin/start.sh bin/start.sh
RUN chmod +x bin/start.sh

# Create necessary OpenOlat directories
RUN mkdir -p lib conf/Catalina/localhost logs olatdata

# COPY olat.local.properties from template
COPY ./config/lib/olat.local.properties lib/olat.local.properties

# COPY ROOT.xml from template
COPY ./config/conf/Catalina/localhost/ROOT.xml conf/Catalina/localhost/

# Copy log4j2 configuration
COPY ./config/lib/log4j2.xml lib/log4j2.xml

# Create non-root user and set appropriate permissions for OpenOLAT runtime
RUN useradd -U -d /home/openolat -s /bin/bash openolat && mkdir -p /home/openolat && chown openolat:openolat /home/openolat
# Switch to non-root user for security
USER openolat

EXPOSE 8080
CMD ["./bin/start.sh"]