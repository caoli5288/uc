package be.isach.ultracosmetics;

import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.emotes.Emote;
import be.isach.ultracosmetics.cosmetics.gadgets.Gadget;
import be.isach.ultracosmetics.cosmetics.gadgets.GadgetType;
import be.isach.ultracosmetics.cosmetics.hats.Hat;
import be.isach.ultracosmetics.cosmetics.morphs.Morph;
import be.isach.ultracosmetics.cosmetics.mounts.Mount;
import be.isach.ultracosmetics.cosmetics.particleeffects.ParticleEffect;
import be.isach.ultracosmetics.cosmetics.pets.Pet;
import be.isach.ultracosmetics.cosmetics.suits.ArmorSlot;
import be.isach.ultracosmetics.cosmetics.suits.Suit;
import be.isach.ultracosmetics.cosmetics.treasurechests.TreasureChest;
import be.isach.ultracosmetics.mysql.SelectQuery;
import be.isach.ultracosmetics.util.ItemFactory;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UltraPlayer {

    public static final Map<UUID, Integer> INDEX = new HashMap<>(); // TODO not flush yet, change if any issue

    private Player player;
    public UUID id;

    public Gadget currentGadget;
    public Mount currentMount;
    public ParticleEffect currentParticleEffect;
    public Pet currentPet;
    public TreasureChest currentTreasureChest;
    public Morph currentMorph;
    public Hat currentHat;
    public Suit currentHelmet;
    public Suit currentChestplate;
    public Suit currentLeggings;
    public Suit currentBoots;

    public Emote currentEmote;

    /**
     * boolean to identify if player is loaded correctly
     */
    private boolean loaded;

    /**
     * Cooldown map storing all the current cooldowns for gadgets.
     */
    private HashMap<GadgetType, Long> cooldown;

    /**
     * Allows to store custom data for each player easily.
     * <p/>
     * Created on join, and deleted on quit.
     */
    public UltraPlayer(Player player) {
        try {
            this.id = player.getUniqueId();

            cooldown = new HashMap<>();

            if (Main.getInstance().usingFileStorage())
                SettingsManager.getData(getPlayer()).addDefault("Keys", 0);

            if (Main.getInstance().isAmmoEnabled()) {
                if (Main.getInstance().usingFileStorage())
                    for (GadgetType type : GadgetType.values())
                        if (type.isEnabled())
                            SettingsManager.getData(getPlayer()).addDefault("Ammo." + type.toString().toLowerCase(), 0);
            }
            if (Main.getInstance().usingFileStorage()) {
                SettingsManager.getData(getPlayer()).addDefault("Gadgets-Enabled", true);
                SettingsManager.getData(getPlayer()).addDefault("Third-Person-Morph-View", true);
            }
            this.player = player;
            loaded = true;
        } catch (Exception exc) {
            // Player couldn't be found.
            System.out.println("UltraCosmetics ERR -> " + "Couldn't find player with UUID: " + id);
        }
    }

    /**
     * Checks if a player can use a given gadget type.
     *
     * @param gadget The gadget type.
     * @return -1 if player can use, otherwise the time left (in seconds).
     */
    public double canUse(GadgetType gadget) {
        Object count = cooldown.get(gadget);
        if (count == null)
            return -1;
        if (System.currentTimeMillis() > (long) count)
            return -1;
        double valueMillis = (long) count - System.currentTimeMillis();
        return valueMillis / 1000d;
    }

    /**
     * Sets the cooldown of a gadget.
     *
     * @param gadget    The gadget.
     * @param countdown The cooldown to set.
     */
    public void setCoolDown(GadgetType gadget, double countdown) {
        cooldown.put(gadget, (long) (countdown * 1000 + System.currentTimeMillis()));
    }

    /**
     * Get the player owning the UltraPlayer.
     *
     * @return The player owning the UltraPlayer.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Removes the current gadget.
     */
    public void removeGadget() {
        if (currentGadget != null) {
            if (getPlayer() != null)
                getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Unequip").replace("%gadgetname%", (Main.getInstance().placeholdersHaveColor()) ? currentGadget.getName() : Main.filterColor(currentGadget.getName())));
            currentGadget.removeItem();
            currentGadget.onClear();
            currentGadget.removeListener();
            currentGadget.unregisterListeners();
            currentGadget = null;
        }
    }


    /**
     * Removes the current emote.
     */
    public void removeEmote() {
        if (currentEmote != null) {
            if (getPlayer() != null)
                getPlayer().sendMessage(MessageManager.getMessage("Emotes.Unequip")
                        .replace("%emotename%", (Main.getInstance().placeholdersHaveColor())
                                ? currentEmote.getName() : Main.filterColor(currentEmote.getName())));
            currentEmote.clear();
            currentEmote = null;
        }
    }

    /**
     * Removes the current Mount.
     */
    public void removeMount() {
        if (currentMount != null) {
            currentMount.clear();
            currentMount = null;
            getPlayer().removePotionEffect(PotionEffectType.CONFUSION);
        }
    }

    /**
     * Removes the current Pet.
     */
    public void removePet() {
        if (currentPet != null) {
            if (currentPet.armorStand != null)
                currentPet.armorStand.remove();
            for (Item item : currentPet.items)
                item.remove();
            currentPet.clear();
            currentPet = null;
        }
    }

    /**
     * Gives a key to the player.
     */
    public void addKey() {
        if (Main.getInstance().usingFileStorage())
            SettingsManager.getData(getPlayer()).set("Keys", getKeys() + 1);
        else
            Main.db.addKey(index());
    }

    /**
     * Removes a key to the player.
     */
    public void removeKey() {
        if (Main.getInstance().usingFileStorage())
            SettingsManager.getData(getPlayer()).set("Keys", getKeys() - 1);
        else
            Main.db.removeKey(index());
    }

    /**
     * @return The amount of keys that the player owns.
     */
    public int getKeys() {
        return Main.getInstance().usingFileStorage() ? (int) SettingsManager.getData(getPlayer()).get("Keys") : Main.db.getKey(index());
    }

    /**
     * Removes the current hat.
     */
    public void removeHat() {
        if (currentHat == null) return;
        getPlayer().getInventory().setHelmet(null);

        getPlayer().sendMessage(MessageManager.getMessage("Hats.Unequip")
                .replace("%hatname%",
                        (Main.getInstance().placeholdersHaveColor()) ? currentHat.getName() : Main.filterColor(currentHat.getName())));
        currentHat = null;
    }

    /**
     * Removes the current suit of armorSlot.
     *
     * @param armorSlot The ArmorSlot to remove.
     */
    public void removeSuit(ArmorSlot armorSlot) {
        switch (armorSlot) {
            case HELMET:
                if (currentHelmet != null)
                    currentHelmet.clear();
                break;
            case CHESTPLATE:
                if (currentChestplate != null)
                    currentChestplate.clear();
                break;
            case LEGGINGS:
                if (currentLeggings != null)
                    currentLeggings.clear();
                break;
            case BOOTS:
                if (currentBoots != null)
                    currentBoots.clear();
                break;
        }
    }

    public double getBalance() {
        try {
            if (Main.getInstance().isVaultLoaded() && Main.economy != null)
                return Main.economy.getBalance(getPlayer());
        } catch (Exception exc) {
            Main.log("Error happened while getting a player's balance.");
            return 0;
        }
        return 0;
    }

    /**
     * @param armorSlot The armorslot to get.
     * @return The Suit from the armor slot.
     */
    public Suit getSuit(ArmorSlot armorSlot) {
        switch (armorSlot) {
            case HELMET:
                return currentHelmet;
            case CHESTPLATE:
                return currentChestplate;
            case LEGGINGS:
                return currentLeggings;
            case BOOTS:
                return currentBoots;
        }
        return null;
    }

    /**
     * Removes entire suit.
     */
    public void removeSuit() {
        for (ArmorSlot armorSlot : ArmorSlot.values())
            removeSuit(armorSlot);
    }

    /**
     * Sets current hat.
     *
     * @param hat The new hat.
     */
    public void setHat(Hat hat) {

        removeHat();

        if (getPlayer().getInventory().getHelmet() != null) {
            getPlayer().sendMessage(MessageManager.getMessage("Hats.Must-Remove-Hat"));
            return;
        }

        getPlayer().getInventory().setHelmet(hat.getItemStack());

        getPlayer().sendMessage(MessageManager.getMessage("Hats.Equip")
                .replace("%hatname%",
                        (Main.getInstance().placeholdersHaveColor()) ? hat.getName() : Main.filterColor(hat.getName())));
        currentHat = hat;
    }

    /**
     * Sets Emote.
     *
     * @param emote new Emote.
     */
    public void setEmote(Emote emote) {
        getPlayer().sendMessage(MessageManager.getMessage("Emotes.Equip")
                .replace("%emotename%",
                        (Main.getInstance().placeholdersHaveColor())
                                ? emote.getName() : Main.filterColor(emote.getName())));
        currentEmote = emote;
    }

    /**
     * Clears all gadgets.
     */
    public boolean clear() {
        boolean toReturn = currentGadget != null
                || currentParticleEffect != null
                || currentPet != null
                || currentMount != null
                || currentTreasureChest != null
                || currentHat != null
                || currentEmote != null;
        if (Category.MORPHS.isEnabled() && Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            removeMorph();
            try {
                DisguiseAPI.undisguiseToAll(getPlayer());
            } catch (Exception e) {
            }
        }
        removeGadget();
        removeParticleEffect();
        removePet();
        removeMount();
        removeTreasureChest();
        removeHat();
        removeEmote();
        for (ArmorSlot armorSlot : ArmorSlot.values())
            removeSuit(armorSlot);
        return toReturn;
    }

    /**
     * Opens the Key Purchase Menu.
     */
    public void openKeyPurchaseMenu() {
        if (!Main.getInstance().isVaultLoaded())
            return;
        try {
            final Inventory inventory = Bukkit.createInventory(null, 54, MessageManager.getMessage("Buy-Treasure-Key"));

            for (int i = 27; i < 30; i++) {
                inventory.setItem(i, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 9, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 18, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
                inventory.setItem(i + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
                inventory.setItem(i + 9 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
                inventory.setItem(i + 18 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            }
            ItemStack itemStack = ItemFactory.create(Material.TRIPWIRE_HOOK, (byte) 0, ChatColor.translateAlternateColorCodes('&', ((String) SettingsManager.getMessages().get("Buy-Treasure-Key-ItemName")).replace("%price%", "" + (int) SettingsManager.getConfig().get("TreasureChests.Key-Price"))));
            inventory.setItem(13, itemStack);

            ItemFactory.fillInventory(inventory);

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> getPlayer().openInventory(inventory), 3);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Removes current Particle Effect.
     */
    public void removeParticleEffect() {
        if (currentParticleEffect != null) {
            getPlayer().sendMessage(MessageManager.getMessage("Particle-Effects.Unsummon").replace("%effectname%", (Main.getInstance().placeholdersHaveColor()) ?
                    currentParticleEffect.getType().getName() : Main.filterColor(currentParticleEffect.getType().getName())));
            currentParticleEffect = null;
        }
    }

    /**
     * Removes current Morph.
     */
    public void removeMorph() {
        if (currentMorph != null) {
            DisguiseAPI.undisguiseToAll(getPlayer());
            currentMorph.clear();
            currentMorph = null;
        }
    }

    /**
     * Sets the name of a pet.
     *
     * @param petName The pet name.
     * @param name    The new name.
     */
    public void setPetName(String petName, String name) {
        if (Main.getInstance().usingFileStorage())
            SettingsManager.getData(getPlayer()).set("Pet-Names." + petName, name);
        else Main.db.setPetName(index(), petName, name);
    }

    /**
     * Gets the name of a pet.
     *
     * @param petName The pet.
     * @return The pet name.
     */
    public String getPetName(String petName) {
        try {
            if (Main.getInstance().usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Pet-Names." + petName);
            } else {
                String out = Main.db.getPetName(index(), petName);// always not null
                if (out.isEmpty() || out.equalsIgnoreCase("Unknown")) return null;
                return out;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Gives ammo to player.
     *
     * @param name   The gadget.
     * @param amount The ammo amount to give.
     */
    public void addAmmo(String name, int amount) {
        if (Main.getInstance().isAmmoEnabled())
            if (Main.getInstance().usingFileStorage())
                SettingsManager.getData(getPlayer()).set("Ammo." + name, getAmmo(name) + amount);
            else
                Main.db.addAmmo(index(), name, amount);
        if (currentGadget != null)
            getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"),
                    ItemFactory.create(currentGadget.getMaterial(), currentGadget.getData(),
                            "§f§l" + Main.getUltraPlayer(getPlayer()).getAmmo(currentGadget.getType().toString()
                                    .toLowerCase()) + " " + currentGadget.getName(), MessageManager.getMessage("Gadgets.Lore")));
    }

    /**
     * Sets if player has gadgets enabled.
     *
     * @param enabled if player has gadgets enabled.
     */
    public void setGadgetsEnabled(Boolean enabled) {
        try {
            if (Main.getInstance().usingFileStorage()) {
                SettingsManager.getData(getPlayer()).set("Gadgets-Enabled", enabled);
            } else {
                Main.db.setGadgetsEnabled(index(), enabled);
            }
            if (enabled) {
                getPlayer().sendMessage(MessageManager.getMessage("Enabled-Gadgets"));
            } else {
                getPlayer().sendMessage(MessageManager.getMessage("Disabled-Gadgets"));
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * @return if the player has gadgets enabled or not.
     */
    public boolean hasGadgetsEnabled() {
        // Make sure it won't be affected before load finished, especially for SQL
        if (!loaded)
            return false;

        try {
            if (Main.getInstance().usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Gadgets-Enabled");
            } else {
                return Main.db.hasGadgetsEnabled(index());
            }
        } catch (NullPointerException e) {
            return true;
        }
    }

    /**
     * Sets if a player can see his own morph or not.
     *
     * @param enabled if player should be able to see his own morph.
     */
    public void setSeeSelfMorph(Boolean enabled) {
        if (Main.getInstance().usingFileStorage()) {
            SettingsManager.getData(getPlayer()).set("Third-Person-Morph-View", enabled);
        } else {
            Main.db.setSeeSelfMorph(index(), enabled);
        }
        if (enabled) {
            getPlayer().sendMessage(MessageManager.getMessage("Enabled-SelfMorphView"));
        } else {
            getPlayer().sendMessage(MessageManager.getMessage("Disabled-SelfMorphView"));
        }
    }

    /**
     * @return if player should be able to see his own morph or not.
     */
    public boolean canSeeSelfMorph() {
        // Make sure it won't be affected before load finished, especially for SQL
        if (!loaded)
            return false;
        try {
            if (Main.getInstance().usingFileStorage()) {
                return SettingsManager.getData(getPlayer()).get("Third-Person-Morph-View");
            } else {
                return Main.db.canSeeSelfMorph(index());
            }
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Gets the ammo of a gadget.
     *
     * @param name The gadget.
     * @return The ammo of the given gadget.
     */
    public int getAmmo(String name) {
        if (Main.getInstance().isAmmoEnabled())
            if (Main.getInstance().usingFileStorage())
                return (int) SettingsManager.getData(getPlayer()).get("Ammo." + name);
            else
                return Main.db.getAmmo(index(), name);
        return 0;
    }

    /**
     * Clears current Treasure Chest.
     */
    public void removeTreasureChest() {
        if (currentTreasureChest == null) return;
        this.currentTreasureChest.clear();
        this.currentTreasureChest = null;
    }

    /**
     * Removes One Ammo of a gadget.
     *
     * @param name The gadget.
     */
    public void removeAmmo(String name) {
        if (Main.getInstance().isAmmoEnabled()) {
            if (Main.getInstance().usingFileStorage()) {
                SettingsManager.getData(getPlayer()).set("Ammo." + name, getAmmo(name) - 1);
            } else {
                Main.db.removeAmmo(index(), name);
            }
        }
    }

    public static void giveMenu(Player p) {
        removeMenu(p);
        int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
        if (p.getInventory().getItem(slot) != null) {
            if (p.getInventory().getItem(slot).hasItemMeta()
                    && p.getInventory().getItem(slot).getItemMeta().hasDisplayName()
                    && p.getInventory().getItem(slot).getItemMeta().getDisplayName().equalsIgnoreCase((String) SettingsManager.getConfig().get("Menu-Item.Displayname"))) {
                p.getInventory().remove(slot);
                p.getInventory().setItem(slot, null);
            }
            p.getWorld().dropItemNaturally(p.getLocation(), p.getInventory().getItem(slot));
            p.getInventory().remove(slot);
        }
        String name = String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§");
        Material material = Material.valueOf((String) SettingsManager.getConfig().get("Menu-Item.Type"));
        byte data = Byte.valueOf(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Data")));
        p.getInventory().setItem(slot, ItemFactory.create(material, data, name));
    }

    /**
     * Gives the Menu Item.
     */
    public void giveMenuItem() {
        if (player == null) return;
        giveMenu(player);
    }

    private static void removeMenu(Player player) {
        int slot = SettingsManager.getConfig().getInt("Menu-Item.Slot");
        if (player.getInventory().getItem(slot) != null
                && player.getInventory().getItem(slot).hasItemMeta()
                && player.getInventory().getItem(slot).getItemMeta().hasDisplayName()
                && player.getInventory().getItem(slot).getItemMeta().getDisplayName()
                .equals(String.valueOf(SettingsManager.getConfig().get("Menu-Item.Displayname")).replace("&", "§")))
            player.getInventory().setItem(slot, null);
    }

    /**
     * Removes the menu Item.
     */
    public void removeMenuItem() {
        if (player == null)
            return;
        removeMenu(player);
    }

    /**
     * Gets the UUID.
     *
     * @return The UUID.
     */
    public UUID id() {
        return id;
    }

    public int index() {
        return getIndexId(player);
    }

    public static int getIndexId(OfflinePlayer p) {
        Integer i = INDEX.get(p.getUniqueId());
        if ($.nil(i)) {
            try (SelectQuery.Binding b = Main.db.query().select("id").where("uuid", p.getUniqueId() + "").execute()) {
                i = b.getResult().getInt("id");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            INDEX.put(p.getUniqueId(), i);
        }
        return $.valid(i, -1);
    }

    public static void putIndexId(OfflinePlayer p, int i) {
        INDEX.put(p.getUniqueId(), i);
    }

}