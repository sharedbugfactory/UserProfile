# ---- build ----
FROM gradle:8.9-jdk21 AS build
WORKDIR /app
COPY . .
# build the boot jar only, then remove any plain jar so exactly one file remains
RUN gradle clean bootJar -x test && \
    rm -f build/libs/*-plain.jar && \
    ls -l build/libs

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# now the wildcard matches exactly one file, so copying to a filename is valid
COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 80
ENTRYPOINT ["java","-jar","/app/app.jar"]