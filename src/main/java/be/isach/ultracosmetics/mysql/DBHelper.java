package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;
import be.isach.ultracosmetics.UltraPlayer;
import org.bukkit.entity.Player;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Package: be.isach.ultracosmetics.mysql
 * Created by: sacha
 * Date: 15/08/15
 * Project: UltraCosmetics
 */
public class DBHelper {

    public DBConnection connection;
    private boolean name;
    private boolean gadget;
    private boolean selfMorphView;

    public DBHelper(DBConnection manager) {
        this.connection = manager;
    }

    public void init(UltraPlayer player) {
        Player p = player.getPlayer();
        try (SelectQuery.Binding b = connection.query().select().where("uuid", p.getUniqueId().toString()).execute()) {
            if (!b.result.next()) {
                connection.query().insert().insert("uuid").value(p.getUniqueId().toString()).execute();
                connection.query().update().set("username", p.getName()).where("uuid", p.getUniqueId().toString()).execute();
            } else {
                String username = b.result.getString("username");
                if (username == null) {
                    connection.query().update().set("username", p.getName()).where("uuid", p.getUniqueId().toString()).execute();
                    return;
                }
                if (!username.equals(p.getName())) {
                    connection.query().update().set("username", p.getName()).where("uuid", p.getUniqueId().toString()).execute();
                }
                connection.putIndexId(p, b.result.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPetName(int index, String pet) {
        L2 l2 = L2Pool.get(index);
        String p = l2.getPet(pet);
        if ($.nil(p)) {
            if (!this.name) {
                try {
                    DatabaseMetaData d = connection.conn().getMetaData();
                    try (ResultSet result = d.getColumns(null, null, "UltraCosmeticsData", "name" + pet)) {
                        if (!result.next()) {
                            PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD name" + pet + " varchar(255)");
                            statement.executeUpdate();
                            statement.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                this.name = true;
            }
            try (SelectQuery.Binding b = connection.query().select().where("id", index).execute()) {
                if (b.result.next()) {
                    p = b.result.getString("name" + pet);
                }
            } catch (SQLException e) {
                $.log(e);
            }
            l2.setPet(pet, $.nil(p) ? "" : p);
        }
        return p.isEmpty() ? null : p;
    }

    public void setPetName(int index, String pet, String name) {
        try {
            DatabaseMetaData d = connection.conn().getMetaData();
            if (!this.name) {
                try (ResultSet result = d.getColumns(null, null, "UltraCosmeticsData", "name" + pet)) {
                    if (!result.next()) {
                        PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD name" + pet + " varchar(255)");
                        statement.executeUpdate();
                        statement.close();
                    }
                }
                this.name = true;
            }
            connection.query().update().set("name" + pet, name).where("id", index).execute();
            L2Pool.get(index).setPet(pet, name);
        } catch (SQLException e) {
            $.log(e);
        }
    }

    public int getKey(int index) {
        L2 l2 = L2Pool.get(index);
        int key = l2.getKey();
        if (key == -1) {
            try (SelectQuery.Binding b = connection.query().select().where("id", index).execute()) {
                if (b.result.next()) {
                    key = b.result.getInt("treasureKeys");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            l2.setKey(key == -1 ? 0 : key);
        }
        return key;
    }

    public void setKey(int index, int value) {
        connection.query().update().set("treasureKeys", value).where("id", index).execute();
        L2Pool.get(index).setKey(value);
    }

    public void removeKey(int index) {
        setKey(index, getKey(index) - 1);
    }

    public void addKey(int index) {
        setKey(index, getKey(index) + 1);
    }

    public int getAmmo(int index, String name) {
        L2 l2 = L2Pool.get(index);
        int result = l2.getAmmo(name);
        if (result == -1) {
            String col = name.replace("_", "");
            try (SelectQuery.Binding b = connection.query().select(col).where("id", index).execute()) {
                if (b.result.next()) {
                    result = b.result.getInt(col);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            l2.setAmmo(name, result == -1 ? 0 : result);
        }
        return result;
    }

    public void setAmmo(int index, String name, int value) {
        connection.query().update().set(name.replace("_", ""), value).where("id", index).execute();
        L2Pool.get(index).setAmmo(name, value);
    }

    public void removeAmmo(int index, String name) {
        setAmmo(index, name, getAmmo(index, name) - 1);
    }

    public void addAmmo(int index, String name, int i) {
        setAmmo(index, name, getAmmo(index, name) + i);
    }

    public void save(L2 l2) {
        connection.query().update().set("gadgetsEnabled", l2.getGadget()).where("id", l2.getIndex()).execute();
    }

    public void setGadgetsEnabled(int index, boolean enabled) {
        try {
            if (!this.gadget) {
                DatabaseMetaData d = connection.conn().getMetaData();
                try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "gadgetsEnabled")) {
                    if (!r.next()) {
                        PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD gadgetsEnabled INT NOT NULL DEFAULT 1");
                        statement.executeUpdate();
                        statement.close();
                    }
                }
                this.gadget = true;
            }
            L2Pool.get(index).setGadget(enabled ? 1 : 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasGadgetsEnabled(int index) {
        L2 l2 = L2Pool.get(index);
        int gadget = l2.getGadget();
        if (gadget == -1) {
            try {
                gadget = 1; // set default value at first
                if (!this.gadget) {
                    DatabaseMetaData d = connection.conn().getMetaData();
                    try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "gadgetsEnabled")) {
                        if (!r.next()) {
                            PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD gadgetsEnabled INT NOT NULL DEFAULT 1");
                            statement.executeUpdate();
                            statement.close();
                        }
                    }
                    this.gadget = true;
                }
                try (SelectQuery.Binding b = connection.query().select().where("id", index).execute()) {
                    if (b.result.next()) {
                        gadget = b.result.getInt("gadgetsEnabled");
                    }
                }
                l2.setGadget(gadget);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return gadget == 1;
    }

    public void setSeeSelfMorph(int index, boolean enabled) {
        try {
            if (!this.selfMorphView) {
                DatabaseMetaData d = connection.conn().getMetaData();
                try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "selfmorphview")) {
                    if (!r.next()) {
                        PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD selfmorphview INT NOT NULL DEFAULT 1");
                        statement.executeUpdate();
                        statement.close();
                    }
                }
                this.selfMorphView = true;
            }
            connection.query().update().set("selfmorphview", enabled ? 1 : 0).where("id", index).execute();
            L2Pool.get(index).setSelfMorphView(enabled ? 1 : 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean canSeeSelfMorph(int index) {
        L2 l2 = L2Pool.get(index);
        int selfMorphView = l2.getSelfMorphView();
        if (selfMorphView == -1) {
            try {
                selfMorphView = 1; // default 1
                if (!this.selfMorphView) {
                    DatabaseMetaData d = connection.conn().getMetaData();
                    try (ResultSet r = d.getColumns(null, null, "UltraCosmeticsData", "selfmorphview")) {
                        if (!r.next()) {
                            PreparedStatement statement = connection.conn().prepareStatement("ALTER TABLE UltraCosmeticsData ADD selfmorphview INT NOT NULL DEFAULT 1");
                            statement.executeUpdate();
                            statement.close();
                        }
                    }
                    this.selfMorphView = true;
                }
                try (SelectQuery.Binding b = connection.query().select().where("id", index).execute()) {
                    if (b.result.next()) {
                        selfMorphView = b.result.getInt("selfmorphview");
                    }
                }
                l2.setSelfMorphView(selfMorphView);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return selfMorphView == 1;
    }

}
