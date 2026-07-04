FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends bash curl git procps inotify-tools \
    && rm -rf /var/lib/apt/lists/*

ENV GRADLE_USER_HOME=/tmp/gradle
ENV LANG=C.UTF-8

WORKDIR /workspace
