#!/bin/bash

git config --global user.name "Aditya Gupta"
git config --global user.email "adityaofficialgupta@gmail.com"

git clone https://adityastic:$PAT@github.com/adityastic/AndroidAppReleases
cd AndroidAppReleases

rm -rf ${GITHUB_REPOSITORY#*/}*

find ../app/build/outputs -type f -name '*.apk' -exec cp -v {} . \;

git checkout --orphan temporary

for file in app*; do
    if [[ $file != *"unsigned"* ]];then
        mv $file ${GITHUB_REPOSITORY#*/}-${file#*-}
        git add ${GITHUB_REPOSITORY#*/}-${file#*-}
        git commit -am "Update ${GITHUB_REPOSITORY#*/}-${file#*-} ($(date +%Y-%m-%d.%H:%M:%S))"
    fi
done

git branch -D master
git branch -m master

git push origin master --force
