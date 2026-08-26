package dev.nifte.mixin;

import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.nifte.feature.guimove.GuiMoveFeature;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
	@Shadow
	@Final
	private Options options;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void nifte$guiMove(CallbackInfo ci) {
		if (!GuiMoveFeature.shouldApply()) {
			return;
		}

		this.keyPresses = GuiMoveFeature.readKeyPresses(this.options);
		this.moveVector = GuiMoveFeature.movementVector(this.keyPresses);
		ci.cancel();
	}
}
