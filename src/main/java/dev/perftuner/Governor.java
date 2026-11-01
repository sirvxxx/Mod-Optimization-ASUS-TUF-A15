package dev.perftuner;

/**
 * Adaptive governor: memantau FPS dan menurunkan / menaikkan "level tekanan" (0..3) yang
 * memperkecil jarak culling dan budget partikel secara otomatis. Hanya bekerja di sisi software mod
 * (tanpa mengubah render distance, jadi tidak memicu reload chunk).
 */
public final class Governor {
    private Governor() {}

    private static final double[] DIST = {1.00, 0.85, 0.70, 0.55};
    private static final double[] PART = {1.00, 0.60, 0.35, 0.15};
    private static final int MAX_LEVEL = 3;

    private static int frames, tickInWindow, level, lowSecs, highSecs, fps;
    private static long windowStartNs = System.nanoTime();
    /** Seam untuk pengujian; produksi memakai System.nanoTime. */
    static java.util.function.LongSupplier clock = System::nanoTime;

    /** Dipanggil setiap frame render (lewat HUD element tanpa gambar). */
    public static void onFrame() { frames++; }

    public static void onTick() {
        if (++tickInWindow < 20) return;
        tickInWindow = 0;
        long now = clock.getAsLong();
        double secs = (now - windowStartNs) / 1_000_000_000.0;
        windowStartNs = now;
        int f = frames;
        frames = 0;
        if (f == 0 || secs <= 0.0) return;       // HUD disembunyikan (F1) atau game di-pause: jangan salah baca
        fps = (int) Math.round(f / secs);

        PerfConfig c = PerfConfig.get();
        if (!c.governor) { level = 0; lowSecs = highSecs = 0; return; }

        if (fps < c.targetFps * 0.9)       { lowSecs++; highSecs = 0; }
        else if (fps > c.targetFps * 1.35) { highSecs++; lowSecs = 0; }
        else                               { lowSecs = highSecs = 0; }

        if (lowSecs >= 3 && level < MAX_LEVEL) { level++; lowSecs = 0; }
        if (highSecs >= 8 && level > 0)        { level--; highSecs = 0; }
    }

    public static void reset() { level = 0; lowSecs = highSecs = 0; }

    public static double distanceScale() { return DIST[level]; }
    public static double particleScale() { return PART[level]; }
    public static int level() { return level; }
    public static int fps() { return fps; }
}
