package de.mecrytv.languageVelocity.models.mariadb;

import de.mecrytv.languageVelocity.cache.CacheNode;
import de.mecrytv.languageVelocity.models.redis.LanguageModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LanguageNode extends CacheNode<LanguageModel> {

    public LanguageNode() {
        super("languages", LanguageModel::new);
    }

    @Override
    public void createTableIfNotExists() {
        try (Connection conn = de.mecrytv.languageVelocity.LanguageVelocity.getInstance().getServiceManager().getMariaDBManager().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS network_languages (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "language VARCHAR(10) NOT NULL, " +
                    "first_join BOOLEAN DEFAULT TRUE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void saveToDatabase(Connection conn, LanguageModel model) throws SQLException {
        String query = "INSERT INTO network_languages (uuid, language, first_join) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE language = ?, first_join = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, model.getIdentifier());
            ps.setString(2, model.getLanguageCode());
            ps.setBoolean(3, model.isFirstJoin());
            ps.setString(4, model.getLanguageCode());
            ps.setBoolean(5, model.isFirstJoin());
            ps.executeUpdate();
        }
    }

    @Override
    protected LanguageModel loadFromDatabase(String identifier) {
        try (Connection conn = de.mecrytv.languageVelocity.LanguageVelocity.getInstance().getServiceManager().getMariaDBManager().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM network_languages WHERE uuid = ?")) {
            ps.setString(1, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LanguageModel(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("language"),
                            rs.getBoolean("first_join")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<LanguageModel> getAllFromDatabase() {
        List<LanguageModel> list = new ArrayList<>();
        try (Connection conn = de.mecrytv.languageVelocity.LanguageVelocity.getInstance().getServiceManager().getMariaDBManager().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM network_languages")) {
            while (rs.next()) {
                list.add(new LanguageModel(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("language"),
                        rs.getBoolean("first_join")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void removeFromDatabase(Connection conn, String identifier) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM network_languages WHERE uuid = ?")) {
            ps.setString(1, identifier);
            ps.executeUpdate();
        }
    }
}