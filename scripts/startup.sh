#!/bin/bash
CWD=`dirname $0`
cd $CWD
CONFIG_FILE="$CWD/conf/config.properties"
JAVA_OPTS="-Doopool.config_file=$CONFIG_FILE"
JAR=`ls oopool-*.jar`
echo "STARTING OOPOOL WITH THE FOLLOWING OPTIONS FROM $CONFIG_FILE"
cat "$CONFIG_FILE"
echo ""
echo "    ./shutdown.php           -  To stop the service."
echo "    ./pool-status.php        -  To show the status of the running service."
echo "    tail -f logs/oopool.log  -  To view process output."
echo ""

java $JAVA_OPTS -jar "$JAR" "$CONFIG_FILE" 2>&1 > /dev/null &
echo "SUCCESSFULLY START PID=$!"
echo ""
