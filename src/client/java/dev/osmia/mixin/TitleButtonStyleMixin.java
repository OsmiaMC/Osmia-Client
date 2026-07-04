package dev.osmia.mixin;

import dev.osmia.gui.OsmiaTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class TitleButtonStyleMixin {
	private static final int HALF_TRANSPARENT_BLACK = 0x80000000;
	private static final int HOVER_EDGE_COLOR = 0x00DFFF;
	private static final int HOVER_FILL_ALPHA = 56;
	private static final int HOVER_EDGE_ALPHA = 176;
	private static final long HOVER_FADE_NANOS = 180_000_000L;

	@Unique
	private float osmia$hoverProgress;

	@Unique
	private long osmia$lastRenderNanos = -1L;

	@Inject(method = "extractDefaultSprite", at = @At("HEAD"), cancellable = true)
	private void osmia$renderBlackTitleButton(GuiGraphicsExtractor graphics, CallbackInfo callbackInfo) {
		if (!(Minecraft.getInstance().gui.screen() instanceof TitleScreen)) {
			return;
		}

		AbstractButton button = (AbstractButton) (Object) this;
		long now = System.nanoTime();
		if (osmia$lastRenderNanos >= 0L) {
			float step = Math.min(1.0F, (float) (now - osmia$lastRenderNanos) / HOVER_FADE_NANOS);
			if (button.isHovered()) {
				osmia$hoverProgress = Math.min(1.0F, osmia$hoverProgress + step);
			} else {
				osmia$hoverProgress = Math.max(0.0F, osmia$hoverProgress - step);
			}
		}
		osmia$lastRenderNanos = now;

		int left = button.getX();
		int top = button.getY();
		int right = left + button.getWidth();
		int bottom = top + button.getHeight();
		graphics.fill(
				left,
				top,
				right,
				bottom,
				HALF_TRANSPARENT_BLACK
		);

		float easedProgress = osmia$smoothStep(osmia$hoverProgress);
		if (easedProgress > 0.0F) {
			int fillAlpha = Math.round(HOVER_FILL_ALPHA * easedProgress);
			int edgeAlpha = Math.round(HOVER_EDGE_ALPHA * easedProgress);
			graphics.fillGradient(
					left,
					top,
					right,
					bottom,
					OsmiaTheme.withAlpha(OsmiaTheme.ACCENT_START, fillAlpha),
					OsmiaTheme.withAlpha(OsmiaTheme.ACCENT_END, fillAlpha)
			);
			graphics.outline(
					left,
					top,
					button.getWidth(),
					button.getHeight(),
					OsmiaTheme.withAlpha(HOVER_EDGE_COLOR, edgeAlpha)
			);
		}
		callbackInfo.cancel();
	}

	@Unique
	private static float osmia$smoothStep(float progress) {
		return progress * progress * (3.0F - 2.0F * progress);
	}
}
