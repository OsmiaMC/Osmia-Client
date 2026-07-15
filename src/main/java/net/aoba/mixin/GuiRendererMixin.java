package net.aoba.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.aoba.Aoba;
import net.minecraft.client.gui.render.GuiRenderer;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void onRenderTail(CallbackInfo ci) {
		if (Aoba.getInstance() != null && Aoba.getInstance().render2D != null) {
			Aoba.getInstance().render2D.render();
		}
	}
}
