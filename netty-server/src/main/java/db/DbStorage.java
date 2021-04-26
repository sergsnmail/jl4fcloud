package db;

import java.sql.*;

public class DbStorage {

    private final String DEFAULT_DATABASE = "storage.db";
    private String dbName;

    public DbStorage() {
        this.dbName = DEFAULT_DATABASE;
    }

    public DbStorage(String dbName) {
        if (dbName == null){
            this.dbName = DEFAULT_DATABASE;
        } else {
            this.dbName = dbName;
        }
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
    }

}
