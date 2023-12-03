<?php
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $jsonData = file_get_contents("php://input");
    $requestData = json_decode($jsonData, true);
    
    if(isset($requestData['name']) && is_numeric($requestData['name'])) {
        
            foreach ($wifeData as $star) {
                if(intval($star['s']) == $requestData['name']){
            echo json_encode($star);
            return;
                }
            }
        
            http_response_code(404);
            echo json_encode(['message' => 'User not found']);
    } else {
        http_response_code(400);
        echo json_encode(['message' => 'Invalid index parameter']);
    }
} else {
    header('HTTP/1.1 405 Method Not Allowed');
    echo 'Method Not Allowed';
}
?>