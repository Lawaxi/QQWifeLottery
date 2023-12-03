<?php
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $jsonData = file_get_contents("php://input");
    $requestData = json_decode($jsonData, true);
    
    if(isset($requestData['index']) && is_numeric($requestData['index'])) {
        $index = intval($requestData['index']);
        $users = $data['users']['users'];
        $wivesData = $data['wives']['sense'];
    
        if (isset($users[$index])) {
            $user = $users[$index];
            $g = strval($user['g']);
            $m = strval($user['m']);
    
            $result = [];
    
            foreach ($wivesData as $key => $value) {
                if (isset($value[$g]) && in_array($m, $value[$g])) {
                    $result[] = ['sid' => $key, 'sense' => $value[$g][1]];
                }
            }
    
            echo json_encode($result);
        } else {
            http_response_code(404);
            echo json_encode(['message' => 'User not found']);
        }
    }
} else {
    header('HTTP/1.1 405 Method Not Allowed');
    echo 'Method Not Allowed';
}
?>