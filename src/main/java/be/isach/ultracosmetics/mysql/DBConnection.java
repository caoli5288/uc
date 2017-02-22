package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.gadgets.GadgetType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Package: be.isach.ultracosmetics.mysql
 * Created by: sachalewin
 * Date: 5/08/16
 * Project: UltraCosmetics
 */
public class DBConnection {

    private static final Map<UUID, Integer> INDEX = new HashMap<>();

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
            for (GadgetType gadgetType : GadgetType.values()) {
                DatabaseMetaData d = connection.getMetaData();
                try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", gadgetType.toString().replace("_", "").toLowerCase())) {
                    if (!r.next()) {
                        try (PreparedStatement st = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD " + gadgetType.toString().replace("_", "").toLowerCase() + " INTEGER DEFAULT 0 not NULL")) {
                            st.executeUpdate();
                        }
                    }
                }
            }
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, "UltraCosmeticsData", "treasureKeys");
            if (!rs.next()) {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE UltraCosmeticsData ADD treasureKeys INTEGER DEFAULT 0 NOT NULL");
                statement.executeUpdate();
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

    public int getIndexId(OfflinePlayer p) {
        Integer i = INDEX.get(p.getUniqueId());
        if ($.nil(i)) {
            try (SelectQuery.Binding b = query().select().where("uuid", p.getUniqueId() + "").execute()) {
                i = b.getResult().getInt("id");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            INDEX.put(p.getUniqueId(), i);
        }
        return $.nil(i) ? -1 : i;
    }

    public void putIndexId(OfflinePlayer p, int i) {
        INDEX.put(p.getUniqueId(), i);
    }

}
