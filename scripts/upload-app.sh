#!/bin/sh

git config --global user.name "Aditya Gupta"
git config --global user.email "adityaofficialgupta@gmail.com"

git clone https://adityastic:$GITHUB_KEY@github.com/adityastic/AndroidAppReleases
cd AndroidAppReleases

rm -rf ${TRAVIS_REPO_SLUG#*/}*

find ../app/build/outputs -type f -name '*.apk' -exec cp -v {} . \;

git checkout --orphan temporary

for file in app*; do
    if [[ $file != *"unsigned"* ]];then
        mv $file ${TRAVIS_REPO_SLUG#*/}-${file#*-}
        git add ${TRAVIS_REPO_SLUG#*/}-${file#*-}
        git commit -am "Update ${TRAVIS_REPO_SLUG#*/}-${file#*-} ($(date +%Y-%m-%d.%H:%M:%S))"
    fi
done

git branch -D master
git branch -m master

git push origin master --force
