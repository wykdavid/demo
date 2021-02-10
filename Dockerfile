FROM java:8
VOLUME /tmp
ADD target/demo-0.0.1-SNAPSHOT.jar miaosha.jar
ENTRYPOINT ["java","-jar","/miaosha.jar","--spring.config.location=//var/config/application.properties"]