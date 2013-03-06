#!/bin/bash
#
# This is a useful script for exploding the .RPM files distributed with
# libre office you can then package up the created opt/libreoffice4
# directory for use with the deployment scripts as part of oopool.
#
FILES=`ls *.rpm`
for FILE in $FILES; do
    rpm2cpio "$FILE" | cpio -idmv
done
