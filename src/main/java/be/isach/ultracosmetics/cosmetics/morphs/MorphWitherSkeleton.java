package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.MathUtils;
import be.isach.ultracosmetics.util.SoundUtil;
import be.isach.ultracosmetics.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Sacha on 18/10/15.
 */
public class MorphWitherSkeleton extends Morph {

    boolean inCooldown;

    public MorphWitherSkeleton(UUID owner) {
        super(owner, MorphType.WITHERSKELETON);
        Main.getInstance().registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer() == getPlayer()
                && !inCooldown) {
            inCooldown = true;
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    inCooldown = false;
                }
            }, 200);
            for (Entity ent : getPlayer().getNearbyEntities(3, 3, 3)) {
                if (ent instanceof Player || ent instanceof Creature)
                    MathUtils.applyVelocity(ent, ent.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).setY(1));
            }
            final List<Entity> items = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Item bone = getPlayer().getWorld().dropItem(getPlayer().getLocation().add(Math.random() * 5.0D - 2.5D, Math.random() * 3.0D, Math.random() * 5.0D - 2.5D), ItemFactory.create(Material.BONE, (byte) 0, UUID.randomUUID().toString()));
                bone.setVelocity(MathUtils.getRandomVector());
                items.add(bone);
            }
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    for (Entity bone : items)
                        bone.remove();
                    items.clear();
                }
            }, 50);
            SoundUtil.playSound(getPlayer(), Sounds.SKELETON_HURT, 0.4f, (float) Math.random() + 1f);
        }
    }
}
