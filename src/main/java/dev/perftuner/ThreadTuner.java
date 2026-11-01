package dev.perftuner;

/**
 * Mengatur prioritas thread worker Minecraft ("Worker-Main-N", "Worker-Bootstrap-N", "IO-Worker-...").
 * Prioritas lebih rendah = render thread lebih jarang kalah rebutan CPU saat chunk loading / meshing,
 * jadi lebih sedikit stutter. Catatan: ini TIDAK mengurangi total kerja CPU, hanya urutan prioritasnya.
 */
public final class ThreadTuner {
    private ThreadTuner() {}

    private static final int SCAN_EVERY_TICKS = 100;
    private static int ticks;
    private static Thread[] scratch = new Thread[256];   // dipakai ulang agar tidak menghasilkan sampah

    public static void onTick() {
        if (++ticks < SCAN_EVERY_TICKS) return;
        ticks = 0;
        apply(PerfConfig.get().workerPriority.value);
    }

    static boolean isWorker(String name) {
        return name != null && (name.startsWith("Worker-") || name.startsWith("IO-Worker"));
    }

    /** @return jumlah thread yang diubah */
    static int apply(int priority) {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) root = root.getParent();
        int n = root.enumerate(scratch, true);
        if (n >= scratch.length) {                         // array penuh: perbesar lalu coba lagi nanti
            scratch = new Thread[scratch.length * 2];
            n = root.enumerate(scratch, true);
        }
        int changed = 0;
        for (int i = 0; i < n; i++) {
            Thread t = scratch[i];
            scratch[i] = null;
            if (t != null && t.isAlive() && isWorker(t.getName()) && t.getPriority() != priority) {
                try { t.setPriority(priority); changed++; } catch (RuntimeException ignored) { }
            }
        }
        return changed;
    }
}
