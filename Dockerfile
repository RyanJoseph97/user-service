FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /build
ARG GITHUB_TOKEN
# Write settings.xml for dependency resolution, download deps, then delete in the same
# layer so the token never persists in an image layer or shows up in build history.
COPY pom.xml .
RUN mkdir -p /root/.m2 && \
    printf '<settings><servers><server><id>github</id><username>token</username><password>%s</password></server></servers></settings>\n' \
        "$GITHUB_TOKEN" > /root/.m2/settings.xml && \
    mvn dependency:go-offline -q && \
    rm -f /root/.m2/settings.xml
# Source is copied after dep download so source-only changes skip the download layer
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
