FROM adzerk/boot-clj:latest as builder

ENV BOOT_VERSION=2.7.2
ENV BOOT_CLOJURE_VERSION=1.8.0

RUN boot uber

FROM openjdk:jre-alpine

ARG VERSION

WORKDIR /usr/local/github-changelog

COPY target/github-changelog-$VERSION.jar ./github-changelog.jar
