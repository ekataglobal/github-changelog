#!/bin/bash

export `tail -1 version.properties`

docker build -t whitepages/github-changelog:${VERSION} -t whitepages/github-changelog:latest --build-arg VERSION=${VERSION} .

docker push whitepages/github-changelog:${VERSION}
docker push whitepages/github-changelog:latest
