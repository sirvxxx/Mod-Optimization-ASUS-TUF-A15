package dev.perftuner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Menu pengaturan performa. Dibuka dengan tombol K atau /perftune. */
public class PerfScreen extends Screen {
    private static final int COL_W = 150, GAP = 6, ROW_H = 24;

    private Button presetButton;

    public PerfScreen() {
        super(Component.translatable("custom_perf_tuner.screen.title"));
    }

    @Override
    protected void init() {
        PerfConfig c = PerfConfig.get();
        int left = (this.width - (2 * COL_W + GAP)) / 2;
        int rightX = left + COL_W + GAP;
        int y = 34;

        presetButton = addRenderableWidget(Button.builder(presetText(), b -> {
            PerfConfig.applyPreset(PerfConfig.get().preset.next());
            this.rebuildWidgets();
        }).bounds(left, y, 2 * COL_W + GAP, 20).build());
        y += ROW_H + 4;

        toggle(left, y, "entity_culling", () -> c.entityCulling, v -> c.entityCulling = v);
        slider(rightX, y, "entity_distance", 0, 256, 8, () -> c.entityDistance, v -> c.entityDistance = v);
        y += ROW_H;
        toggle(left, y, "occlusion", () -> c.occlusionCulling, v -> c.occlusionCulling = v);
        slider(rightX, y, "occlusion_range", 16, 128, 8, () -> c.occlusionRange, v -> c.occlusionRange = v);
        y += ROW_H;
        toggle(left, y, "block_entity_culling", () -> c.blockEntityCulling, v -> c.blockEntityCulling = v);
        slider(rightX, y, "block_entity_distance", 0, 256, 8, () -> c.blockEntityDistance, v -> c.blockEntityDistance = v);
        y += ROW_H;
        toggle(left, y, "particle_limit", () -> c.particleLimit, v -> c.particleLimit = v);
        slider(rightX, y, "particle_budget", 0, 2000, 50, () -> c.particleBudget, v -> c.particleBudget = v);
        y += ROW_H;
        toggle(left, y, "governor", () -> c.governor, v -> c.governor = v);
        slider(rightX, y, "target_fps", 30, 240, 5, () -> c.targetFps, v -> c.targetFps = v);
        y += ROW_H;

        addRenderableWidget(Button.builder(priorityText(), b -> {
            PerfConfig.get().workerPriority = PerfConfig.get().workerPriority.next();
            changed();
            b.setMessage(priorityText());
        }).bounds(left, y, COL_W, 20).build());
        toggle(rightX, y, "apply_vanilla", () -> c.applyVanillaOptions, v -> c.applyVanillaOptions = v);
        y += ROW_H + 4;

        addRenderableWidget(Button.builder(Component.translatable("custom_perf_tuner.screen.done"), b -> this.onClose())
                .bounds(left, y, 2 * COL_W + GAP, 20).build());
    }

    private void changed() {
        PerfConfig.markCustom();
        if (presetButton != null) presetButton.setMessage(presetText());
    }

    private static Component presetText() {
        return Component.translatable("custom_perf_tuner.screen.preset")
                .append(Component.literal(": "))
                .append(Component.translatable(PerfConfig.get().preset.langKey()).withStyle(ChatFormatting.GOLD));
    }

    private static Component priorityText() {
        return Component.translatable("custom_perf_tuner.screen.worker_priority")
                .append(Component.literal(": "))
                .append(Component.translatable(PerfConfig.get().workerPriority.langKey()));
    }

    private static Component onOff(String key, boolean on) {
        return Component.translatable("custom_perf_tuner.opt." + key)
                .append(Component.literal(": "))
                .append(Component.translatable(on ? "custom_perf_tuner.on" : "custom_perf_tuner.off")
                        .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private void toggle(int x, int y, String key, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(onOff(key, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            changed();
            b.setMessage(onOff(key, get.getAsBoolean()));
        }).bounds(x, y, COL_W, 20).build());
    }

    private void slider(int x, int y, String key, int min, int max, int step, IntSupplier get, IntConsumer set) {
        addRenderableWidget(new IntSlider(this, x, y, COL_W, 20, key, min, max, step, get, set));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int cx = this.width / 2;
        String title = this.getTitle().getString();
        g.text(this.font, title, cx - this.font.width(title) / 2, 14, 0xFFFFD36B, true);

        int y = this.height - 40;
        String l1 = Component.translatable("custom_perf_tuner.screen.status", Governor.fps(), Governor.level()).getString();
        g.text(this.font, l1, cx - this.font.width(l1) / 2, y, 0xFF9CFF7A, true);
        String l2 = Stats.summary() + hookSummary();
        g.text(this.font, l2, cx - this.font.width(l2) / 2, y + 12, 0xFFFFFFFF, true);

        String compat = compatLine();
        if (!compat.isEmpty()) g.text(this.font, compat, cx - this.font.width(compat) / 2, y + 24, 0xFF9FE8FF, true);
    }

    private static String hookSummary() {
        return "   Hooks: " + (Stats.hookEntity ? "E✓ " : "E✗ ") + (Stats.hookBlockEntity ? "BE✓ " : "BE✗ ")
                + (Stats.hookParticle ? "P✓" : "P✗");
    }

    private static String compatLine() {
        FabricLoader l = FabricLoader.getInstance();
        boolean sodium = l.isModLoaded("sodium"), iris = l.isModLoaded("iris");
        if (!sodium && !iris) return "";
        return "Compat: " + (sodium ? "Sodium " : "") + (iris ? "Iris" : "");
    }

    @Override
    public void removed() {
        PerfConfig.save();
        super.removed();
    }

    /** Slider bilangan bulat dengan langkah tetap; nilai 0 ditampilkan sebagai "Off". */
    private static final class IntSlider extends AbstractSliderButton {
        private final String key;
        private final int min, max, step;
        private final IntSupplier get;
        private final IntConsumer set;
        private final PerfScreen owner;

        IntSlider(PerfScreen owner, int x, int y, int w, int h, String key, int min, int max, int step,
                  IntSupplier get, IntConsumer set) {
            super(x, y, w, h, Component.empty(), 0.0);
            this.owner = owner; this.key = key; this.min = min; this.max = max; this.step = step;
            this.get = get; this.set = set;
            this.value = (get.getAsInt() - min) / (double) (max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (get == null) return;
            int v = get.getAsInt();
            Component val = (min == 0 && v == 0) ? Component.translatable("custom_perf_tuner.off") : Component.literal(Integer.toString(v));
            setMessage(Component.translatable("custom_perf_tuner.opt." + key).append(Component.literal(": ")).append(val));
        }

        @Override
        protected void applyValue() {
            int v = min + (int) Math.round(this.value * (max - min) / step) * step;
            set.accept(Math.max(min, Math.min(max, v)));
            owner.changed();
        }
    }
}
