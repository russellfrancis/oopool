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
$response = writeMessage($socket, "STATUS");
if ($response) {
    if ($response->action === "OK") {
        if ($response->parameters !== null) {
	        foreach ($response->parameters as $p) {
                if ($p->key === 'statistics') {
		            foreach ($p->value->officeInstanceStats as $s) {
                        echo "================================================\n";
                        echo "NAME                     : " . $s->name . "\n";
                        echo "    STATE                : " . $s->state . "\n";
    		            if ($s->state === 'IDLE') {
	                                echo "    IDLE SINCE           : " . $s->idleSince . "\n";
                        }
                        echo "    JOBS PROCESSED       : " . $s->jobsProcessed . "\n";
                        echo "    TOTAL JOBS PROCESSED : " . $s->totalJobsProcessed . "\n";
                    }
                }
		    }
        }
    } else {
        echo "FAILURE: " . var_export($response, true) . "\n";
    }
}
socket_close($socket);
exit (0);
