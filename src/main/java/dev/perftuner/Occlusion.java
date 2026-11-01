package dev.perftuner;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

/**
 * Deteksi entity tertutup tembok: 3 raycast (kepala, tengah, kaki) dari kamera.
 * Hanya jika SEMUA tertutup, entity dianggap terhalang. Hasil di-cache per entity selama beberapa tick
 * (cache direct-mapped berbasis array primitif, tanpa alokasi, tanpa boxing).
 */
final class Occlusion {
    private Occlusion() {}

    private static final int SIZE = 4096, MASK = SIZE - 1;
    private static final int TTL_TICKS = 5;
    private static final int[] ids = new int[SIZE];
    private static final long[] stamps = new long[SIZE];     // (tick << 1) | (occluded ? 1 : 0)

    static { Arrays.fill(stamps, -1L); }

    static int rayCount;     // untuk pengujian / statistik

    static boolean isOccluded(Entity e, double cx, double cy, double cz) {
        int id = e.getId();
        int slot = id & MASK;
        long s = stamps[slot];
        if (s != -1L && ids[slot] == id && Stats.tick - (s >>> 1) < TTL_TICKS) {
            return (s & 1L) != 0L;
        }
        boolean occ = probe(e, cx, cy, cz);
        ids[slot] = id;
        stamps[slot] = (Stats.tick << 1) | (occ ? 1L : 0L);
        return occ;
    }

    private static boolean probe(Entity e, double cx, double cy, double cz) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.player == null) return false;
        double x = e.getX(), y = e.getY(), z = e.getZ(), h = e.getBbHeight();
        Vec3 from = new Vec3(cx, cy, cz);
        return blocked(level, mc.player, from, x, y + h * 0.9, z)
            && blocked(level, mc.player, from, x, y + h * 0.5, z)
            && blocked(level, mc.player, from, x, y + 0.15, z);
    }

    private static boolean blocked(Level level, Entity who, Vec3 from, double x, double y, double z) {
        rayCount++;
        HitResult r = level.clip(new ClipContext(from, new Vec3(x, y, z),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, who));
        return r.getType() != HitResult.Type.MISS;
    }
}
