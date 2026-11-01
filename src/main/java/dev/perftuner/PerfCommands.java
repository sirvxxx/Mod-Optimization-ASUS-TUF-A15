package dev.perftuner;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** /perftune [open] | preset <balanced|ultra_fps|quality> | status */
public final class PerfCommands {
    private PerfCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var root = ClientCommands.literal("perftune")
                .executes(ctx -> { PerfTunerClient.requestOpen(); return 1; })
                .then(ClientCommands.literal("open")
                    .executes(ctx -> { PerfTunerClient.requestOpen(); return 1; }))
                .then(ClientCommands.literal("status")
                    .executes(ctx -> {
                        PerfConfig c = PerfConfig.get();
                        ctx.getSource().sendFeedback(Component.literal(
                            "[PerfTuner] preset=" + c.preset + "  fps=" + Governor.fps() + "  governor-level=" + Governor.level()
                            + "  particle-budget=" + ParticleBudget.currentBudget()));
                        ctx.getSource().sendFeedback(Component.literal("[PerfTuner] " + Stats.summary()
                            + "  hooks entity=" + Stats.hookEntity + " blockEntity=" + Stats.hookBlockEntity
                            + " particle=" + Stats.hookParticle));
                        return 1;
                    }));

            var presets = ClientCommands.literal("preset");
            for (Preset p : new Preset[]{Preset.BALANCED, Preset.ULTRA_FPS, Preset.QUALITY}) {
                presets.then(ClientCommands.literal(p.name().toLowerCase(Locale.ROOT)).executes(ctx -> {
                    PerfConfig.applyPreset(p);
                    ctx.getSource().sendFeedback(Component.translatable("custom_perf_tuner.cmd.preset_set",
                            Component.translatable(p.langKey())));
                    return 1;
                }));
            }
            dispatcher.register(root.then(presets));
        });
    }
}
