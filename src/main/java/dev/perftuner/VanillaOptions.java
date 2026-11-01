package dev.perftuner;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;

/**
 * Menerapkan opsi video vanilla untuk sebuah preset. Memakai reflection pada nama accessor Options
 * agar build tidak gagal bila Mojang mengganti nama; opsi yang tidak ditemukan dilewati dan dicatat di log.
 */
final class VanillaOptions {
    private VanillaOptions() {}

    static void apply(Preset p) {
        Object o = Minecraft.getInstance().options;
        switch (p) {
            case ULTRA_FPS -> { set(o, "renderDistance", 8);  set(o, "simulationDistance", 6);  set(o, "entityDistanceScaling", 0.75);
                                setEnum(o, "particles", "MINIMAL");   set(o, "biomeBlendRadius", 1); set(o, "mipmapLevels", 2); }
            case QUALITY   -> { set(o, "renderDistance", 16); set(o, "simulationDistance", 12); set(o, "entityDistanceScaling", 1.25);
                                setEnum(o, "particles", "ALL");       set(o, "biomeBlendRadius", 5); set(o, "mipmapLevels", 4); }
            case BALANCED  -> { set(o, "renderDistance", 12); set(o, "simulationDistance", 8);  set(o, "entityDistanceScaling", 1.0);
                                setEnum(o, "particles", "DECREASED"); set(o, "biomeBlendRadius", 3); set(o, "mipmapLevels", 3); }
            default -> { return; }
        }
        try {
            o.getClass().getMethod("save").invoke(o);
        } catch (ReflectiveOperationException | RuntimeException e) {
            warn("save", e);
        }
    }

    private static Object option(Object options, String accessor) throws ReflectiveOperationException {
        return options.getClass().getMethod(accessor).invoke(options);
    }

    private static Method setter(Object optionInstance) throws NoSuchMethodException {
        return optionInstance.getClass().getMethod("set", Object.class);
    }

    private static void set(Object options, String accessor, Object value) {
        try {
            Object inst = option(options, accessor);
            setter(inst).invoke(inst, value);
        } catch (ReflectiveOperationException | RuntimeException e) {
            warn(accessor, e);
        }
    }

    /** Opsi bertipe enum (mis. particles): cari konstanta berdasarkan nama dari nilai saat ini. */
    private static void setEnum(Object options, String accessor, String constant) {
        try {
            Object inst = option(options, accessor);
            Object current = inst.getClass().getMethod("get").invoke(inst);
            for (Object k : current.getClass().getEnumConstants()) {
                if (((Enum<?>) k).name().equals(constant)) {
                    setter(inst).invoke(inst, k);
                    return;
                }
            }
            warn(accessor + "=" + constant, new IllegalArgumentException("konstanta tidak ada"));
        } catch (ReflectiveOperationException | RuntimeException e) {
            warn(accessor, e);
        }
    }

    private static void warn(String what, Exception e) {
        System.err.println("[PerfTuner] Opsi vanilla '" + what + "' dilewati: " + e);
    }
}
