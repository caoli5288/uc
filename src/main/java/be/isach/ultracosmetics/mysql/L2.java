package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 17-2-23.
 */
public class L2 {

    private final Map<String, Integer> ammo = new HashMap<>();
    private final Map<String, String> p = new HashMap<>();
    private final int index;
    private int gadget = -1;
    private int selfMorphView = -1;
    private int key = -1;

    public L2(int index) {
        this.index = index;
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

    public String getPet(String pet) {
        return p.get(pet);
    }

    public void setPet(String pet, String name) {
        p.put(pet, name);
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

    public int getIndex() {
        return index;
    }

}
