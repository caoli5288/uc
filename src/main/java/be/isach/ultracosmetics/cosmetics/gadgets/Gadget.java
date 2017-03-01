package be.isach.ultracosmetics.cosmetics.gadgets;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.menu.GadgetManager;
import be.isach.ultracosmetics.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Created by sacha on 03/08/15.
 */
public abstract class Gadget implements Listener {

    /**
     * If true, it will differentiate left and right click.
     */
    public boolean useTwoInteractMethods;
    /**
     * If it should open Gadget Menu after purchase.
     */
    public boolean openGadgetsInvAfterAmmo;
    /**
     * If true, will display cooldown left when fail on use
     * because cooldown active.
     */
    public boolean displayCooldownMessage = true;

    public int lastPage = 1;
    /**
     * Last Clicked Block by the player.
     */
    protected Block lastClickedBlock;
    /**
     * Gadget ItemStack.
     */
    protected ItemStack itemStack;
    /**
     * If Gadget interaction should run asynchronously.
     */
    protected boolean asyncAction = false;
    /**
     * If true, it will affect players (velocity).
     */
    boolean affectPlayers;
    /**
     * The Ammo Purchase inventory.
     */
    private Inventory inv;
    /**
     * Event listener.
     */
    private Listener listener;
    /**
     * Type of the Gadget.
     */
    private GadgetType type;
    /**
     * Required permission.
     */
    private String permission;
    /**
     * Owner's UUID.
     */
    private UUID owner;

    public Gadget(final UUID owner, final GadgetType type) {
        this.permission = type.getPermission();
        this.type = type;
        this.affectPlayers = type.affectPlayers();
        if (!type.isEnabled())
            return;

        this.useTwoInteractMethods = false;
        if (owner != null) {
            this.owner = owner;
            if (Main.getUltraPlayer(getPlayer()).currentGadget != null)
                Main.getUltraPlayer(getPlayer()).removeGadget();
            if (!getPlayer().hasPermission(permission)) {
                getPlayer().sendMessage(MessageManager.getMessage("No-Permission"));
                return;
            }
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator('.');
            otherSymbols.setPatternSeparator('.');
            final DecimalFormat decimalFormat = new DecimalFormat("0.0", otherSymbols);
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (Bukkit.getPlayer(owner) != null
                                && Main.getUltraPlayer(Bukkit.getPlayer(owner)).currentGadget != null
                                && Main.getUltraPlayer(Bukkit.getPlayer(owner)).currentGadget.getType() == type) {
                            onUpdate();
                            if (Main.cooldownInBar) {
                                if (getPlayer().getItemInHand() != null
                                        && itemStack != null
                                        && getPlayer().getItemInHand().hasItemMeta()
                                        && getPlayer().getItemInHand().getItemMeta().hasDisplayName()
                                        && getPlayer().getItemInHand().getItemMeta().getDisplayName().contains(getType().getName())
                                        && Main.getUltraPlayer(getPlayer()).canUse(type) != -1)
                                    sendCooldownBar();
                                double left = Main.getUltraPlayer(getPlayer()).canUse(type);
                                if (left > -0.1) {
                                    String leftRounded = decimalFormat.format(left);
                                    double decimalRoundedValue = Double.parseDouble(leftRounded);
                                    if (decimalRoundedValue == 0) {
                                        PlayerUtils.sendInActionBar(getPlayer(),
                                                MessageManager.getMessage("Gadgets.Gadget-Ready-ActionBar").
                                                        replace("%gadgetname%", (Main.getInstance().placeholdersHaveColor()) ?
                                                                getName() : Main.filterColor(getName())));
                                        SoundUtil.playSound(getPlayer(), Sounds.NOTE_STICKS, 1.4f, 1.5f);
                                    }
                                }
                            }
                        } else {
                            cancel();
                            unregisterListeners();
                        }
                    } catch (NullPointerException exc) {
                        removeItem();
                        onClear();
                        removeListener();
                        getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Unequip").replace("%gadgetname%", (Main.getInstance().placeholdersHaveColor()) ? getName() : Main.filterColor(getName())));
                        cancel();
                    }
                }
            };
            runnable.runTaskTimerAsynchronously(Main.getInstance(), 0, 10);
            listener = new GadgetListener(this);
            Main.getInstance().registerListener(listener);
            Main.getInstance().registerListener(this);
            if (getPlayer().getInventory().getItem((int) SettingsManager.getConfig().get("Gadget-Slot")) != null) {
                getPlayer().getWorld().dropItem(getPlayer().getLocation(), getPlayer().getInventory().getItem((int) SettingsManager.getConfig().get("Gadget-Slot")));
                getPlayer().getInventory().remove((int) SettingsManager.getConfig().get("Gadget-Slot"));
            }
            String d = Main.getInstance().isAmmoEnabled() && getType().requiresAmmo() ?
                    "§f§l" + Main.getUltraPlayer(getPlayer()).getAmmo(type.toString().toLowerCase()) + " "
                    : "";
            itemStack = ItemFactory.create(type.getMaterial(), type.getData(), d + getName(), MessageManager.getMessage("Gadgets.Lore"));
            getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), itemStack);
            getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Equip").replace("%gadgetname%", (Main.getInstance().placeholdersHaveColor()) ? getName() : Main.filterColor(getName())));
            Main.getUltraPlayer(getPlayer()).currentGadget = this;
        }
    }

    /**
     * Unregister Listener.
     */
    public void removeListener() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Sends the current cooldown in action bar.
     */
    private void sendCooldownBar() {
        if (getPlayer() == null) return;

        StringBuilder stringBuilder = new StringBuilder();

        double currentCooldown = Main.getUltraPlayer(getPlayer()).canUse(type);
        double maxCooldown = type.getCountdown();

        int res = (int) (currentCooldown / maxCooldown * 10);
        ChatColor color;
        for (int i = 0; i < 10; i++) {
            color = ChatColor.RED;
            if (i < 10 - res)
                color = ChatColor.GREEN;
            stringBuilder.append(color + "█");
        }

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator('.');
        otherSymbols.setPatternSeparator('.');
        final DecimalFormat decimalFormat = new DecimalFormat("0.0", otherSymbols);
        String timeLeft = decimalFormat.format(currentCooldown) + "s";

        PlayerUtils.sendInActionBar(getPlayer(),
                getName() + " §f" + stringBuilder.toString() + " §f" + timeLeft);

    }

    public String getName() {
        return type.getName();
    }

    public Material getMaterial() {
        return type.getMaterial();
    }

    public GadgetType getType() {
        return type;
    }

    public Byte getData() {
        return type.getData();
    }

    /**
     * If useTwoInteractMethods is true,
     * called when only a right click is called.
     * <p/>
     * Otherwise, called when a right or left click
     * is performed.
     */
    abstract void onRightClick();

    /**
     * Called when a left click is done with gadget,
     * only called if useTwoInteractMethods is true.
     */
    abstract void onLeftClick();

    /**
     * Called on each tick.
     */
    abstract void onUpdate();

    /**
     * Called when gadget is cleared.
     */
    public abstract void onClear();

    /**
     * unregister listeners.
     */
    public void unregisterListeners() {
        try {
            HandlerList.unregisterAll(this);
            HandlerList.unregisterAll(listener);
        } catch (Exception exc) {
        }
    }

    /**
     * Gets the owner as a UUID.
     *
     * @return the owner as a UUID.
     */
    protected UUID getOwner() {
        return owner;
    }

    /**
     * Gets the owner as a player.
     *
     * @return the owner as a player.
     */
    protected Player getPlayer() {
        return Bukkit.getPlayer(owner);
    }

    /**
     * Removes the item.
     */
    public void removeItem() {
        itemStack = null;
        getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), null);
    }

    /**
     * Gets the price for each ammo purchase.
     *
     * @return the price for each ammo purchase.
     */
    public int getPrice() {
        return (int) SettingsManager.getConfig().get("Gadgets." + type.getConfigName() + ".Ammo.Price");
    }

    /**
     * Gets the ammo it should give after a purchase.
     *
     * @return the ammo it should give after a purchase.
     */
    public int getResultAmmoAmount() {
        return (int) SettingsManager.getConfig().get("Gadgets." + type.getConfigName() + ".Ammo.Result-Amount");
    }

    /**
     * Gets the gadget current Item Stack.
     *
     * @return
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Opens Ammo Purchase Menu.
     */
    public void openAmmoPurchaseMenu() {

        Inventory inventory = Bukkit.createInventory(null, 54, MessageManager.getMessage("Menus.Buy-Ammo"));

        inventory.setItem(13, ItemFactory.create(type.getMaterial(), type.getData(), MessageManager.getMessage("Buy-Ammo-Description").replace("%amount%", "" + getResultAmmoAmount()).replace("%price%", "" + getPrice()).replaceAll("%gadgetname%", getName())));

        for (int i = 27; i < 30; i++) {
            inventory.setItem(i, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 9, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 18, ItemFactory.create(Material.EMERALD_BLOCK, (byte) 0x0, MessageManager.getMessage("Purchase")));
            inventory.setItem(i + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            inventory.setItem(i + 9 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
            inventory.setItem(i + 18 + 6, ItemFactory.create(Material.REDSTONE_BLOCK, (byte) 0x0, MessageManager.getMessage("Cancel")));
        }
        ItemFactory.fillInventory(inventory);


        getPlayer().openInventory(inventory);

        this.inv = inventory;
    }

    /**
     * Event Listener.
     */
    public class GadgetListener implements Listener {
        private Gadget gadget;

        public GadgetListener(Gadget gadget) {
            this.gadget = gadget;
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            if (event.getPlayer() == getPlayer() && inv != null && isSameInventory(event.getInventory(), inv)) {
                inv = null;
                openGadgetsInvAfterAmmo = false;
                return;
            }
        }

        @EventHandler
        public void onInventoryClickAmmo(final InventoryClickEvent event) {
            if (event.getWhoClicked() == getPlayer() && inv != null && isSameInventory(event.getWhoClicked().getOpenInventory().getTopInventory(), inv)) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
                    String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                    String purchase = MessageManager.getMessage("Purchase");
                    String cancel = MessageManager.getMessage("Cancel");
                    if (displayName.equals(purchase)) {
                        if (Main.getUltraPlayer((Player) event.getWhoClicked()).getBalance() >= getPrice()) {
                            Main.economy.withdrawPlayer((Player) event.getWhoClicked(), getPrice());
                            Main.getUltraPlayer((Player) event.getWhoClicked()).addAmmo(type.toString().toLowerCase(), getResultAmmoAmount());
                            event.getWhoClicked().sendMessage(MessageManager.getMessage("Successful-Purchase"));
                            if (openGadgetsInvAfterAmmo)
                                Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        GadgetManager.openMenu((Player) event.getWhoClicked(), lastPage);
                                        openGadgetsInvAfterAmmo = false;
                                        lastPage = 1;
                                    }
                                }, 1);
                        } else {
                            getPlayer().sendMessage(MessageManager.getMessage("Not-Enough-Money"));
                        }
                        event.getWhoClicked().closeInventory();
                    } else if (displayName.equals(cancel)) {
                        event.getWhoClicked().closeInventory();
                    }
                }
            }
        }

        public boolean isSameInventory(Inventory first, Inventory second) {
            return Main.getInstance().getEntityUtil().isSameInventory(first, second);
        }

        @EventHandler
        protected void onPlayerInteract(final PlayerInteractEvent EVENT) {
            Player player = EVENT.getPlayer();
            UUID uuid = player.getUniqueId();
            UltraPlayer cp = Main.getUltraPlayer(getPlayer());
            if (!uuid.equals(gadget.owner)) return;
            ItemStack itemStack = player.getItemInHand();
            if (itemStack.getType() != gadget.getMaterial()) return;
            if (itemStack.getData().getData() != gadget.getData()) return;
            if (player.getInventory().getHeldItemSlot() != (int) SettingsManager.getConfig().get("Gadget-Slot")) return;
            if (Main.getUltraPlayer(getPlayer()).currentGadget != gadget) return;
            if (EVENT.getAction() == Action.PHYSICAL) return;
            EVENT.setCancelled(true);
            player.updateInventory();
            if (!Main.getUltraPlayer(getPlayer()).hasGadgetsEnabled()) {
                getPlayer().sendMessage(MessageManager.getMessage("Gadgets-Enabled-Needed"));
                return;
            }
            if (Main.getUltraPlayer(getPlayer()).currentTreasureChest != null)
                return;

            if (Main.getInstance().isAmmoEnabled() && getType().requiresAmmo()) {
                if (Main.getUltraPlayer(getPlayer()).getAmmo(getType().toString().toLowerCase()) < 1) {
                    openAmmoPurchaseMenu();
                    return;
                }
            }
            if (type == GadgetType.PORTALGUN) {
                if (getPlayer().getTargetBlock((Set<Material>) null, 20).getType() == Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.PortalGun.No-Block-Range"));
                    return;
                }
            }
            if (type == GadgetType.ROCKET) {
                boolean pathClear = true;
                Cuboid c = new Cuboid(getPlayer().getLocation().add(-1, 0, -1), getPlayer().getLocation().add(1, 75, 1));
                if (!c.isEmpty()) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-Enough-Space"));
                    return;
                }
                if (!getPlayer().isOnGround()) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-On-Ground"));
                    return;
                }
            }
            if (type == GadgetType.DISCOBALL) {
                if (Main.getInstance().discoBalls.size() > 0) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.DiscoBall.Already-Active"));
                    return;
                }
                if (getPlayer().getLocation().add(0, 4, 0).getBlock() != null && getPlayer().getLocation().add(0, 4, 0).getBlock().getType() != Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.DiscoBall.Not-Space-Above"));
                    return;
                }
            }
            if (type == GadgetType.CHRISTMASTREE) {
                if (EVENT.getClickedBlock() == null
                        || EVENT.getClickedBlock().getType() == Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.ChristmasTree.Click-On-Block"));
                    return;
                }
            }
            if (type == GadgetType.TRAMPOLINE) {
                // Check blocks above.
                Location loc1 = getPlayer().getLocation().add(2, 15, 2);
                Location loc2 = getPlayer().getLocation().clone().add(-2, 0, -2);
                Block block = loc1.getBlock().getRelative(3, 0, 0);
                Block block2 = loc1.getBlock().getRelative(3, 1, 0);
                Cuboid checkCuboid = new Cuboid(loc1, loc2);

                if (!checkCuboid.isEmpty()
                        || block.getType() != Material.AIR
                        || block2.getType() != Material.AIR) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-Enough-Space"));
                    return;
                }
            }
            // Check for the parachute if there is space 30-40 blocks above the player to avoid problems.
            if (type == GadgetType.PARACHUTE) {
                // Check blocks above.
                Location loc1 = getPlayer().getLocation().add(2, 28, 2);
                Location loc2 = getPlayer().getLocation().clone().add(-2, 40, -2);
                Cuboid checkCuboid = new Cuboid(loc1, loc2);

                if (!checkCuboid.isEmpty()) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Rocket.Not-Enough-Space"));
                    return;
                }
            }
            if (type == GadgetType.EXPLOSIVESHEEP) {
                if (Main.getInstance().explosiveSheep.size() > 0) {
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.ExplosiveSheep.Already-Active"));
                    return;
                }
            }
            double coolDown = cp.canUse(getType());
            if (coolDown != -1) {
                String timeLeft = new DecimalFormat("#.#").format(coolDown);
                if (type.getCountdown() > 1)
                    getPlayer().sendMessage(MessageManager.getMessage("Gadgets.Countdown-Message").replace("%gadgetname%", (Main.getInstance().placeholdersHaveColor()) ? getName() : Main.filterColor(getName())).replace("%time%", timeLeft));
                return;
            } else
                cp.setCoolDown(getType(), type.getCountdown());
            if (Main.getInstance().isAmmoEnabled() && getType().requiresAmmo()) {
                Main.getUltraPlayer(getPlayer()).removeAmmo(getType().toString().toLowerCase());
                itemStack = ItemFactory.create(type.getMaterial(), type.getData(), "§f§l" + Main.getUltraPlayer(getPlayer()).getAmmo(type.toString().toLowerCase()) + " " + getName(), MessageManager.getMessage("Gadgets.Lore"));
                getPlayer().getInventory().setItem((int) SettingsManager.getConfig().get("Gadget-Slot"), itemStack);
            }
            if (EVENT.getClickedBlock() != null
                    && EVENT.getClickedBlock().getType() != Material.AIR)
                lastClickedBlock = EVENT.getClickedBlock();
            if (asyncAction) {
                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (useTwoInteractMethods) {
                            if (EVENT.getAction() == Action.RIGHT_CLICK_AIR
                                    || EVENT.getAction() == Action.RIGHT_CLICK_BLOCK)
                                onRightClick();
                            else if (EVENT.getAction() == Action.LEFT_CLICK_BLOCK
                                    || EVENT.getAction() == Action.LEFT_CLICK_AIR)
                                onLeftClick();
                        } else {
                            onRightClick();
                        }
                    }
                });
            } else {
                if (useTwoInteractMethods) {
                    if (EVENT.getAction() == Action.RIGHT_CLICK_AIR
                            || EVENT.getAction() == Action.RIGHT_CLICK_BLOCK)
                        onRightClick();
                    else if (EVENT.getAction() == Action.LEFT_CLICK_BLOCK
                            || EVENT.getAction() == Action.LEFT_CLICK_AIR)
                        onLeftClick();
                } else {
                    onRightClick();
                }
            }

        }

        @EventHandler
        protected void onItemDrop(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack().getType() == type.getMaterial()) {
                if (event.getItemDrop().getItemStack().getData().getData() == type.getData()) {
                    if (event.getItemDrop().getItemStack().getItemMeta().hasDisplayName()) {
                        if (event.getItemDrop().getItemStack().getItemMeta().getDisplayName().endsWith(getName())) {
                            if (SettingsManager.getConfig().getBoolean("Remove-Gadget-With-Drop")) {
                                Main.getUltraPlayer(getPlayer()).removeGadget();
                                event.getItemDrop().remove();
                                return;
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        /**
         * Cancel players from removing, picking the item in their inventory.
         *
         * @param event
         */
        @EventHandler(priority = EventPriority.LOWEST)
        public void cancelMove(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            if (player == getPlayer()
                    && ((event.getCurrentItem() != null && event.getCurrentItem().equals(gadget.getItemStack())))
                    || ((event.getCursor() != null && event.getCursor().equals(gadget.getItemStack())))) {
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT
                        || event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.UNKNOWN) {
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
                if (event.getCurrentItem() != null) {
                    if (event.getCurrentItem().equals(itemStack)) {
                        event.setCancelled(true);
                        player.updateInventory();
                        return;
                    }
                }
            }
        }

        /**
         * Cancel players from removing, picking the item in their inventory.
         *
         * @param event
         */
        @EventHandler
        public void cancelMove(InventoryDragEvent event) {
            Player player = (Player) event.getWhoClicked();
            for (ItemStack item : event.getNewItems().values()) {
                if (item != null
                        && player == getPlayer()
                        && item.equals(itemStack)) {
                    event.setCancelled(true);
                    ((Player) event.getWhoClicked()).updateInventory();
                    return;
                }
            }
        }
    }
}
