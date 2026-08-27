package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.autoweapon.AutoWeaponFeature;
import dev.nifte.feature.fastplace.FastBlockPlacement;
import dev.nifte.feature.glow.EntityGlowFeature;
import dev.nifte.feature.quickuse.QuickUseFeature;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	private int rightClickDelay;

	@Inject(method = "tick", at = @At("HEAD"))
	private void nifte$tickHead(CallbackInfo ci) {
		Minecraft minecraft = (Minecraft) (Object) this;
		QuickUseFeature.beforeTick(minecraft);
		FastBlockPlacement.beforeTick(minecraft);
		if (FastBlockPlacement.shouldSkipDelay(minecraft.player) || QuickUseFeature.shouldSkipUseDelay()) {
			this.rightClickDelay = 0;
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"))
	private void nifte$autoWeapon(CallbackInfoReturnable<Boolean> cir) {
		Minecraft minecraft = (Minecraft) (Object) this;
		if (minecraft.missTime > 0 || !(minecraft.hitResult instanceof EntityHitResult hit)) {
			return;
		}

		AutoWeaponFeature.selectFor(minecraft, hit.getEntity());
	}

	@Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
	private void nifte$entityGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && EntityGlowFeature.shouldGlow(entity)) {
			cir.setReturnValue(true);
		}
	}
}
