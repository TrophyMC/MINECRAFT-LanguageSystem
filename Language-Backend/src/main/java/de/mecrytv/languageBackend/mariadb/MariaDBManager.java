package de.mecrytv.languageBackend.mariadb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mecrytv.languageVelocity.LanguageVelocity;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDBManager {

    private HikariDataSource dataSource;

    public MariaDBManager() {
        String host = LanguageVelocity.getInstance().getServiceManager().getConfig().getString("mariadb.host");
        int port = LanguageVelocity.getInstance().getServiceManager().getConfig().getInt("mariadb.port");
        String database = LanguageVelocity.getInstance().getServiceManager().getConfig().getString("mariadb.database").trim();
        String username = LanguageVelocity.getInstance().getServiceManager().getConfig().getString("mariadb.user");
        String password = LanguageVelocity.getInstance().getServiceManager().getConfig().getString("mariadb.password");

        HikariConfig mariaDBConfig = new HikariConfig();

        mariaDBConfig.setUsername(username);
        mariaDBConfig.setPassword(password);

        mariaDBConfig.setConnectionTimeout(2000);
        mariaDBConfig.setMaximumPoolSize(10);
        mariaDBConfig.setDriverClassName("org.mariadb.jdbc.Driver");

        String jdbcURL = "jdbc:mariadb://" + host + ":" + port + "/" + database;
        mariaDBConfig.setJdbcUrl(jdbcURL);

        dataSource = new HikariDataSource(mariaDBConfig);

        try {
            Connection connection = getConnection();
            closeConnection(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("MySQL-Initialisierung fehlgeschlagen", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LanguageVelocity.getInstance().getLogger().warn("Error closing connection: " + e.getMessage());
        }
    }

    public void shutDown() {
        dataSource.close();
    }
}
