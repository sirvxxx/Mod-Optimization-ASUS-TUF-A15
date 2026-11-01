package dev.perftuner;

/** Prioritas thread worker Minecraft (chunk meshing / world gen / IO). */
public enum WorkerPriority {
    NORMAL(Thread.NORM_PRIORITY), LOW(3), LOWEST(Thread.MIN_PRIORITY);

    public final int value;

    WorkerPriority(int value) { this.value = value; }

    public WorkerPriority next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public String langKey() { return "custom_perf_tuner.priority." + name().toLowerCase(java.util.Locale.ROOT); }
}
