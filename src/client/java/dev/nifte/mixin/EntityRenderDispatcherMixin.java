package dev.nifte.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.mobs.HideDeadMobs;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(
		method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDDF)Z",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private void nifte$hideDeadMobs(
		Entity entity,
		Frustum culler,
		double camX,
		double camY,
		double camZ,
		float partialTick,
		CallbackInfoReturnable<Boolean> cir
	) {
		nifte$hideDeadMob(entity, cir);
	}

	@Inject(
		method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
		at = @At("HEAD"),
		cancellable = true,
		require = 0
	)
	private void nifte$hideDeadMobsLegacy(
		Entity entity,
		Frustum culler,
		double camX,
		double camY,
		double camZ,
		CallbackInfoReturnable<Boolean> cir
	) {
		nifte$hideDeadMob(entity, cir);
	}

	private void nifte$hideDeadMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (HideDeadMobs.shouldHide(entity)) {
			cir.setReturnValue(false);
		}
	}
}
