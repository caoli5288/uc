package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.gadgets.GadgetType;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Package: be.isach.ultracosmetics.mysql
 * Created by: sachalewin
 * Date: 5/08/16
 * Project: UltraCosmetics
 */
public class DBConnection {

    /**
     * MySQL Connection & Table.
     */
    private Connection connection;

    public static final String TABLE = "CREATE TABLE IF NOT EXISTS UltraCosmeticsData(" +
            "id INTEGER not NULL AUTO_INCREMENT," +
            " uuid VARCHAR(255)," +
            " username VARCHAR(255)," +
            " PRIMARY KEY ( id ))";

    public void init() {
        Connection connection = conn();
        try {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(TABLE);
            }
            DatabaseMetaData d = connection.getMetaData();
            for (GadgetType gadgetType : GadgetType.values()) {
                try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", gadgetType.toString().replace("_", "").toLowerCase())) {
                    if (!r.next()) {
                        try (PreparedStatement st = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD " + gadgetType.toString().replace("_", "").toLowerCase() + " INTEGER DEFAULT 0 not NULL")) {
                            st.executeUpdate();
                        }
                    }
                }
            }
            try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "gadgetsEnabled")) {
                if (!r.next()) {
                    PreparedStatement statement = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD gadgetsEnabled INT NOT NULL DEFAULT 1");
                    statement.executeUpdate();
                    statement.close();
                }
            }
            try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "selfmorphview")) {
                if (!r.next()) {
                    PreparedStatement statement = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD selfmorphview INT NOT NULL DEFAULT 1");
                    statement.executeUpdate();
                    statement.close();
                }
            }
            try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "treasureKeys")) {
                if (!r.next()) {
                    PreparedStatement statement = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD treasureKeys INTEGER DEFAULT 0 NOT NULL");
                    statement.executeUpdate();
                    statement.close();
                }
            }
            try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "pet_name")) {
                if (!r.next()) {
                    PreparedStatement statement = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD pet_name TEXT");
                    statement.executeUpdate();
                    statement.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            String hostname = String.valueOf(SettingsManager.getConfig().get("Ammo-System-For-Gadgets.MySQL.hostname"));
            String portNumber = String.valueOf(SettingsManager.getConfig().get("Ammo-System-For-Gadgets.MySQL.port"));
            String database = String.valueOf(SettingsManager.getConfig().get("Ammo-System-For-Gadgets.MySQL.database"));
            String username = String.valueOf(SettingsManager.getConfig().get("Ammo-System-For-Gadgets.MySQL.username"));
            String password = String.valueOf(SettingsManager.getConfig().get("Ammo-System-For-Gadgets.MySQL.password"));
            DBSpec sql = new DBSpec(hostname, portNumber, database, username, password);
            connection = sql.getConnection();
            Bukkit.getConsoleSender().sendMessage("§b§lUltraCosmetics -> Successfully connected to MySQL server! :)");
        } catch (Exception e) {
            Bukkit.getLogger().info("");
            Bukkit.getConsoleSender().sendMessage("§c§lUltra Cosmetics >>> Could not connect to MySQL server!");
            Bukkit.getLogger().info("");
            Bukkit.getConsoleSender().sendMessage("§c§lError:");
            e.printStackTrace();
        }
    }

    public Table query() {
        return new Table(conn(), "UltraCosmeticsData");
    }

    public Connection conn() {
        if (!$.nil(connection)) {
            try {
                if (!connection.isValid(1)) {
                    connect();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            connect();
        }
        return connection;
    }

}
