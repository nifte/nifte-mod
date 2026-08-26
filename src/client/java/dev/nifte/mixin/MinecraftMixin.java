package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.autoweapon.AutoWeaponFeature;
import dev.nifte.feature.fastplace.FastBlockPlacement;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	private int rightClickDelay;

	@Inject(method = "tick", at = @At("HEAD"))
	private void nifte$fastBlockPlacement(CallbackInfo ci) {
		Minecraft minecraft = (Minecraft) (Object) this;
		if (FastBlockPlacement.shouldSkipDelay(minecraft.player)) {
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
}
