#!/bin/bash
#
# This is a useful script for exploding the .RPM files distributed with
# libre office you can then package up the created opt/libreoffice4
# directory for use with the deployment scripts as part of oopool.
#
# Also note that the libreoffice-java package also needs to be manually
# constructed from the necessary .jar files.  It should be noted that
# those files should have their Sealed: true status removed from the
# MANIFEST.MF file if such an entry exists in those jar files.
#
FILES=`ls *.rpm`
for FILE in $FILES; do
    rpm2cpio "$FILE" | cpio -idmv
done
