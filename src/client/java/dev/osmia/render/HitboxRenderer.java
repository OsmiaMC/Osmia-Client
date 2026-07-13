package dev.osmia.render;

import dev.osmia.module.impl.visual.HitboxModule;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class HitboxRenderer {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final int HITBOX_COLOR = 0xFF00DFFF;
	private static final int LOOK_COLOR = 0xFF248CFF;
	private static final int REACH_COLOR = 0xFFFFA826;
	private static final double MAX_RENDER_DISTANCE_SQUARED = 256.0D * 256.0D;

	private final HitboxModule module;

	public HitboxRenderer(HitboxModule module) {
		this.module = module;
	}

	public void register() {
		LevelRenderEvents.BEFORE_GIZMOS.register(this::render);
	}

	private void render(LevelRenderContext context) {
		if (!module.isEnabled() || MINECRAFT.level == null || MINECRAFT.player == null) {
			return;
		}

		float partialTick = MINECRAFT.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		boolean renderLookDirection = module.lookDirection().value();
		boolean renderReachDistance = module.reachDistance().value();

		Gizmos.TemporaryCollection collection =
				context.levelRenderer().collectPerFrameRenderThreadGizmos();
		try {
			for (AbstractClientPlayer player : MINECRAFT.level.players()) {
				if (shouldSkip(player)) {
					continue;
				}

				Vec3 currentPosition = player.position();
				Vec3 renderPosition = player.getPosition(partialTick);
				AABB hitbox = player.getBoundingBox().move(
						renderPosition.x - currentPosition.x,
						renderPosition.y - currentPosition.y,
						renderPosition.z - currentPosition.z
				);
				Gizmos.cuboid(hitbox, GizmoStyle.stroke(HITBOX_COLOR, 1.5F));

				if (!renderLookDirection && !renderReachDistance) {
					continue;
				}

				Vec3 eye = player.getEyePosition(partialTick);
				Vec3 look = player.getViewVector(partialTick).normalize();
				if (renderReachDistance) {
					renderReach(player, eye, look);
				}
				if (renderLookDirection) {
					Gizmos.arrow(eye, eye.add(look.scale(1.25D)), LOOK_COLOR, 1.5F);
				}
			}
		} finally {
			collection.close();
		}
	}

	private static void renderReach(AbstractClientPlayer player, Vec3 eye, Vec3 look) {
		double reach = Math.clamp(player.entityInteractionRange(), 0.0D, 64.0D);
		Vec3 reachEnd = eye.add(look.scale(reach));
		Gizmos.line(eye, reachEnd, REACH_COLOR, 1.5F);
		Gizmos.point(reachEnd, REACH_COLOR, 4.0F);
		Gizmos.billboardText(
				String.format(Locale.ROOT, "%.1fm", reach),
				reachEnd.add(0.0D, 0.12D, 0.0D),
				TextGizmo.Style.forColorAndCentered(REACH_COLOR).withScale(0.10F)
		);
	}

	private static boolean shouldSkip(AbstractClientPlayer player) {
		if (player.isRemoved() || player.isSpectator()) {
			return true;
		}
		if (player == MINECRAFT.player && MINECRAFT.options.getCameraType().isFirstPerson()) {
			return true;
		}
		return player.distanceToSqr(MINECRAFT.player) > MAX_RENDER_DISTANCE_SQUARED;
	}
}
