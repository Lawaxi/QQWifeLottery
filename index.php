<!-- index.php -->
<?php
require_once('handler/DBHandler.php');
session_start();

if (!isset($_SESSION['userId'])) {
    header("Location: login.php");
    exit();
}

$userId = $_SESSION['userId'];
$userDetails = getUserDetails($userId);
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Page</title>
    <link rel="stylesheet" href="styles.css">
    <style>
        #coinDisplay{
    display: flex;
    justify-content: space-between;
    align-items: center;
        }
        #coinDisplay p{
            margin: 0;
        }
        .coin-log-link {
    display: inline-block;
    position: relative;
    cursor: pointer;
}

.coin-log-link::after {
    content: "?";
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 14px;
    color: #888;
    border-radius: 50px;
    border: 1px solid gray;
    padding: 0 4px 0 4px;
}

    </style>
</head>
<body>

    <div class="container">
        <div class="logo-container">
            <img src="source/logo.png" alt="Logo">
        </div>
        <div class="box">
            <h1>Welcome!</h1>
            
            <p>UID: <?php echo $userId; ?></p>
            <p>QQ号: <?php echo $userDetails['user_number']; ?></p>
            <p>群号: <?php echo $userDetails['group_number']; ?></p>
            <div id="coinDisplay"><p><img class="mini-icon" src="source/Item_Primogem.webp">原石: <?php echo $userDetails['coins']; ?></p><div class="coin-log-link"  onclick="goToCoinLog()" ></div></div>
            <p><img class="mini-icon" src="source/Item_Intertwined_Fate.webp">纠缠之缘: <?php echo $userDetails['lottery_entries']; ?></p>
            <p>纠缠之缘即额外的抽奖次数，也可通过口袋送礼物获得。</p>
            <hr>
            <h2>商店</h2>
            <div class="shop-container">
                <div id="lotteryEntry" class="shop-item" onclick="buyItem(101,1)">
                    <img class ="shop-item-img" src="source/Item_Intertwined_Fate.webp">
                    <div class ="shop-item-info"><img class="mini-icon" src="source/Item_Primogem.webp">×16</div>
                </div>
            </div>
            
            <form method="post" action="logout.php">
                <button type="submit">退出登陆</button>
            </form>
                <a href="http://call.lawaxi.net:233/">一周目报告查询入口</a>
        </div>
        <div class="copyright">
            &copy; 2023 Wife Lottery<img src="ylg48.png" >
        </div>
    </div>

<script>

function buyItem(id, amount) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "api/buyItem.php", true);
    xhr.setRequestHeader("Content-Type", "application/json");

    var itemToBuy = {
        "item_id": id,
        "amount": amount
    };

    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            var response = JSON.parse(xhr.responseText);

            if (response.success) {
                alert("购买成功");
                location.reload();
            } else {
                alert("购买失败: " + response.message);
            }
        }
    };

    var jsonItemToBuy = JSON.stringify(itemToBuy);
    xhr.send(jsonItemToBuy);
}

function goToCoinLog() {
    window.location.href = 'coin_log.php';
}
</script>

</body>
</html>
