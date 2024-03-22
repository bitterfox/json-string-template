#!/bin/bash

majorminor=`grep 'version = ' ./build.gradle.kts | sed -r 's/.*version = "(.*)\.(.*)"/\1/'`
update=`grep 'version = ' ./build.gradle.kts | sed -r 's/.*version = "(.*)\.(.*)"/\2/'`

old_update=$((update - 1))
new_update=$((update + 1))

echo "Update build.gradle.kts for ${majorminor}.${update} -> ${majorminor}.${new_update}-SNAPSHOT"

sed -i -r "s/(.*version.*)${majorminor}\.${update}(.*)/\1${majorminor}.${new_update}-SNAPSHOT\2/" ./build.gradle.kts

echo "Update README.md for ${majorminor}.${old_update} -> ${majorminor}.${update}"

sed -i -r "s/(.*io\.github\.bitterfox:json-string-template.*)${majorminor}\.${old_update}(.*)/\1${majorminor}.${update}\2/" ./README.md

git add ./build.gradle.kts ./README.md

git commit -m "Bump ${majorminor}.${new_update}-SNAPSHOT"
