package dev.osmia.mixin;

import dev.osmia.OsmiaClient;
import dev.osmia.module.impl.movement.AutoSprintModule;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
	@ModifyArg(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Input;<init>(ZZZZZZZ)V"
			),
			index = 6
	)
	private boolean osmia$applyAutoSprint(boolean sprintKeyDown) {
		return sprintKeyDown || OsmiaClient.modules().get(AutoSprintModule.class).isEnabled();
	}
}
