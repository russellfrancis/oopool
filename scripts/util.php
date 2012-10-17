<?php
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
        return $response;
    }
    return false;
}

function readMessage($socket) {
    $len = unpack("N", socket_read($socket, 4));
    $response = socket_read($socket, $len[1]);
    return json_decode($response);
}

function parse_properties($confFile) {
    $result = array();
    $txtProperties = file_get_contents($confFile);

    $lines = explode("\n", $txtProperties);
    $key = "";

    $isWaitingOtherLine = false;
    foreach($lines as $i=>$line) {
        if(empty($line) || (!$isWaitingOtherLine && strpos($line,"#") === 0))
            continue;
        if(!$isWaitingOtherLine) {
            $key = substr($line,0,strpos($line,'='));
            $value = substr($line,strpos($line,'=') + 1, strlen($line));
        }
        else {
            $value .= $line;
        }

        /* Check if ends with single '\' */
        if(strrpos($value,"\\") === strlen($value)-strlen("\\")) {
            $value = substr($value, 0, strlen($value)-1)."\n";
            $isWaitingOtherLine = true;
        }
        else {
            $isWaitingOtherLine = false;
        }

        $result[$key] = $value;
        unset($lines[$i]);
    }

    return $result;
}
