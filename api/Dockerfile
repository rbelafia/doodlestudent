FROM maven:3.8.1-jdk-11-slim

COPY target/tlcdemoApp-1.0.0-SNAPSHOT-runner.jar /usr/src/tlc/

EXPOSE 8127

ENTRYPOINT ["java","-jar","/usr/src/tlc/tlcdemoApp-1.0.0-SNAPSHOT-runner.jar"]
