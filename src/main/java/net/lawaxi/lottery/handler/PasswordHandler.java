package net.lawaxi.lottery.handler;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONObject;
import net.lawaxi.lottery.manager.database;
import net.mamoe.mirai.contact.User;

public class PasswordHandler {
    private final String salt;
    private final net.lawaxi.lottery.manager.database database;

    public PasswordHandler(database database, int log_rounds) {
        this.database = database;
        this.salt = BCrypt.gensalt(log_rounds);
    }

    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, salt);
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public void testChangePassword(String m, User user) {
        try {
            String[] args = m.split(" ");
            if (args.length >= 3) {
                int id = Integer.valueOf(args[1]);
                String password = args[2];
                JSONObject u = database.getUserDetailsById(id);
                if (u == null) {
                    user.sendMessage("uid不存在");
                } else if (user.getId() != u.getLong("user_number")) {
                    user.sendMessage("该uid不属于您");
                } else if (database.changePassword(id, password)) {
                    user.sendMessage("设置成功");
                } else {
                    user.sendMessage("设置失败，请联系管理员");
                }
                return;
            }
        } catch (Exception e) {

        }
        user.sendMessage("格式错误：\n修改密码 uid 密码");
    }
}
