<?php

class PasswordHandler {
    public function hashPassword($plainPassword) {
        return password_hash($plainPassword, PASSWORD_BCRYPT);
    }

    public function verifyPassword($plainPassword, $hashedPassword) {
        return password_verify($plainPassword, $hashedPassword);
    }
}

?>
