package be.isach.ultracosmetics.cosmetics.mounts;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.util.PlayerUtils;

import java.util.UUID;

public class MountHypeCart extends Mount {

    public MountHypeCart(UUID owner, Main ultraCosmetics) {
        super(owner, MountType.HYPECART, ultraCosmetics);
    }

    @Override
    protected void onUpdate() {
        if (entity.isOnGround())
            entity.setVelocity(PlayerUtils.getHorizontalDirection(getPlayer(), 7.6));
        Main.getInstance().getEntityUtil().setClimb(entity);
    }

    @Override
    public void onClear() {
    }
}
