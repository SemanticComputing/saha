#!/bin/sh

TARGETDIR="/common/group/finnonto/saha3/"

if [ "$TDBROOT" = "" ]
then
    echo "TDBROOT is not set" 1>&2
    exit 1
fi

for dir in $(find "$TARGETDIR" -type d); do
    
    if [ $dir = "$TARGETDIR" ]
    then
        continue;
    fi

    echo -n "Optimizing directory: "
    echo $dir

    $TDBROOT/bin/tdbconfig stats --loc "$dir" > "$dir"/stats.opt
done

echo "Done."
