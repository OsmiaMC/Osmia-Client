package dev.osmia.profile;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.server.level.ParticleStatus;

import java.util.Objects;

public final class PvpSettingsProfile {
	private PvpSettingsProfile() {
	}

	public static int apply(Options options) {
		int changed = 0;

		changed += set(options.renderDistance(), 8);
		changed += set(options.simulationDistance(), 6);
		changed += set(options.entityDistanceScaling(), 1.0);
		changed += set(options.framerateLimit(), Options.UNLIMITED_FRAMERATE_CUTOFF);
		changed += set(options.enableVsync(), false);
		changed += set(options.cloudStatus(), CloudStatus.OFF);
		changed += set(options.weatherRadius(), 5);
		changed += set(options.cutoutLeaves(), false);
		changed += set(options.vignette(), false);
		changed += set(options.improvedTransparency(), false);
		changed += set(options.ambientOcclusion(), false);
		changed += set(options.chunkSectionFadeInTime(), 0.0);
		changed += set(options.prioritizeChunkUpdates(), PrioritizeChunkUpdates.NONE);
		changed += set(options.mipmapLevels(), 0);
		changed += set(options.textureFiltering(), TextureFilteringMethod.NONE);
		changed += set(options.biomeBlendRadius(), 0);
		changed += set(options.entityShadows(), false);
		changed += set(options.menuBackgroundBlurriness(), 0);
		changed += set(options.particles(), ParticleStatus.MINIMAL);

		changed += set(options.rawMouseInput(), true);
		changed += set(options.autoJump(), false);
		changed += set(options.bobView(), false);
		changed += set(options.toggleSprint(), true);
		changed += set(options.attackIndicator(), AttackIndicatorStatus.CROSSHAIR);
		changed += set(options.fov(), 90);
		changed += set(options.fovEffectScale(), 0.0);
		changed += set(options.screenEffectScale(), 0.0);
		changed += set(options.darknessEffectScale(), 0.0);
		changed += set(options.damageTiltStrength(), 0.0);
		changed += set(options.gamma(), 1.0);

		if (changed > 0) {
			options.save();
		}

		return changed;
	}

	private static <T> int set(OptionInstance<T> option, T value) {
		if (Objects.equals(option.get(), value)) {
			return 0;
		}

		option.set(value);
		return 1;
	}
}
