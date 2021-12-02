FROM openjdk:17-jdk

VOLUME /tmp
ADD /build/libs/*.jar /toggl-to-jira.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/toggl-to-jira.jar"]
