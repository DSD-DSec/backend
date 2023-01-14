FROM amazoncorretto:17-alpine-jdk as build

RUN apk update && apk add dos2unix

WORKDIR /workspace/app

COPY .mvn .mvn

COPY pom.xml mvnw ./

COPY src src

RUN find . -type f -print0 | xargs -0 dos2unix

RUN ./mvnw package -DskipTests=true -Pprod -Dspring.profiles.active=prod

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM amazoncorretto:17-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.dsec.backend.BackendApplication"]