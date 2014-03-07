#!/bin/bash

if [ $# -eq 0 ] 
    then echo "Need gitinit.sh 'progect name'"
    exit 1
fi
echo "Init git repository."
git init
echo "Make .gitignore file"
echo ".idea/*" > .gitignore
echo "Adding all files."
git add .
echo "Initial commit."
git commit -m "init commit"
