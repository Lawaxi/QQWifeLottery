<?php
function parseIniFile($contents) {
    $data = [];

    $lines = explode("\n", $contents);

    foreach ($lines as $line) {
        $line = trim($line);

        if (empty($line)) {
            continue;
        }

        if ($line[0] == '[' && substr($line, -1) == ']') {
            $section = substr($line, 1, -1);
            $data[$section] = [];
        } else {
            list($key, $value) = explode('=', $line, 2);
            $data[$section][trim($key)] = $section == 'users' || 'wives' ? json_decode(trim($value),true) : trim($value);
        }
    }

    return $data;
}

$configContents = file_get_contents('config.json');
$config = json_decode($configContents, true);

// dbFile
$dbContents = file_get_contents(__DIR__ . $config['dbFilePath']);
$data = parseIniFile($dbContents);

//wifeDbFile
$wifeDbContents = file_get_contents(__DIR__ . $config['wifeDbFilePath']);
$wifeData = json_decode($wifeDbContents, true);
