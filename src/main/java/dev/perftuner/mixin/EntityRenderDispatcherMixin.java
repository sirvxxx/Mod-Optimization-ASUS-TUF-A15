package dev.perftuner.mixin;

import dev.perftuner.CullLogic;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Titik pusat keputusan "entity ini digambar atau tidak" (setelah frustum culling vanilla).
 * Target berupa string + require = 0: jika nama berubah di versi lain, fitur ini nonaktif tanpa crash.
 */
@Mixin(targets = "net.minecraft.client.renderer.entity.EntityRenderDispatcher")
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void perftuner$shouldRender(Entity entity, @Coerce Object frustum, double x, double y, double z,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!CullLogic.shouldRenderEntity(entity, x, y, z)) cir.setReturnValue(false);
    }
}
