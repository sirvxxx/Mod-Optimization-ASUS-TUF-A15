package dev.perftuner;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class PerfTunerClient implements ClientModInitializer {
    public static final String MOD_ID = "custom_perf_tuner";

    private static KeyMapping openKey;
    private static volatile boolean openRequested;

    /** Dipanggil dari command (dibuka di tick berikutnya agar tidak bentrok dengan layar chat). */
    public static void requestOpen() { openRequested = true; }

    @Override
    public void onInitializeClient() {
        PerfConfig.load();

        // Penghitung frame: elemen HUD yang tidak menggambar apa pun, hanya menambah counter.
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "frame_counter"),
                (graphics, delta) -> Governor.onFrame());

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.custom_perf_tuner.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Stats.onTick();
            Governor.onTick();
            ParticleBudget.onTick();
            ThreadTuner.onTick();

            boolean open = false;
            while (openKey.consumeClick()) open = true;
            if (openRequested) { openRequested = false; open = true; }
            if (open && client.player != null) McCompat.openScreen(new PerfScreen());
        });

        PerfCommands.register();
    }
}
