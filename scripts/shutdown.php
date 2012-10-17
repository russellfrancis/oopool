#!/usr/bin/env php
<?php
require_once('util.php');

$properties = parse_properties(__DIR__ . "/../conf/config.properties");
$host = "localhost";
$port = $properties['oopool.admin_port'];

if (!($socket = adminConnect($host, $port))) {
    echo "ERROR: Unable to establish a connection to $host:$port.\n";
    exit(1);
}
writeMessage($socket, "SHUTDOWN");
socket_close($socket);
exit (0);
