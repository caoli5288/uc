package be.isach.ultracosmetics.v1_8_R3.mount;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.mounts.IMountCustomEntity;
import be.isach.ultracosmetics.cosmetics.mounts.Mount;
import be.isach.ultracosmetics.cosmetics.mounts.MountType;
import be.isach.ultracosmetics.util.EntitySpawningManager;
import be.isach.ultracosmetics.v1_8_R3.customentities.CustomEntities;
import be.isach.ultracosmetics.v1_8_R3.customentities.CustomSlime;
import be.isach.ultracosmetics.v1_8_R3.customentities.FlyingSquid;
import be.isach.ultracosmetics.v1_8_R3.customentities.RideableSpider;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Created by Sacha on 15/03/16.
 */
public abstract class MountCustomEntity extends Mount {

    /**
     * Custom Entity.
     */
    public IMountCustomEntity customEntity;

    public MountCustomEntity(UUID owner, MountType type, Main ultraCosmetics) {
        super(owner, type, ultraCosmetics);
    }

    @Override
    public void equip() {
        if (getType() == MountType.SKYSQUID)
            customEntity = new FlyingSquid(((CraftPlayer) getPlayer()).getHandle().getWorld());
        else if (getType() == MountType.SLIME)
            customEntity = new CustomSlime(((CraftPlayer) getPlayer()).getHandle().getWorld());
        else if (getType() == MountType.SPIDER)
            customEntity = new RideableSpider(((CraftPlayer) getPlayer()).getHandle().getWorld());
        double x = getPlayer().getLocation().getX();
        double y = getPlayer().getLocation().getY();
        double z = getPlayer().getLocation().getZ();
        getCustomEntity().setLocation(x, y + 2, z, 0, 0);

        EntitySpawningManager.setBypass(true);
        ((CraftWorld) getPlayer().getWorld()).getHandle().addEntity(getCustomEntity());
        EntitySpawningManager.setBypass(false);
        Main.getInstance().getEntityUtil().setPassenger(getEntity(), getPlayer());
        CustomEntities.customEntities.add(getCustomEntity());
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (getEntity().getPassenger() != getPlayer() && getCustomEntity().ticksLived > 10) {
                        clear();
                        cancel();
                        return;
                    }
                    if (!getCustomEntity().valid) {
                        cancel();
                        return;
                    }
                    if (owner != null
                            && Bukkit.getPlayer(owner) != null
                            && Main.getUltraPlayer(Bukkit.getPlayer(owner)).currentMount != null
                            && Main.getUltraPlayer(Bukkit.getPlayer(owner)).currentMount.getType() == getType()) {
                        onUpdate();
                    } else {
                        cancel();
                    }

                } catch (NullPointerException exc) {
                    clear();
                    cancel();
                }
            }
        };
        runnable.runTaskTimerAsynchronously(Main.getInstance(), 0, repeatDelay);
        listener = new MountListener(this);

        getPlayer().sendMessage(MessageManager.getMessage("Mounts.Spawn").replace("%mountname%", (Main.getInstance().placeHolderColor) ? getType().getMenuName() : Main.filterColor(getType().getMenuName())));
        Main.getUltraPlayer(getPlayer()).currentMount = this;
    }

    @Override
    protected void removeEntity() {
        getCustomEntity().dead = true;
        CustomEntities.customEntities.remove(customEntity);
    }


    @Override
    public org.bukkit.entity.Entity getEntity() {
        return customEntity.getEntity();
    }

    public Entity getCustomEntity() {
        return ((CraftEntity) customEntity.getEntity()).getHandle();
    }
}
