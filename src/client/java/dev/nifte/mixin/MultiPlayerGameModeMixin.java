package dev.nifte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.feature.autotool.AutoToolFeature;
import dev.nifte.feature.breakdelay.NoBreakDelay;
import dev.nifte.feature.bridge.BedrockBridging;
import dev.nifte.feature.restock.HandRestockFeature;
import dev.nifte.feature.toolprotect.ToolProtectFeature;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private int destroyDelay;

	@Shadow
	public abstract InteractionResult useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit);

	@Shadow
	public abstract void stopDestroyBlock();

	@ModifyVariable(method = "useItemOn", at = @At("HEAD"), argsOnly = true)
	private BlockHitResult nifte$bedrockBridging(BlockHitResult hit, LocalPlayer player, InteractionHand hand) {
		return BedrockBridging.adjust(player, hand, hit);
	}

	@Inject(method = "useItemOn", at = @At("HEAD"))
	private void nifte$handRestockCaptureUseOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		HandRestockFeature.capture(player, hand);
	}

	@Inject(method = "useItemOn", at = @At("RETURN"))
	private void nifte$handRestockUseOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		HandRestockFeature.afterUse(player, hand, cir.getReturnValue());
	}

	@Inject(method = "useItem", at = @At("HEAD"))
	private void nifte$handRestockCaptureUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof LocalPlayer localPlayer) {
			HandRestockFeature.capture(localPlayer, hand);
		}
	}

	@Inject(method = "useItem", at = @At("RETURN"))
	private void nifte$handRestockUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof LocalPlayer localPlayer) {
			HandRestockFeature.afterUse(localPlayer, hand, cir.getReturnValue());
		}
	}

	@Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
	private void nifte$bedrockBridgingAir(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (!(player instanceof LocalPlayer localPlayer) || ToolProtectFeature.blockHand(localPlayer, hand)) {
			return;
		}

		BlockHitResult hit = BedrockBridging.airHit(localPlayer, hand, this.minecraft.hitResult);
		if (hit != null) {
			cir.setReturnValue(this.useItemOn(localPlayer, hand, hit));
		}
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"))
	private void nifte$noBreakDelay(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (NoBreakDelay.enabled()) {
			this.destroyDelay = 0;
		}
	}

	@Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void nifte$beforeDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		AutoToolFeature.selectFor(this.minecraft, pos);
		if (ToolProtectFeature.blockMainHand(this.minecraft)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void nifte$protectContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (ToolProtectFeature.blockMainHand(this.minecraft)) {
			this.stopDestroyBlock();
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void nifte$protectAttack(Player player, Entity entity, CallbackInfo ci) {
		if (player instanceof LocalPlayer localPlayer && ToolProtectFeature.blockHand(localPlayer, InteractionHand.MAIN_HAND)) {
			ci.cancel();
		}
	}

	@Inject(method = "piercingAttack", at = @At("HEAD"), cancellable = true)
	private void nifte$protectPiercingAttack(PiercingWeapon weapon, CallbackInfo ci) {
		if (ToolProtectFeature.blockMainHand(this.minecraft)) {
			ci.cancel();
		}
	}

	@Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
	private void nifte$protectUseOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHit, CallbackInfoReturnable<InteractionResult> cir) {
		if (ToolProtectFeature.shouldBlock(player.getItemInHand(hand))) {
			cir.setReturnValue(ToolProtectFeature.useOnWhenBlocked(this.minecraft, player, blockHit));
		}
	}

	@Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
	private void nifte$protectUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof LocalPlayer localPlayer && ToolProtectFeature.blockHand(localPlayer, hand)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void nifte$protectInteract(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof LocalPlayer localPlayer && ToolProtectFeature.blockEntityInteract(localPlayer, entity, hand)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
