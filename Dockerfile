FROM openjdk:16-jdk

VOLUME /tmp
ADD /build/libs/*.jar /toggl-to-jira.jar

ENV SPRING_PROFILES_ACTIVE=production

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/toggl-to-jira.jar"]
