#!/usr/bin/env php
<?php
require_once('util.php');
global $argv;

$properties = parse_properties(__DIR__ . "/../conf/config.properties");
$libreofficeBaseDir = $properties['libreoffice.base.dir'];
$python = $libreofficeBaseDir . '/program/python';
exec($python . ' ' . escapeshellarg(__DIR__ . '/DocumentConverter.py') . ' ' . escapeshellarg($argv[1]) . ' ' . escapeshellarg($argv[2]));