package storage;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Db {
    private static final Properties PROPS = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("db.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("db.properties পড়া যায়নি (প্রজেক্টের মূল ফোল্ডারে আছে তো?)", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }
}