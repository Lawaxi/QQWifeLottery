<?php
require_once('handler/DBHandler.php');

session_start();

if (!isset($_SESSION['userId'])) {
    echo json_encode(['success' => false, 'message' => 'User not logged in']);
    exit();
}

$userId = $_SESSION['userId'];

$inputJSON = file_get_contents('php://input');
$inputData = json_decode($inputJSON, true);

if ($inputData === null) {
    echo json_encode(['success' => false, 'message' => 'Invalid JSON data']);
    exit();
}

if (!isset($inputData['item_id']) || !isset($inputData['amount'])) {
    echo json_encode(['success' => false, 'message' => 'Missing item information']);
    exit();
}

$itemId = $inputData['item_id'];
$amount = $inputData['amount'];

$result = buyItem($userId, $itemId, $amount);

echo json_encode($result);

function buyItem($userId, $itemId, $amount) {
    $pdo = connectDatabase();

    try {
        
        $userDetails = getUserDetails($userId);
        $cost; $log;
        
        switch ($itemId) {
            case 101:
                $cost = $amount * 16;
                $log = '购买抽奖次数';
                break;
            default:
                return ['success' => false, 'message' => '无效商品id'];
        }
        
        if ($userDetails['coins'] < $cost) {
            return ['success' => false, 'message' => '原石不足'];
        }

        $pdo->beginTransaction();

        $stmt = $pdo->prepare("UPDATE users SET coins = coins - :cost, lottery_entries = lottery_entries + :amount WHERE id = :userId");
        $stmt->bindParam(':cost', $cost, PDO::PARAM_INT);
        $stmt->bindParam(':amount', $amount, PDO::PARAM_INT);
        $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
        $stmt->execute();
        
        addCoinLog($pdo, $userId, -$cost, $itemId, $log);
        
        $pdo->commit();

        return ['success' => true];
        
    } catch (PDOException $e) {
        $pdo->rollBack();
        return ['success' => false, 'message' => '出错: ' . $e->getMessage()];
    }
}

function addCoinLog($pdo, $userId, $amount, $reasonCategory, $reasonDetails) {
    $stmt = $pdo->prepare("INSERT INTO coin_log (user_id, amount, reason_category, reason_details) VALUES (:userId, :amount, :reasonCategory, :reasonDetails)");
    $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
    $stmt->bindParam(':amount', $amount, PDO::PARAM_INT);
    $stmt->bindParam(':reasonCategory', $reasonCategory, PDO::PARAM_STR);
    $stmt->bindParam(':reasonDetails', $reasonDetails, PDO::PARAM_STR);
    $stmt->execute();
}
?>
