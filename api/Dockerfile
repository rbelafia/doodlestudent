#
# Build stage
#
FROM maven:3.8.1-jdk-11-slim
RUN mkdir -p /app
WORKDIR /app
COPY pom.xml /app
COPY src /app/src
RUN mvn clean package -Dmaven.test.skip=true
EXPOSE 8127
ENTRYPOINT ["java","-jar","app/target/mytlcdemoapp-1.0.0-SNAPSHOT-runner.jar"]
