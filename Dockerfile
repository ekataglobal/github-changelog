FROM adzerk/boot-clj:latest as builder

ENV BOOT_VERSION=2.7.2
ENV BOOT_CLOJURE_VERSION=1.8.0

WORKDIR /usr/local/github-changelog

COPY . .
RUN cd /usr/local/github-changelog && boot uberjar

RUN /bin/bash -c 'source /usr/local/github-changelog/version.properties && cp /usr/local/github-changelog/target/github-changelog-$VERSION.jar /usr/local/github-changelog/github-changelog.jar'

FROM openjdk:jre-alpine

WORKDIR /usr/local/github-changelog

COPY --from=builder /usr/local/github-changelog/github-changelog.jar .
