package dev.perftuner;

/** Membatasi jumlah partikel BARU per tick (ledakan / pertarungan massal). Budget turun otomatis oleh Governor. */
public final class ParticleBudget {
    private ParticleBudget() {}

    private static int budget;      // 0 = tanpa batas
    private static int spawned;

    public static void onTick() {
        spawned = 0;
        PerfConfig c = PerfConfig.get();
        budget = (c.particleLimit && c.particleBudget > 0)
                ? Math.max(20, (int) (c.particleBudget * Governor.particleScale()))
                : 0;
    }

    /** true = partikel boleh dibuat. Dipanggil dari ParticleEngineMixin. */
    public static boolean tryAcquire() {
        Stats.hookParticle = true;
        if (budget <= 0) return true;
        if (spawned < budget) { spawned++; return true; }
        Stats.particlesDropped++;
        return false;
    }

    public static int currentBudget() { return budget; }
}
