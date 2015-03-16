#!/usr/bin/env zsh
set -eux
./build.sh
rm -rf bin
rm -f .classpath .project
svn status
rm -rf **/.svn
