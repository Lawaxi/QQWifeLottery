<?php
require_once(__DIR__ . '/db.php');

function getUserDetails($userId) {
    $pdo = connectDatabase();

    try {
        $stmt = $pdo->prepare("SELECT user_number, group_number, coins, lottery_entries FROM users WHERE id = :userId");
        $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
        $stmt->execute();

        $userDetails = $stmt->fetch(PDO::FETCH_ASSOC);

        return $userDetails;
    } catch (PDOException $e) {
        die("Error fetching user details: " . $e->getMessage());
    }
}
?>