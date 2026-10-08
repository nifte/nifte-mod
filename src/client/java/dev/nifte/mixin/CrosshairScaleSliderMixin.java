package dev.nifte.mixin;

import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.nifte.config.CrosshairScaleSlider;

@Mixin(targets = "me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry$Slider", remap = false)
public abstract class CrosshairScaleSliderMixin {
	@Shadow
	@Final
	private IntegerSliderEntry this$0;

	@Shadow(remap = true)
	protected double value;

	@Shadow(remap = true)
	protected boolean canChangeValue;

	@Shadow(remap = true)
	protected abstract void setValue(double newValue);

	@Inject(method = "applyValue", at = @At("HEAD"), cancellable = true, remap = false)
	private void nifte$snapCrosshairScale(CallbackInfo ci) {
		if (!CrosshairScaleSlider.applies(this.this$0.getFieldName())) {
			return;
		}

		IntegerSliderEntryAccessor entry = (IntegerSliderEntryAccessor) this.this$0;
		int snapped = CrosshairScaleSlider.snap(entry.nifte$minimum(), entry.nifte$maximum(), this.value);
		entry.nifte$value().set(snapped);
		this.value = CrosshairScaleSlider.progress(entry.nifte$minimum(), entry.nifte$maximum(), snapped);
		ci.cancel();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = true)
	private void nifte$stepCrosshairScale(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!CrosshairScaleSlider.applies(this.this$0.getFieldName()) || !this.this$0.isEditable() || !this.canChangeValue) {
			return;
		}

		boolean left = event.isLeft();
		boolean right = event.isRight();
		if (!left && !right) {
			return;
		}

		IntegerSliderEntryAccessor entry = (IntegerSliderEntryAccessor) this.this$0;
		int next = Mth.clamp(this.this$0.getValue() + (right ? 1 : -1), entry.nifte$minimum(), entry.nifte$maximum());
		this.setValue(CrosshairScaleSlider.progress(entry.nifte$minimum(), entry.nifte$maximum(), next));
		cir.setReturnValue(true);
	}
}
