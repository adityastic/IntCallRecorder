#!/bin/sh

git config --global user.name "Aditya Gupta"
git config --global user.email "adityaofficialgupta@gmail.com"

git clone https://adityastic:$GITHUB_KEY@github.com/adityastic/AndroidAppReleases
cd AndroidAppReleases

rm -rf ${TRAVIS_REPO_SLUG:11}*

find ../app/build/outputs -type f -name '*.apk' -exec cp -v {} . \;

for file in app*; do
    if [[ $file != *"unsigned"* ]];then
        mv $file ${TRAVIS_REPO_SLUG:11}-${file:4}
        git add ${TRAVIS_REPO_SLUG:11}-${file:4}
        git commit -am "Update ${TRAVIS_REPO_SLUG:11}-${file:4} ($(date +%Y-%m-%d.%H:%M:%S))"
    fi
done

git push origin master