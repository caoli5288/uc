package be.isach.ultracosmetics.listeners;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.db.L2;
import be.isach.ultracosmetics.db.L2Pool;
import be.isach.ultracosmetics.run.FallDamageManager;
import be.isach.ultracosmetics.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if ((boolean) SettingsManager.getConfig().get("Menu-Item.Give-On-Join") && player.hasPermission("ultracosmetics.receivechest") && ((List<String>) SettingsManager.getConfig().get("Enabled-Worlds")).contains(player.getWorld().getName())) {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (player.isOnline()) {
                    UltraPlayer.giveMenu(player);
                }
            }, 5);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                if ((boolean) SettingsManager.getConfig().get("Menu-Item.Give-On-Join") && event.getPlayer().hasPermission("ultracosmetics.receivechest") && ((List<String>) SettingsManager.getConfig().get("Enabled-Worlds")).contains(event.getPlayer().getWorld().getName())) {
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            Main.getUltraPlayer(event.getPlayer()).giveMenuItem();
                        }
                    }, 5);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().hasItemMeta()
                && event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()
                && event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§"))) {
            event.setCancelled(true);
            event.getItemDrop().remove();
            ItemStack chest = event.getPlayer().getItemInHand().clone();
            chest.setAmount(1);
            event.getPlayer().setItemInHand(chest);
            event.getPlayer().updateInventory();
        }
    }

    /**
     * Cancel players from removing, picking the item in their inventory.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if ((SettingsManager.getConfig().getStringList("Enabled-Worlds")).contains(player.getWorld().getName())) {

            if ((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                    && (event.getCursor() == null || event.getCursor().getType() == Material.AIR)) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }

            if (event.getCurrentItem() != null
                    && event.getCurrentItem().hasItemMeta()
                    && event.getCurrentItem().getItemMeta().hasDisplayName()
                    && event.getCurrentItem().getItemMeta().getDisplayName().equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§"))) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }
    }

    /**
     * Cancel players from removing, picking the item in their inventory.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelMove(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null
                    && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§"))) {
                event.setCancelled(true);
                ((Player) event.getWhoClicked()).updateInventory();
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if ((boolean) SettingsManager.getConfig().get("Menu-Item.Give-On-Respawn") && ((List<String>) SettingsManager.getConfig().get("Enabled-Worlds")).contains(event.getPlayer().getWorld().getName())) {
            int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
            if (event.getPlayer().getInventory().getItem(slot) != null) {
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), event.getPlayer().getInventory().getItem(slot));
                event.getPlayer().getInventory().remove(slot);
            }
            String name = String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§");
            Material material = Material.valueOf((String) SettingsManager.getConfig().get("Menu-Item.Type"));
            byte data = Byte.valueOf(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Data")));
            event.getPlayer().getInventory().setItem(slot, ItemFactory.create(material, data, name));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        UltraPlayer player = Main.getUltraPlayer(p);
        if (player.currentTreasureChest != null)
            player.currentTreasureChest.forceOpen(0);
        player.clear();
        Main.getPlayerManager().getUltraPlayer(p).removeMenuItem();
        Main.getPlayerManager().remove(p);
        L2 l2 = L2Pool.get(Main.index(p));
        if (l2.getGadget() > -1) {
            Main.db.saveGadgetEnabled(l2.getId(), l2.getGadget());
        }
        L2Pool.del(l2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
        if (event.getEntity().getInventory().getItem(slot) != null
                && event.getEntity().getInventory().getItem(slot).hasItemMeta()
                && event.getEntity().getInventory().getItem(slot).getItemMeta().hasDisplayName()
                && event.getEntity().getInventory().getItem(slot).getItemMeta().getDisplayName().equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§"))) {
            event.getDrops().remove(event.getEntity().getInventory().getItem(slot));
            event.getEntity().getInventory().setItem(slot, null);
        }
        if (Main.getUltraPlayer(event.getEntity()).currentGadget != null)
            event.getDrops().remove(event.getEntity().getInventory().getItem((Integer) SettingsManager.getConfig().get("Gadget-Slot")));
        if (Main.getUltraPlayer(event.getEntity()).currentHat != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentHat.getItemStack());
        if (Main.getUltraPlayer(event.getEntity()).currentHelmet != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentHelmet.getItemStack());
        if (Main.getUltraPlayer(event.getEntity()).currentChestplate != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentChestplate.getItemStack());
        if (Main.getUltraPlayer(event.getEntity()).currentLeggings != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentLeggings.getItemStack());
        if (Main.getUltraPlayer(event.getEntity()).currentBoots != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentBoots.getItemStack());
        if (Main.getUltraPlayer(event.getEntity()).currentEmote != null)
            event.getDrops().remove(Main.getUltraPlayer(event.getEntity()).currentEmote.getItemStack());
        Main.getUltraPlayer(event.getEntity()).clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && FallDamageManager.shouldBeProtected(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickUpItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack() != null
                && event.getItem().getItemStack().hasItemMeta()
                && event.getItem().getItemStack().getItemMeta().hasDisplayName()
                && event.getItem().getItemStack().getItemMeta().getDisplayName().equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§"))) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractGhost(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() != null
                && event.getRightClicked().hasMetadata("C_AD_ArmorStand"))
            event.setCancelled(true);
    }

}
