FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/platformappservice-0.0.1-SNAPSHOT.jar platform_app_service.jar
ENTRYPOINT ["java","-Xms512m","-Xmx512m","-Xmn256m","-Xss256k","-Duser.timezone=Asia/Shanghai","-Djava.security.egd=file:/dev/./urandom","-server","-jar","/platform_app_service.jar"]