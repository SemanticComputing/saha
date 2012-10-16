#!/bin/sh
if [ -z "$1" ]; then
  echo usage: $0 branchName
  echo  - transfers relevant parts of the tree to a particular branch, leaving the rest in trunk
  exit
fi
svn switch https://nipo.seco.hut.fi/svn/semweb/newtrunk
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/app app
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/test/app test/app
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/context/app context/app
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/context/WEB-INF/web.xml context/WEB-INF/web.xml
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/context/WEB-INF/lib context/WEB-INF/lib
svn switch https://nipo.seco.hut.fi/svn/semweb/branches/$1/context/WEB-INF/tags/app context/WEB-INF/tags/app
