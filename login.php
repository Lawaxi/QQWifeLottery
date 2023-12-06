<?php
require_once('handler/db.php');
require_once('handler/PasswordHandler.php');

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!isset($_POST['uid']) || !isset($_POST['password'])) {
        echo json_encode(['status' => 0, 'message' => '用户名或密码为空']);
        exit();
    }
    
    $uid = $_POST['uid'];
    $password = $_POST['password'];
    
    if (!userExists($uid)) {
        echo json_encode(['status' => 2, 'message' => '用户不存在']);
        exit();
    }
    
    if (checkPasswordIsNull($uid)) {
        echo json_encode(['status' => 2, 'message' => '未设置密码']);
        exit();
    }
    
    $userId = authenticateUser($uid, $password);

    if ($userId !== -1) {
        session_start();
        $_SESSION['userId'] = $userId;
        echo json_encode(['status' => 1, 'message' => '登陆成功']);
    } else {
        echo json_encode(['status' => 0, 'message' => '密码错误，登陆失败']);
    }
    
    exit();
}

function userExists($uid) {
    $pdo = connectDatabase();

    $stmt = $pdo->prepare("SELECT COUNT(*) as count FROM users WHERE id = :uid");
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    return $row['count'] > 0;
}

function checkPasswordIsNull($uid) {
    $pdo = connectDatabase();

    $stmt = $pdo->prepare("SELECT password FROM users WHERE id = :uid");
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    return ($row['password'] === null);
}

function authenticateUser($uid, $password) {
    $pdo = connectDatabase();

    $stmt = $pdo->prepare("SELECT id, password FROM users WHERE id = :uid");
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    $passwordHandler = new PasswordHandler();
    if ($row && $passwordHandler->verifyPassword($password, $row['password'])) {
        return $row['id'];
    } else {
        return -1;
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="styles.css">
    <style>
    #tip {
        text-align: center;
        font-style: italic;
        color: #4caf50;
    }
    #error {
        margin-top: 16px;
    }
    </style>
    <title>登录</title>
</head>
<body>
    <div class="container">
        <div class="logo-container">
            <img src="source/logo.png" alt="Logo">
        </div>
        <div class="box box-thin">
            <h1>登录</h1>
            <p>未设置密码用户请私信机器人发送（不需要斜杠）：</p>
            <p id="tip">修改密码 uid 密码</p>
            
            <form id="loginForm" onsubmit="return login()">
                <label for="uid">UID:</label>
                <input type="text" id="uid" name="uid" required><br>

                <label for="password">密码:</label>
                <input type="password" id="password" name="password" required><br>

                <button type="submit">登录</button>
            </form>

            <p id="error" style="color: red;"></p>
        </div>
        <div class="copyright">
            &copy; 2023 Wife Lottery<img src="ylg48.png" >
        </div>
    </div>

<script>
    function login() {
        var uid = document.getElementById('uid').value.replace(/^0+/, '');
        var password = document.getElementById('password').value;
        
        if (uid !== '' && password !== '') {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'login.php', true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4 && xhr.status == 200) {
                    var response = JSON.parse(xhr.responseText);
                    if (response.status === 1) {
                        window.location.href = 'index.php';
                    } else if (response.status === 0) {
                        alert(response.message);
                    } else if (response.status === 2) {
                        document.getElementById('error').textContent = response.message;
                    }
                }
            };
            xhr.send('uid=' + uid + '&password=' + password);
        } else {
            alert('请填写uid和密码');
        }
        return false;
    }
</script>

</body>
</html>