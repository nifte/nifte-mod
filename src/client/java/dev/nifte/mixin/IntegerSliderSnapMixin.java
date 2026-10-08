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

import dev.nifte.config.IntegerSliderSnap;
import dev.nifte.config.NifteConfigScreen;

@Mixin(targets = "me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry$Slider", remap = false)
public abstract class IntegerSliderSnapMixin {
	@Shadow
	@Final
	private IntegerSliderEntry this$0;

	@Shadow
	public abstract double getProgress();

	@Shadow
	public abstract void setProgress(double progress);

	@Shadow(remap = true)
	protected abstract void setValue(double newValue);

	@Inject(method = "applyValue", at = @At("HEAD"), cancellable = true, remap = false)
	private void nifte$snapIntegerSlider(CallbackInfo ci) {
		if (!nifte$snaps()) {
			return;
		}

		IntegerSliderEntryAccessor entry = (IntegerSliderEntryAccessor) this.this$0;
		int snapped = IntegerSliderSnap.snap(entry.nifte$minimum(), entry.nifte$maximum(), this.getProgress());
		entry.nifte$value().set(snapped);
		this.setProgress(IntegerSliderSnap.progress(entry.nifte$minimum(), entry.nifte$maximum(), snapped));
		ci.cancel();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = true)
	private void nifte$stepIntegerSlider(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!nifte$snaps() || !this.this$0.isEditable() || !((AbstractSliderButtonAccessor) (Object) this).nifte$canChangeValue()) {
			return;
		}

		boolean left = event.isLeft();
		boolean right = event.isRight();
		if (!left && !right) {
			return;
		}

		IntegerSliderEntryAccessor entry = (IntegerSliderEntryAccessor) this.this$0;
		int next = Mth.clamp(this.this$0.getValue() + (right ? 1 : -1), entry.nifte$minimum(), entry.nifte$maximum());
		this.setValue(IntegerSliderSnap.progress(entry.nifte$minimum(), entry.nifte$maximum(), next));
		cir.setReturnValue(true);
	}

	private boolean nifte$snaps() {
		return NifteConfigScreen.isNifteScreen(this.this$0.getConfigScreen());
	}
}
