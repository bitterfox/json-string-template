#!/bin/bash

set -eo pipefail

./gradlew clean build test

sed -i -r "s/(.*version.*)-SNAPSHOT(.*)/\1\2/" ./build.gradle.kts
git add ./build.gradle.kts
git commit -m "[RELEASE] Bump `grep 'version = ' build.gradle.kts | sed -r 's/.*version = "(.*)"/\1/'`"

./gradlew :json-string-template-core:publishToCentralPortal
./gradlew :json-string-template-jakarta-json:publishToCentralPortal
./gradlew :json-string-template-org-json:publishToCentralPortal
./gradlew :json-string-template-jackson:publishToCentralPortal

echo "Check https://central.sonatype.com/publishing"
