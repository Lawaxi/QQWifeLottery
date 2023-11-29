package net.lawaxi.lottery.manager;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class database {
    private static final String TABLE_NAME = "logs";
    private static final String USERS_TABLE_NAME = "users";
    private final File databaseFile;
    private final Connection connection;

    public database(File database) {
        databaseFile = database;
        if (!databaseFile.exists()) {
            FileUtil.touch(databaseFile);
        }

        connection = initConnection();
        if (!isTableExists(TABLE_NAME)) {
            initDatabase();
        }

        if (!isTableExists(USERS_TABLE_NAME)) {
            initUsersTable();
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
                    "lottery_time TIMETAMP" +
                    ")";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing logs table.", e);
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

    private void initUsersTable() {
        try (Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE_NAME + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "group_number BIGINT," +
                    "user_number BIGINT" +
                    ")";
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing users table.", e);
        }
    }

    public void appendLotteryRecord(long groupNumber, int userId, int wifeId, String wifeName, int sense) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO " + TABLE_NAME + " (group_number, user_id, wife_id, wife_name, sense, lottery_time) VALUES (?, ?, ?, ?, ?, ?)")) {

            preparedStatement.setLong(1, groupNumber);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, wifeId);
            preparedStatement.setString(4, wifeName);
            preparedStatement.setInt(5, sense);
            preparedStatement.setLong(6, new DateTime().getTime());

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

}
