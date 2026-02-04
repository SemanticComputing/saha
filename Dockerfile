FROM tomcat:9-jdk8-openjdk-slim

ENV PATH_WEBAPPS /usr/local/tomcat/webapps

RUN mkdir -p /usr/share/man/man1 # https://github.com/debuerreotype/docker-debian-artifacts/issues/24

RUN apt-get update \
 && apt-get install -y maven

WORKDIR /tmp/saha

COPY pom.xml .
COPY src/main src/main/

# Build project
RUN mvn install

# Copy built to tomcat hosting directory
RUN mkdir "$PATH_WEBAPPS/ssaha/"
RUN cp -r /tmp/saha/target/smetana/* "$PATH_WEBAPPS/ssaha/"
# Remove now-unnecessary files
RUN rm -rf /tmp/saha

ENV JAVA_OPTS -Dhttp.agent=Saha/1.0.0
