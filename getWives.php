<?php
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $jsonData = file_get_contents("php://input");
    $requestData = json_decode($jsonData, true);
    
    if(isset($requestData['index']) && is_numeric($requestData['index'])) {
        $index = intval($requestData['index']);
    
        if (isset($data['users']['wives'][$index])) {
            $user = $data['users']['wives'][$index];
            echo json_encode($user);
        } else {
            http_response_code(404);
            echo json_encode(['message' => 'User not found']);
        }
    } else {
        http_response_code(400);
        echo json_encode(['message' => 'Invalid index parameter']);
    }
} else {
    header('HTTP/1.1 405 Method Not Allowed');
    echo 'Method Not Allowed';
}
?>
