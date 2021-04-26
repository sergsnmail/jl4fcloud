package db.user;

import db.DbStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class UserDataSourceImpl implements UserDataSource {

    private final DbStorage userDbStorage;

    private final String CREATE_USER_TABLE_SQL = "CREATE TABLE IF NOT EXISTS 'user' ('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
            " 'created_at' TEXT," +
            " 'updated_at' TEXT," +
            " 'username' TEXT," +
            " 'email' TEXT," +
            " 'hashed_password' TEXT," +
            " UNIQUE(username));";
    private final String SELECT_USER_SQL = "SELECT * FROM user WHERE username = ?";
    private final String UPDATE_USER_SQL = "UPDATE user SET updated_at = ?, email = ?, hashed_password = ? WHERE username = ?";
    private final String INSERT_USER_SQL = "INSERT INTO user (created_at, updated_at, username, email, hashed_password) VALUES (?, ?, ?, ?, ?)";
    private final String DELETE_USER_SQL = "DELETE FROM user WHERE username = ?";

    public UserDataSourceImpl(DbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
        initStorage();
    }

    @Override
    public User getUser(String username) {
        if (username == null || username.equals("")){
            throw new IllegalArgumentException();
        }
        User user = null;
        try(Connection connection = userDbStorage.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_SQL)){

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                user = User.builder()
                        .username(resultSet.getString("username"))
                        .email(resultSet.getString("email"))
                        .hashedPass(resultSet.getString("hashed_password"))
                        .build();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null){
            throw new IllegalArgumentException();
        }
        try (Connection conn = userDbStorage.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_USER_SQL)) {
            preparedStatement.setString(1, getDateTime());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getHashedPass());
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createUser(User user) {
        if (user == null){
            throw new IllegalArgumentException();
        }
        try(Connection conn = userDbStorage.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_USER_SQL)){
            String currTime = getDateTime();
            preparedStatement.setString(1, currTime);
            preparedStatement.setString(2, currTime);
            preparedStatement.setString(3, user.getUsername());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setString(5, user.getHashedPass());
            preparedStatement.executeUpdate();
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteUser(User user) {
        try(Connection conn = userDbStorage.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(DELETE_USER_SQL)){
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.executeUpdate();
            return true;
        }catch (Exception e){
            return false;
        }

    }

    protected void initStorage() {
        try (Connection conn = userDbStorage.getConnection();
             Statement statement = conn.createStatement()){
            statement.execute(CREATE_USER_TABLE_SQL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(System.currentTimeMillis());
    }
}
