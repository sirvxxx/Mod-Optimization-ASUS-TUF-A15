package dev.perftuner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Keputusan culling. Dipanggil dari mixin. Tidak mengalokasikan objek pada jalur umum
 * (hanya raycast occlusion yang membuat beberapa objek kecil, itupun di-cache beberapa tick).
 */
public final class CullLogic {
    private CullLogic() {}

    private static final double NEAR_SQ = 12.0 * 12.0;   // entity dekat tidak pernah di-cull

    public static boolean shouldRenderEntity(Entity e, double cx, double cy, double cz) {
        Stats.hookEntity = true;
        PerfConfig c = PerfConfig.get();
        if (!c.entityCulling) return true;
        if (e instanceof Player || e.isPassenger() || e.isVehicle() || e.isCurrentlyGlowing()) return true;

        double dx = e.getX() - cx, dy = e.getY() - cy, dz = e.getZ() - cz;
        double d2 = dx * dx + dy * dy + dz * dz;
        if (d2 < NEAR_SQ) return true;

        int limit = (e instanceof ItemEntity || e instanceof ExperienceOrb) ? c.itemDistance
                  : (e instanceof ArmorStand) ? c.armorStandDistance
                  : c.entityDistance;
        if (limit > 0) {
            double l = limit * Governor.distanceScale();
            if (d2 > l * l) { Stats.entitiesCulled++; return false; }
        }

        if (c.occlusionCulling) {
            double r = c.occlusionRange;
            if (d2 <= r * r && Occlusion.isOccluded(e, cx, cy, cz)) { Stats.entitiesCulled++; return false; }
        }
        return true;
    }

    public static boolean shouldRenderBlockEntity(BlockEntity be) {
        Stats.hookBlockEntity = true;
        PerfConfig c = PerfConfig.get();
        if (!c.blockEntityCulling || be instanceof BeaconBlockEntity) return true;   // beam beacon terlihat jauh
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return true;

        int limit = (be instanceof SignBlockEntity) ? c.signDistance : c.blockEntityDistance;
        if (limit <= 0) return true;

        BlockPos pos = be.getBlockPos();
        double dx = pos.getX() + 0.5 - p.getX(), dy = pos.getY() + 0.5 - p.getY(), dz = pos.getZ() + 0.5 - p.getZ();
        double l = limit * Governor.distanceScale();
        if (dx * dx + dy * dy + dz * dz > l * l) { Stats.blockEntitiesCulled++; return false; }
        return true;
    }
}
