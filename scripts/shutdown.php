<?php
$host = "localhost";
$port = 8099;

if (!($socket = adminConnect($host, $port))) {
    echo "ERROR: Unable to establish a connection to $host:$port.\n";
    exit(1);
}
writeMessage($socket, "SHUTDOWN");
socket_close($socket);
exit (0);

function adminConnect($host, $port) {
    if ($socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) {
        if (socket_connect($socket, $host, $port)) {
            return $socket;
        }
    }
    return false;
}

function writeMessage($socket, $action, $parameters = null) {
    echo "REQUEST : $action\n";
    $msg = new stdClass();
    $msg->action = $action;
    if (null === $parameters) {
        $msg->parameters = $parameters;
    }
    $payload = json_encode($msg);
    $len = strlen($payload);
    $message = pack("N", $len) . $payload;
    socket_write($socket, $message, strlen($message));

    $response = readMessage($socket);
    if (isset($response) && isset($response->action)) {
        echo "RESPONSE: {$response->action}\n";
        if ($response->action === "OK") {
            return true;
        }
        echo "FAILURE: " . var_export($response, true) . "\n";
    }
    return false;
}

function readMessage($socket) {
    $len = unpack("N", socket_read($socket, 4));
    $response = socket_read($socket, $len[1]);
    return json_decode($response);
}