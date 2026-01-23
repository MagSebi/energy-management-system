FROM maven:3.9 AS build

ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV CATALINA_OPTS="-Duser.timezone=UTC"

# copy pom and download dependencies
COPY ./pom.xml /app/pom.xml
WORKDIR /app
RUN mvn dependency:go-offline -B

# copy sources and build
COPY ./src /app/src
WORKDIR /app
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/auth-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
