package db.file;

import db.DbStorage;
import db.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileDataSourceImpl implements FileDataSource {

    private final DbStorage fileDbStorage;

    private final String CREATE_USER_FILE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS 'user_file' ('id' INTEGER PRIMARY KEY AUTOINCREMENT," +
            " 'user_id' INTEGER," +
            " 'created_at' TEXT," +
            " 'modified_at' TEXT," +
            " 'file_name' TEXT," +
            " 'user_location' TEXT," +
            " 'storage' TEXT," +
            " FOREIGN KEY(user_id) REFERENCES user(id));";
    private final String SELECT_FILE_SQL = "SELECT * FROM user_file WHERE user_id = ?";
    private final String UPDATE_FILE_SQL = "UPDATE user_file SET modified_at = ? WHERE id = ?";
    private final String INSERT_FILE_SQL = "INSERT INTO user_file (user_id, created_at, modified_at, file_name, user_location, storage) VALUES (?, ?, ?, ?, ?, ?)";
    private final String DELETE_FILE_SQL = "DELETE FROM user_file WHERE id = ?";

    public FileDataSourceImpl(DbStorage fileDbStorage) {
        this.fileDbStorage = fileDbStorage;
        initStorage();
    }

    @Override
    public List<StorageFile> getFiles(User user) {
        if (user == null){
            throw new IllegalArgumentException();
        }
        List<StorageFile> result = null;
        try(Connection connection = fileDbStorage.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FILE_SQL)){

            preparedStatement.setInt(1, user.getUserId());
            ResultSet resultSet = preparedStatement.executeQuery();

            result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(StorageFile.builder()
                        .id(resultSet.getInt("id"))
                        .created_at(resultSet.getString("created_at"))
                        .modified_at(resultSet.getString("modified_at"))
                        .file_name(resultSet.getString("file_name"))
                        .user_location(resultSet.getString("user_location"))
                        .storage(resultSet.getString("storage"))
                        .build());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean updateFile(StorageFile updatedStorageFile) {
        if (updatedStorageFile == null){
            throw new IllegalArgumentException();
        }
        try (Connection conn = fileDbStorage.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(UPDATE_FILE_SQL)) {
            preparedStatement.setString(1, updatedStorageFile.getModified_at());
            preparedStatement.setInt(2, updatedStorageFile.getId());
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addFile(User user, StorageFile storageFile) {
        if (storageFile == null){
            throw new IllegalArgumentException();
        }
        try(Connection conn = fileDbStorage.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_FILE_SQL)){
            //String currTime = getDateTime();
            preparedStatement.setInt(1, user.getUserId());
            preparedStatement.setString(2, storageFile.getCreated_at());
            preparedStatement.setString(3, storageFile.getModified_at());
            preparedStatement.setString(4, storageFile.getFile_name());
            preparedStatement.setString(5, storageFile.getUser_location());
            preparedStatement.setString(6, storageFile.getStorage());
            preparedStatement.executeUpdate();
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteFile(StorageFile deletedStorageFile) {
        try(Connection conn = fileDbStorage.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(DELETE_FILE_SQL)){
            preparedStatement.setInt(1, deletedStorageFile.getId());
            preparedStatement.executeUpdate();
            return true;
        }catch (Exception e){
            return false;
        }

    }

    protected void initStorage() {
        try (Connection conn = fileDbStorage.getConnection();
             Statement statement = conn.createStatement()){
            statement.execute(CREATE_USER_FILE_TABLE_SQL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(System.currentTimeMillis());
    }
}
