package dev.perftuner;

/** Level optimisasi. CUSTOM dipakai otomatis saat pemain mengubah pengaturan manual. */
public enum Preset {
    BALANCED, ULTRA_FPS, QUALITY, CUSTOM;

    /** Urutan siklus tombol: CUSTOM dilewati (hanya muncul otomatis). */
    public Preset next() {
        return switch (this) {
            case BALANCED -> ULTRA_FPS;
            case ULTRA_FPS -> QUALITY;
            default -> BALANCED;
        };
    }

    public String langKey() { return "custom_perf_tuner.preset." + name().toLowerCase(java.util.Locale.ROOT); }
}
