package dev.nifte.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.bridge.BedrockBridging;
import dev.nifte.hud.ProjectileTrajectory;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private ClientLevel level;

	@Inject(
		method = "extract",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extractGizmos()V")
	)
	private void nifte$projectileTrajectory(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
		ProjectileTrajectory.emit(this.minecraft, camera, deltaPartialTick);
	}

	@Inject(method = "extractBlockOutline", at = @At("RETURN"))
	private void nifte$bridgePreview(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (this.level == null) {
			return;
		}

		BlockPos target = BedrockBridging.previewTarget(this.minecraft);
		if (target == null) {
			return;
		}

		boolean highContrast = this.minecraft.options.highContrastBlockOutline().get();
		levelRenderState.blockOutlineRenderState = new BlockOutlineRenderState(target, true, highContrast, Shapes.block());
	}
}
