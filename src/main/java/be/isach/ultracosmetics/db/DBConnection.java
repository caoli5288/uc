package be.isach.ultracosmetics.db;

import be.isach.ultracosmetics.$;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.gadgets.GadgetType;
import lombok.val;
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

    public static final String TABLE = "CREATE TABLE " +
            "IF NOT EXISTS `UltraCosmeticsData` (" +
            "  `id` INT NOT NULL AUTO_INCREMENT," +
            "  `uuid` CHAR (36) NOT NULL," +
            "  `username` CHAR (16) NOT NULL," +
            "  `treasureKeys` INT NOT NULL," +
            "  `gadgetsEnabled` INT NOT NULL DEFAULT 1," +
            "  `selfmorphview` INT NOT NULL DEFAULT 1," +
            "  `pet_name` CHAR (255)," +
            "  PRIMARY KEY (`id`)," +
            "  INDEX `idx_uuid` (`uuid`, `username`)" +
            ");";

    public void init() {
        try {
            Connection connection = conn();
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(TABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseMetaData d = connection.getMetaData();
            for (val type : GadgetType.values()) {
                try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", type.toString().replace("_", "").toLowerCase())) {
                    if (!r.next()) {
                        try (PreparedStatement st = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD " + type.toString().replace("_", "").toLowerCase() + " INT DEFAULT 0 not NULL")) {
                            st.executeUpdate();
                        }
                    }
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
