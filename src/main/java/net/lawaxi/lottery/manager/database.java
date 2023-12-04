package net.lawaxi.lottery.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class database {
    private static final String TABLE_NAME = "logs";
    private static final String USERS_TABLE_NAME = "users";
    private static final String WISH_TABLE_NAME = "wish";
    private static final String COIN_LOG_TABLE_NAME = "coin_log";
    private final File databaseFile;
    private final Connection connection;

    public database(File database) {
        databaseFile = database;
        if (!databaseFile.exists()) {
            FileUtil.touch(databaseFile);
        }

        connection = initConnection();
        initDatabase();
        initUsersTable();
        initWishTable();
        initCoinLogTable();

        //版本更新 0.2.0-test5
        if (!isColumnExists(TABLE_NAME, "wish")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN wish BOOLEAN DEFAULT FALSE")) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //版本更新 0.2.0-test8
        if (!isColumnExists(USERS_TABLE_NAME, "coins")) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN coins INTEGER DEFAULT 0")) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private Connection initConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error initializing database connection.", e);
        }
    }

    private void initDatabase() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "group_number BIGINT," +
                    "user_id INTEGER," +
                    "wife_id INTEGER," +
                    "wife_name TEXT," +
                    "sense INTEGER," +
                    "lottery_time TIMETAMP DEFAULT CURRENT_TIMESTAMP," +
                    "wish BOOLEAN DEFAULT FALSE" +
                    ")";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing logs table.", e);
        }
    }

    private void initUsersTable() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "group_number BIGINT," +
                    "user_number BIGINT," +
                    "coins INTEGER DEFAULT 0" +
                    ")";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing users table.", e);
        }
    }

    private void initWishTable() {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + WISH_TABLE_NAME + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "wish_target TEXT," +
                        "wish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "wish_status INTEGER," +
                        "remaining_count INTEGER" +
                        ")")) {

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing wish table.", e);
        }
    }

    private void initCoinLogTable() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + COIN_LOG_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "amount INTEGER," +
                    "reason_category INTEGER," +
                    "reason_details TEXT," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing coin log table.", e);
        }
    }

    private boolean isTableExists(String tableName) {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'");
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isColumnExists(String tableName, String columnName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "PRAGMA table_info(" + tableName + ")")) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String existingColumnName = resultSet.getString("name");
                    if (columnName.equals(existingColumnName)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void appendLotteryRecord(long groupNumber, int userId, int wifeId, String wifeName, int sense, boolean wish) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (group_number, user_id, wife_id, wife_name, sense, wish) VALUES (?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setLong(1, groupNumber);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, wifeId);
            preparedStatement.setString(4, wifeName);
            preparedStatement.setInt(5, sense);
            preparedStatement.setBoolean(6, wish);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getRecordByIndex(int index) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?")) {

            preparedStatement.setInt(1, index);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return constructJSONObject(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getMaxSenseRecord(int wifeId, long groupNumber) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE wife_id = ? AND group_number = ?" +
                        " ORDER BY sense DESC, lottery_time DESC LIMIT 1")) {

            preparedStatement.setInt(1, wifeId);
            preparedStatement.setLong(2, groupNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return constructJSONObject(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; //未被抽到过
    }

    public JSONObject[] getAllRecordsByUserId(int userId) {
        List<JSONObject> resultList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ?")) {

            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    resultList.add(constructJSONObject(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList.toArray(new JSONObject[0]);
    }

    public JSONObject[] getAllRecordsThatMaxSense(int userId, long groupNumber) {
        List<JSONObject> resultList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE group_number = ? AND user_id = ?" +
                        " AND NOT EXISTS (" +
                        "   SELECT 1 FROM " + TABLE_NAME + " t2" +
                        "   WHERE t2.group_number = ?" +
                        "     AND t2.wife_id = " + TABLE_NAME + ".wife_id" +
                        "     AND t2.sense > " + TABLE_NAME + ".sense" +
                        "     AND t2.user_id = ?" +
                        ")")) {

            preparedStatement.setLong(1, groupNumber);
            preparedStatement.setInt(2, userId);
            preparedStatement.setLong(3, groupNumber);
            preparedStatement.setInt(4, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    resultList.add(constructJSONObject(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList.toArray(new JSONObject[0]);
    }

    private JSONObject[] constructJSONObjects(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.last();
            int rowCount = resultSet.getRow();
            resultSet.beforeFirst();

            JSONObject[] result = new JSONObject[rowCount];
            int i = 0;

            while (resultSet.next()) {
                result[i++] = constructJSONObject(resultSet);
            }

            return result;
        }
        return new JSONObject[0];
    }

    private JSONObject constructJSONObject(ResultSet resultSet) throws SQLException {
        return new JSONObject()
                .put("id", resultSet.getInt("id"))
                .put("group_number", resultSet.getLong("group_number"))
                .put("user_id", resultSet.getInt("user_id"))
                .put("wife_id", resultSet.getInt("wife_id"))
                .put("wife_name", resultSet.getString("wife_name"))
                .put("sense", resultSet.getInt("sense"))
                .put("lottery_time", resultSet.getLong("lottery_time"));
    }


    public int getUserIdByNumbers(long groupNumber, long userNumber) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id FROM " + USERS_TABLE_NAME + " WHERE group_number = ? AND user_number = ?")) {

            preparedStatement.setLong(1, groupNumber);
            preparedStatement.setLong(2, userNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                } else {
                    // If no matching entry found, insert a new one
                    return insertUser(groupNumber, userNumber);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting user id by numbers.", e);
        }
    }

    private int insertUser(long groupNumber, long userNumber) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + USERS_TABLE_NAME + " (group_number, user_number) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setLong(1, groupNumber);
            preparedStatement.setLong(2, userNumber);
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated ID
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user.", e);
        }
    }

    public JSONObject getUserDetailsById(int userId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT group_number, user_number FROM " + USERS_TABLE_NAME + " WHERE id = ?")) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long groupNumber = resultSet.getLong("group_number");
                    long userNumber = resultSet.getLong("user_number");

                    return new JSONObject()
                            .put("group_number", groupNumber)
                            .put("user_number", userNumber);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject[] analyseGroupRecords(long groupNumber) {
        List<JSONObject> analysisResults = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT t1.user_id, t2.user_number, COUNT(*) AS count " +
                        "FROM " + TABLE_NAME + " t1 " +
                        "JOIN " + USERS_TABLE_NAME + " t2 ON t1.user_id = t2.id " +
                        "WHERE t1.group_number = ? AND t1.sense >= 80 " +
                        "GROUP BY t1.user_id " +
                        "ORDER BY count DESC")) {

            preparedStatement.setLong(1, groupNumber);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    long userNumber = resultSet.getLong("user_number");
                    int count = resultSet.getInt("count");

                    JSONObject analysisResult = new JSONObject()
                            .put("user_id", userId)
                            .put("user_number", userNumber)
                            .put("count", count);

                    analysisResults.add(analysisResult);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return analysisResults.toArray(new JSONObject[0]);
    }

    public int getCoins(int userId) {
        // 获取金币数
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT coins FROM " + USERS_TABLE_NAME + " WHERE id = ?")) {

            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("coins");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void addCoins(int userId, int amount, int reasonCategory, String reasonDetails) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE " + USERS_TABLE_NAME + " SET coins = coins + ? WHERE id = ?")) {

            preparedStatement.setInt(1, amount);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            logCoinChange(userId, amount, reasonCategory, reasonDetails);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean spendCoins(int userId, int amount, int reasonCategory, String reasonDetails) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE " + USERS_TABLE_NAME + " SET coins = coins - ? WHERE id = ? AND coins >= ?")) {

            preparedStatement.setInt(1, amount);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, amount);
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                logCoinChange(userId, -amount, reasonCategory, reasonDetails);
            }

            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int applyWish(int userId, String wishTarget, int count) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + WISH_TABLE_NAME + " (user_id, wish_target, wish_status, remaining_count) " +
                        "VALUES (?, ?, 2, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, wishTarget);
            preparedStatement.setInt(3, count);
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // 返回生成的ID
                } else {
                    throw new SQLException("Creating wish failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error applying wish.", e);
        }
    }

    public void reduceWishCount(int wishId) {
        // 减少许愿次数
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE " + WISH_TABLE_NAME + " SET remaining_count = remaining_count - 1 WHERE id = ?")) {

            preparedStatement.setInt(1, wishId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setWishStatus(int wishId, int wishStatus) {
        // 设置许愿状态
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE " + WISH_TABLE_NAME + " SET wish_status = ? WHERE id = ?")) {

            preparedStatement.setInt(1, wishStatus);
            preparedStatement.setInt(2, wishId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject[] getAllOngoingWishes() {
        // 获取全部正在进行的许愿
        List<JSONObject> ongoingWishes = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id, user_id, wish_target, remaining_count FROM " + WISH_TABLE_NAME +
                        " WHERE wish_status = 2")) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int wishId = resultSet.getInt("id");
                    int userId = resultSet.getInt("user_id");
                    String wishTarget = resultSet.getString("wish_target");
                    int remainingCount = resultSet.getInt("remaining_count");

                    JSONObject ongoingWish = new JSONObject()
                            .put("id", wishId)
                            .put("user_id", userId)
                            .put("wish_target", wishTarget)
                            .put("remaining_count", remainingCount);

                    ongoingWishes.add(ongoingWish);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ongoingWishes.toArray(new JSONObject[0]);
    }

    private void logCoinChange(int userId, int amount, int reasonCategory, String reasonDetails) {
        // 记录金币变动
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + COIN_LOG_TABLE_NAME + " (user_id, amount, reason_category, reason_details) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, amount);
            preparedStatement.setInt(3, reasonCategory);
            preparedStatement.setString(4, reasonDetails);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
