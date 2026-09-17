package de.example.hearts;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public final class DatabaseManager implements AutoCloseable {
    private final HeartsPlugin plugin;
    private Connection connection;

    public DatabaseManager(HeartsPlugin plugin) { this.plugin = plugin; }

    public void open() throws SQLException {
        File db = new File(plugin.getDataFolder(), "hearts.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, hearts INTEGER NOT NULL, banned INTEGER NOT NULL DEFAULT 0)");
        }
    }

    public synchronized PlayerData load(UUID uuid, int startingHearts) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT hearts,banned FROM players WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new PlayerData(rs.getInt("hearts"), rs.getBoolean("banned"));
            }
        }
        save(uuid, startingHearts, false);
        return new PlayerData(startingHearts, false);
    }

    public synchronized void save(UUID uuid, int hearts, boolean banned) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO players(uuid,hearts,banned) VALUES(?,?,?) ON CONFLICT(uuid) DO UPDATE SET hearts=excluded.hearts,banned=excluded.banned")) {
            ps.setString(1, uuid.toString()); ps.setInt(2, hearts); ps.setBoolean(3, banned); ps.executeUpdate();
        }
    }

    public synchronized void setBanned(UUID uuid, boolean banned) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE players SET banned=? WHERE uuid=?")) {
            ps.setBoolean(1, banned); ps.setString(2, uuid.toString()); ps.executeUpdate();
        }
    }

    public record PlayerData(int hearts, boolean banned) {}

    @Override public synchronized void close() {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
    }
}
