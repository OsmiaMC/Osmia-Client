package dev.osmia.mixin;

import dev.osmia.config.OsmiaConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceBar.class)
public abstract class ExperienceBarMixin {
	@Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
	private void osmia$hideExperienceBackground(
			GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker,
			CallbackInfo callback
	) {
		if (OsmiaConfig.isHudEnabled()) {
			callback.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void osmia$hideExperienceProgress(
			GuiGraphicsExtractor graphics,
			DeltaTracker deltaTracker,
			CallbackInfo callback
	) {
		if (OsmiaConfig.isHudEnabled()) {
			callback.cancel();
		}
	}
}
