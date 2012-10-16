#!/bin/sh
if [ -z "$1" ]; then
  echo usage: $0 branchName
  echo  - transfers whole tree to a particular branch
  exit
fi
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1

