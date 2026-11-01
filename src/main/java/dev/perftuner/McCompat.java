package dev.perftuner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/** 26.1.2: setScreen(Screen); 26.2: setScreenAndShow(Screen). Dipanggil lewat reflection agar 1 source jalan di keduanya. */
public final class McCompat {
    private McCompat() {}

    public static void openScreen(Screen screen) {
        Minecraft mc = Minecraft.getInstance();
        for (String name : new String[]{"setScreenAndShow", "setScreen"}) {
            try {
                Method m;
                try {
                    m = Minecraft.class.getMethod(name, Screen.class);
                } catch (NoSuchMethodException e) {
                    m = Minecraft.class.getDeclaredMethod(name, Screen.class);
                    m.setAccessible(true);
                }
                m.invoke(mc, screen);
                return;
            } catch (NoSuchMethodException ignored) {
                // coba nama berikutnya
            } catch (Exception e) {
                System.err.println("[PerfTuner] Gagal membuka layar: " + e);
                return;
            }
        }
        System.err.println("[PerfTuner] Method setScreen tidak ditemukan di versi ini.");
    }
}
