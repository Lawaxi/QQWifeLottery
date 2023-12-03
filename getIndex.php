<?php
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $jsonData = file_get_contents("php://input");
    $requestData = json_decode($jsonData, true);
    
    if(isset($requestData['qid']) && is_numeric($requestData['qid'])) {
        $users = $data['users']['users'];
        $result = [];
        
        for ($i = 0; $i < count($users); $i++) {
            $user = $users[$i];
            
            if ($user['m'] == $requestData['qid']) {
                $result[] = $i;
            }
        }
        
        echo json_encode($result);

    } else {
        http_response_code(400);
        echo json_encode(['message' => 'Invalid qid parameter']);
    }
} else {
    header('HTTP/1.1 405 Method Not Allowed');
    echo 'Method Not Allowed';
}
?>