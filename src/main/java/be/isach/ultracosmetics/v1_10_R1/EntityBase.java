package be.isach.ultracosmetics.v1_10_R1;

import be.isach.ultracosmetics.v1_10_R1.nms.WrapperEntityHuman;
import be.isach.ultracosmetics.v1_10_R1.nms.WrapperEntityInsentient;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.MathHelper;

/**
 * By MinusKube, THANKS!
 */
public interface EntityBase {

    void g_(float sideMot, float forMot);

    float getSpeed();
    boolean canFly();

}
