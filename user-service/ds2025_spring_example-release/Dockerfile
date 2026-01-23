FROM maven:3.9 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

COPY ./demo/pom.xml /demo/pom.xml
WORKDIR /demo
RUN mvn dependency:go-offline
COPY ./demo/src/ /demo/src
WORKDIR /demo
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /demo
COPY --from=build /demo/target/demo-0.0.1-SNAPSHOT.jar /demo/demo.jar
EXPOSE 8080
CMD ["java", "-jar", "demo.jar"]
