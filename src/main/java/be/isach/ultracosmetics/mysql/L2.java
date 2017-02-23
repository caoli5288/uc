package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 17-2-23.
 */
public class L2 {

    private final Map<String, Integer> ammo = new HashMap<>();
    private final int id;
    private int gadget = -1;
    private int selfMorphView = -1;
    private int key = -1;

    private JSONObject p;

    public L2(int id) {
        this.id = id;
    }

    public int getGadget() {
        return gadget;
    }

    public void setGadget(int gadget) {
        this.gadget = gadget;
    }

    public int getSelfMorphView() {
        return selfMorphView;
    }

    public void setSelfMorphView(int selfMorphView) {
        this.selfMorphView = selfMorphView;
    }

    public String getPetName(String pet) {// Magic func
        if ($.nil(p)) return null;
        return $.valid((String) p.get(pet), "");
    }

    @SuppressWarnings("all")
    public void setPetName(Object pet, Object name) {
        if ($.nil(p)) p = new JSONObject();
        if ($.nil(name) || String.valueOf(name).isEmpty()) {
            p.remove(pet, name);
        } else {
            p.put(pet, name);
        }
    }

    public String getPetValue() {
        return $.nil(p) ? "{}" : String.valueOf(p);
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getAmmo(String name) {
        return $.valid(ammo.get(name), -1);
    }

    public int setAmmo(String name, int value) {
        return $.valid(ammo.put(name, value), -1);
    }

    public int getId() {
        return id;
    }

}
