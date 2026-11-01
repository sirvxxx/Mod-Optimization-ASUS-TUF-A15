package dev.perftuner;

/** Penghitung ringan (tanpa alokasi per panggilan). Ringkasan teks hanya dibangun 1x per detik. */
public final class Stats {
    private Stats() {}

    public static long tick;
    public static long entitiesCulled, blockEntitiesCulled, particlesDropped;

    // Apakah tiap hook mixin benar-benar terpanggil (false = target tidak ditemukan di versi ini)
    public static boolean hookEntity, hookBlockEntity, hookParticle;

    private static long lastE, lastB, lastP;
    private static int secTicks;
    private static String summary = "-";

    public static String summary() { return summary; }

    public static void onTick() {
        tick++;
        if (++secTicks < 20) return;
        secTicks = 0;
        summary = "Culled/s  Entity: " + (entitiesCulled - lastE)
                + "   BlockEntity: " + (blockEntitiesCulled - lastB)
                + "   Particle: " + (particlesDropped - lastP);
        lastE = entitiesCulled; lastB = blockEntitiesCulled; lastP = particlesDropped;
    }
}
