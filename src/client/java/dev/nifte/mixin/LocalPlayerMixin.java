package dev.nifte.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.camera.DynamicThirdPerson;
import dev.nifte.feature.combat.IgnoreGrassFeature;
import dev.nifte.feature.dropconfirm.DropConfirmFeature;
import dev.nifte.feature.sprint.KeepSprintFeature;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Invoker("isSprintingPossible")
	abstract boolean nifte$isSprintingPossible(boolean allowedInShallowWater);

	@Inject(method = "shouldStopRunSprinting", at = @At("HEAD"), cancellable = true)
	private void nifte$keepSprintOnWall(CallbackInfoReturnable<Boolean> cir) {
		if (!KeepSprintFeature.enabled()) {
			return;
		}

		LocalPlayer player = (LocalPlayer) (Object) this;
		cir.setReturnValue(!this.nifte$isSprintingPossible(player.getAbilities().flying) || !player.input.hasForwardImpulse());
	}

	@Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
	private void nifte$dropConfirm(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (DropConfirmFeature.shouldBlockDrop(player.getInventory().getSelectedItem())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "raycastHitResult", at = @At("RETURN"), cancellable = true)
	private void nifte$ignoreGrass(float partialTick, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
		HitResult rewritten = IgnoreGrassFeature.hitThroughGrass(
			(LocalPlayer) (Object) this,
			cameraEntity,
			cir.getReturnValue(),
			partialTick
		);
		if (rewritten != null) {
			cir.setReturnValue(rewritten);
		}
	}

	@Inject(method = "applyInput", at = @At("HEAD"))
	private void nifte$dynamicThirdPerson(CallbackInfo ci) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		Vec2 rewritten = DynamicThirdPerson.apply(player);
		if (rewritten != null) {
			((ClientInputAccessor) player.input).nifte$setMoveVector(rewritten);
		}
	}
}
