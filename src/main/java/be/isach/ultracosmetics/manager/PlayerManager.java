package be.isach.ultracosmetics.manager;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private Map<UUID, UltraPlayer> handle;

    public PlayerManager() {
        this.handle = new ConcurrentHashMap<>();
    }

    public UltraPlayer getUltraPlayer(Player player) {
        UltraPlayer p = handle.get(player.getUniqueId());
        if (p == null) return get(player);
        return p;
    }

    public UltraPlayer get(Player player) {
        UltraPlayer p = new UltraPlayer(player);
        handle.put(player.getUniqueId(), p);
        if (!Main.getInstance().usingFileStorage()) Main.db.init(p);
        return p;
    }

    public void remove(Player player) {
        handle.remove(player.getUniqueId());
    }

    public Collection<UltraPlayer> getPlayers() {
        return handle.values();
    }

    public void dispose() {
        Collection<UltraPlayer> set = handle.values();
        for (UltraPlayer cp : set) {
            if (cp.currentTreasureChest != null)
                cp.currentTreasureChest.forceOpen(0);
            cp.clear();
            cp.removeMenuItem();
        }

        handle.clear();
        handle = null;
    }

}
