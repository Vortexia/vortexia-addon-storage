// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.network;

import me.alikuxac.vortexia.addon.storage.StorageAddon;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final StorageAddon addon;
    private Connection connection;

    public DatabaseManager(StorageAddon addon) {
        this.addon = addon;
        init();
    }

    private void init() {
        try {
            File dataFolder = addon.getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "storage.db"));

            try (Statement s = connection.createStatement()) {
                // Table for Cloud Inventories
                s.execute("CREATE TABLE IF NOT EXISTS cloud_inventories (" +
                        "id TEXT PRIMARY KEY, " +
                        "data TEXT NOT NULL)");

                // Table for Cloud Channels
                s.execute("CREATE TABLE IF NOT EXISTS cloud_channels (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT, " +
                        "owner TEXT, " +
                        "password TEXT, " +
                        "members TEXT, " +
                        "blocked TEXT)");

                // Table for Storage Grids (Nodes)
                s.execute("CREATE TABLE IF NOT EXISTS storage_grids (" +
                        "grid_id TEXT, " +
                        "node_data TEXT)");
            }
        } catch (Exception e) {
            addon.getLogger().severe("Could not initialize SQLite database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) init();
        } catch (SQLException e) {
            init();
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            addon.getLogger().warning("Error closing database: " + e.getMessage());
        }
    }

    // --- Helpers for CRUD ---

    public String getCloudData(String id) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT data FROM cloud_inventories WHERE id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("data");
        } catch (SQLException e) {
            addon.getLogger().severe("Error reading cloud data: " + e.getMessage());
        }
        return null;
    }

    public void saveCloudData(String id, String json) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO cloud_inventories (id, data) VALUES (?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            addon.getLogger().severe("Error saving cloud data: " + e.getMessage());
        }
    }

    public List<CloudChannelManager.CloudChannel> loadChannels() {
        List<CloudChannelManager.CloudChannel> list = new ArrayList<>();
        try (Statement s = getConnection().createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * FROM cloud_channels");
            while (rs.next()) {
                CloudChannelManager.CloudChannel c = new CloudChannelManager.CloudChannel(
                        rs.getString("id"),
                        rs.getString("name"),
                        UUID.fromString(rs.getString("owner")),
                        rs.getString("password")
                );
                String members = rs.getString("members");
                if (members != null && !members.isEmpty()) {
                    for (String m : members.split(",")) c.members.add(UUID.fromString(m));
                }
                String blocked = rs.getString("blocked");
                if (blocked != null && !blocked.isEmpty()) {
                    for (String b : blocked.split(",")) c.blocked.add(UUID.fromString(b));
                }
                list.add(c);
            }
        } catch (SQLException e) {
            addon.getLogger().severe("Error loading channels: " + e.getMessage());
        }
        return list;
    }

    public void saveChannel(CloudChannelManager.CloudChannel channel) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO cloud_channels (id, name, owner, password, members, blocked) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, channel.id);
            ps.setString(2, channel.name);
            ps.setString(3, channel.owner.toString());
            ps.setString(4, channel.password);
            
            List<String> mStrings = new ArrayList<>();
            for (UUID u : channel.members) mStrings.add(u.toString());
            ps.setString(5, String.join(",", mStrings));

            List<String> bStrings = new ArrayList<>();
            for (UUID u : channel.blocked) bStrings.add(u.toString());
            ps.setString(6, String.join(",", bStrings));

            ps.executeUpdate();
        } catch (SQLException e) {
            addon.getLogger().severe("Error saving channel: " + e.getMessage());
        }
    }

    public void saveGrid(String gridId, String json) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO storage_grids (grid_id, node_data) VALUES (?, ?)")) {
            ps.setString(1, gridId);
            ps.setString(2, json);
            ps.executeUpdate();
        } catch (SQLException e) {
            addon.getLogger().severe("Error saving grid: " + e.getMessage());
        }
    }

    public List<String[]> loadGrids() {
        List<String[]> list = new ArrayList<>();
        try (Statement s = getConnection().createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * FROM storage_grids");
            while (rs.next()) {
                list.add(new String[]{rs.getString("grid_id"), rs.getString("node_data")});
            }
        } catch (SQLException e) {
            addon.getLogger().severe("Error loading grids: " + e.getMessage());
        }
        return list;
    }
}
