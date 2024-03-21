#!/bin/bash

./gradlew :json-string-template-core:publishToCentralPortal
./gradlew :json-string-template-jakarta-json:publishToCentralPortal
./gradlew :json-string-template-org-json:publishToCentralPortal
./gradlew :json-string-template-jackson:publishToCentralPortal

echo "Check https://central.sonatype.com/publishing"
