package dev.perftuner.mixin;

import dev.perftuner.CullLogic;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mengembalikan renderer = null membuat Minecraft (dan Sodium) melewati block entity itu
 * (sign, banner, kepala mob, chest, dst) bila terlalu jauh.
 */
@Mixin(targets = "net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher")
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void perftuner$getRenderer(BlockEntity blockEntity, CallbackInfoReturnable<Object> cir) {
        if (!CullLogic.shouldRenderBlockEntity(blockEntity)) cir.setReturnValue(null);
    }
}
