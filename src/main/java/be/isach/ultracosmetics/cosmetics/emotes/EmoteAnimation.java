package be.isach.ultracosmetics.cosmetics.emotes;

import be.isach.ultracosmetics.Main;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Project: UltraCosmetics
 * Package: be.isach.ultracosmetics.cosmetics.emotes
 * Created by: Sacha
 * Created on: 17th June, 2016
 * at 02:48
 */
class EmoteAnimation extends BukkitRunnable {

    private static final int INTERVAL_BETWEEN_REPLAY = 20;

    private int ticks, ticksPerFrame, currentFrame, intervalTick;
    private Emote emote;
    private boolean running, up = true;

    EmoteAnimation(int ticksPerFrame, Emote emote) {
        this.ticksPerFrame = ticksPerFrame;
        this.emote = emote;
        this.ticks = 0;
        this.running = false;
    }

    @Override
    public void run() {
        if (ticks < ticksPerFrame) {
            ticks++;
        } else {
            ticks = 0;
            updateTexture();
        }
    }

    void start() {
        this.running = true;
        runTaskTimer(Main.getInstance(), 0, ticksPerFrame);
    }

    public void pause() {
        this.running = !running;
    }

    void stop() {
        this.running = false;
        cancel();
    }

    private void updateTexture() {
        if (!running) return;

        emote.getPlayer().getInventory().setHelmet(getType().getFrames().get(currentFrame));
        emote.setItemStack(getType().getFrames().get(currentFrame));

        if (up) {
            if (currentFrame >= getType().getMaxFrames() - 1) {
                up = false;
            } else {
                currentFrame++;
            }
        } else {
            if (currentFrame <= 0) {
                if (intervalTick >= INTERVAL_BETWEEN_REPLAY / ticksPerFrame) {
                    up = true;
                    intervalTick = 0;
                } else {
                    intervalTick++;
                }
            } else {
                currentFrame--;
            }
        }
    }

    private EmoteType getType() {
        return emote.getEmoteType();
    }
}
