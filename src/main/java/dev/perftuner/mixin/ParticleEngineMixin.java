package dev.perftuner.mixin;

import dev.perftuner.ParticleBudget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Membatasi jumlah partikel baru per tick (ledakan, pertarungan massal, kembang api). */
@Mixin(targets = "net.minecraft.client.particle.ParticleEngine")
public abstract class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true, require = 0)
    private void perftuner$add(@Coerce Object particle, CallbackInfo ci) {
        if (!ParticleBudget.tryAcquire()) ci.cancel();
    }
}
