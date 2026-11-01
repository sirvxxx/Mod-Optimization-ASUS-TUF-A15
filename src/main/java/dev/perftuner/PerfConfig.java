package dev.perftuner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Config JSON: .minecraft/config/custom_perf_tuner.json. 0 pada jarak/budget berarti "nonaktif / tanpa batas". */
public final class PerfConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static PerfConfig instance = new PerfConfig();

    public Preset preset = Preset.BALANCED;
    /** Saat preset dipilih di menu, ikut menerapkan opsi video vanilla (render distance, partikel, dll). */
    public boolean applyVanillaOptions = true;

    // --- Entity culling
    public boolean entityCulling = true;
    public int entityDistance = 96;
    public int itemDistance = 40;
    public int armorStandDistance = 48;
    public boolean occlusionCulling = false;
    public int occlusionRange = 64;

    // --- Block entity culling
    public boolean blockEntityCulling = true;
    public int blockEntityDistance = 64;
    public int signDistance = 32;

    // --- Partikel
    public boolean particleLimit = true;
    public int particleBudget = 600;          // maksimum partikel BARU per tick

    // --- Adaptive governor
    public boolean governor = true;
    public int targetFps = 60;

    // --- CPU
    public WorkerPriority workerPriority = WorkerPriority.NORMAL;

    public static PerfConfig get() { return instance; }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("custom_perf_tuner.json");
    }

    public static void load() {
        try {
            Path f = file();
            if (Files.exists(f)) {
                PerfConfig c = GSON.fromJson(Files.readString(f), PerfConfig.class);
                if (c != null) instance = c;
            }
        } catch (Exception e) {
            System.err.println("[PerfTuner] Gagal baca config, pakai default: " + e);
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.writeString(file(), GSON.toJson(instance));
        } catch (Exception e) {
            System.err.println("[PerfTuner] Gagal simpan config: " + e);
        }
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    /** Menjaga nilai tetap masuk akal walau file diedit manual. */
    public void sanitize() {
        if (preset == null) preset = Preset.BALANCED;
        if (workerPriority == null) workerPriority = WorkerPriority.NORMAL;
        entityDistance = clamp(entityDistance, 0, 256);
        itemDistance = clamp(itemDistance, 0, 256);
        armorStandDistance = clamp(armorStandDistance, 0, 256);
        occlusionRange = clamp(occlusionRange, 16, 128);
        blockEntityDistance = clamp(blockEntityDistance, 0, 256);
        signDistance = clamp(signDistance, 0, 256);
        particleBudget = clamp(particleBudget, 0, 5000);
        targetFps = clamp(targetFps, 20, 240);
    }

    /** Dipanggil setiap pemain mengubah sesuatu secara manual. */
    public static void markCustom() {
        instance.preset = Preset.CUSTOM;
        save();
    }

    /** Menerapkan seluruh nilai preset (dan opsional opsi video vanilla). */
    public static void applyPreset(Preset p) {
        PerfConfig c = instance;
        c.preset = p;
        switch (p) {
            case ULTRA_FPS -> {
                c.entityCulling = true;  c.entityDistance = 64; c.itemDistance = 24; c.armorStandDistance = 32;
                c.occlusionCulling = true; c.occlusionRange = 64;
                c.blockEntityCulling = true; c.blockEntityDistance = 48; c.signDistance = 24;
                c.particleLimit = true;  c.particleBudget = 250;
                c.governor = true;       c.targetFps = 60;
                c.workerPriority = WorkerPriority.LOW;
            }
            case QUALITY -> {
                c.entityCulling = true;  c.entityDistance = 0;  c.itemDistance = 64; c.armorStandDistance = 64;
                c.occlusionCulling = false; c.occlusionRange = 64;
                c.blockEntityCulling = true; c.blockEntityDistance = 96; c.signDistance = 48;
                c.particleLimit = false; c.particleBudget = 0;
                c.governor = false;      c.targetFps = 60;
                c.workerPriority = WorkerPriority.NORMAL;
            }
            case BALANCED -> {
                c.entityCulling = true;  c.entityDistance = 96; c.itemDistance = 40; c.armorStandDistance = 48;
                c.occlusionCulling = false; c.occlusionRange = 64;
                c.blockEntityCulling = true; c.blockEntityDistance = 64; c.signDistance = 32;
                c.particleLimit = true;  c.particleBudget = 600;
                c.governor = true;       c.targetFps = 60;
                c.workerPriority = WorkerPriority.NORMAL;
            }
            default -> { /* CUSTOM: nilai tidak diubah */ }
        }
        c.sanitize();
        save();
        Governor.reset();
        if (c.applyVanillaOptions && p != Preset.CUSTOM) VanillaOptions.apply(p);
    }
}
