<?php
require_once('handler/DBHandler.php');

session_start();

if (!isset($_SESSION['userId'])) {
    header("Location: login.php");
    exit();
}

$userId = $_SESSION['userId'];

// Get the latest 20 coin log entries for the user
$coinLogEntries = getCoinLogEntries($userId);

?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>我的原石</title>
    <link rel="stylesheet" href="styles.css">
    <style>
        #comment{
    min-width: 100px;
        }
    </style>
</head>
<body>

    <div class="container">
        <div class="logo-container">
            <img src="source/logo.png" alt="Logo">
        </div>
        <div class="box">
        <h1>我的原石</h1>
<p>如何获取？</p>
        <li>每日首签</li>
        <li>抽奖情愫80及以上（成功带走）</li>
        <li>抽中许愿对象</li>
        <li>抽中当天过生日的成员</li>
<p>显示最近的20条数据</p>
<table>
    <thead>
        <tr>
            <th id="code">标码</th>
            <th id="amount">数量</th>
            <th id="comment">备注</th>
            <th id="time">时间</th>
        </tr>
    </thead>
    <tbody>
        <?php foreach ($coinLogEntries as $entry): ?>
            <tr>
                <td><?php echo $entry['reason_category']; ?></td>
                <td><?php echo $entry['amount']; ?></td>
                <td><?php echo $entry['reason_details']; ?></td>
                <td><?php echo date('Y-m-d H:i:s', strtotime($entry['timestamp'])); ?></td>
            </tr>
        <?php endforeach; ?>
    </tbody>
</table>

            <form method="post" action="index.php">
                <button type="submit">回到主页</button>
            </form>
        </div>
        <div class="copyright">
            &copy; 2023 Wife Lottery<img src="ylg48.png" >
        </div>
</body>
</html>

<?php
// Function to get the latest 20 coin log entries for the user
function getCoinLogEntries($userId) {
    $pdo = connectDatabase();

    try {
        $stmt = $pdo->prepare("SELECT reason_category, amount, reason_details, timestamp FROM coin_log WHERE user_id = :userId ORDER BY timestamp DESC LIMIT 20");
        $stmt->bindParam(':userId', $userId, PDO::PARAM_INT);
        $stmt->execute();

        $coinLogEntries = $stmt->fetchAll(PDO::FETCH_ASSOC);

        return $coinLogEntries;
    } catch (PDOException $e) {
        die("Error fetching coin log entries: " . $e->getMessage());
    }
}
?>
