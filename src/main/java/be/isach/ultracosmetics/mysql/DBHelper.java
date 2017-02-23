package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;
import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import lombok.val;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.SQLException;

/**
 * Package: be.isach.ultracosmetics.mysql
 * Created by: sacha
 * Date: 15/08/15
 * Project: UltraCosmetics
 */
public class DBHelper {

    private DBConnection connection;

    public DBHelper(DBConnection manager) {
        this.connection = manager;
    }

    public void init(UltraPlayer player) {
        Player p = player.getPlayer();
        if (!UltraPlayer.INDEX.containsKey(p.getUniqueId())) {
            try (val b = connection.query().select().where("uuid", p.getUniqueId().toString()).execute()) {
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
                    UltraPlayer.putIndexId(p, b.result.getInt("id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Table query() {
        return connection.query();
    }

    /**
     * @return always not {@code null} for upsource code safe
     */
    public String getPetName(int index, String pet) {
        L2 l2 = L2Pool.get(index);
        String p = l2.getPetName(pet);
        if ($.nil(p)) {
            try (val b = connection.query().select("pet_name").where("id", index).execute()) {
                l2.setPetName(pet, null); // magic pre-init handled map
                if (b.result.next()) {
                    val raw = b.result.getString("pet_name");
                    if (!$.nil(raw)) {
                        JSONObject object = (JSONObject) JSONValue.parse(raw);
                        if (!($.nil(object) || object.isEmpty())) {
                            object.forEach(l2::setPetName);
                        }
                    }
                    p = l2.getPetName(pet);
                }
            } catch (SQLException e) {
                $.log(e);
            }
        }
        return $.valid(p, "");
    }

    public void setPetName(int index, String pet, String name) {
        getPetName(index, pet); // pre-load
        try {
            L2 l2 = L2Pool.get(index);
            l2.setPetName(pet, name);
            connection.query().update().set("pet_name", l2.getPetValue()).where("id", index).execute();
        } catch (SQLException e) {
            $.log(e);
        }
    }

    public int getKey(int index) {
        L2 l2 = L2Pool.get(index);
        int key = l2.getKey();
        if (key == -1) {
            try (val b = connection.query().select("treasureKeys").where("id", index).execute()) {
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
        try {
            connection.query().update().set("treasureKeys", value).where("id", index).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        L2Pool.get(index).setKey(value);
    }

    public void removeKey(int index) {
        setKey(index, getKey(index) - 1);
    }

    public void addKey(int index) {
        setKey(index, getKey(index) + 1);
    }

    public int getAmmo(int index, String name) {
        val col = name.replace("_", "");
        L2 l2 = L2Pool.get(index);
        int result = l2.getAmmo(col);
        if (result == -1) {
            try (val b = connection.query().select(col).where("id", index).execute()) {
                if (b.result.next()) {
                    result = b.result.getInt(col);
                } else {
                    result = 0;
                }
                Main.debug("DEBUG #2 " + index + " " + name + " " + result);
            } catch (SQLException e) {
                $.log(e);
            }
            l2.setAmmo(name, result);
        }
        return result;
    }

    public void setAmmo(int index, String name, int value) {
        val col = name.replace("_", "");
        try {
            connection.query().update().set(col, value).where("id", index).execute();
        } catch (SQLException e) {
            $.log(e);
        }// Update l2cache even if db update failed
        L2Pool.get(index).setAmmo(col, value);
    }

    public void removeAmmo(int index, String name) {
        setAmmo(index, name, getAmmo(index, name) - 1);
    }

    public void addAmmo(int index, String name, int i) {
        setAmmo(index, name, getAmmo(index, name) + i);
    }

    public void save(L2 l2) {
        try {
            connection.query().update().set("gadgetsEnabled", l2.getGadget()).where("id", l2.getId()).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setGadgetsEnabled(int index, boolean b) {
        L2Pool.get(index).setGadget(b ? 1 : 0);
    }

    public boolean hasGadgetsEnabled(int index) {
        L2 l2 = L2Pool.get(index);
        int gadget = l2.getGadget();
        if (gadget == -1) {
            try {
                gadget = 1; // set default value at first
                try (val b = connection.query().select("gadgetsEnabled").where("id", index).execute()) {
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
                try (val b = connection.query().select("selfmorphview").where("id", index).execute()) {
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
