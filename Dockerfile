FROM openjdk:latest

ARG VERSION

WORKDIR /usr/local/github-changelog

RUN apt-get update
RUN apt-get install --assume-yes ca-certificates curl

COPY target/github-changelog-$VERSION.jar ./github-changelog.jar
