package dev.osmia.render;

import dev.osmia.config.OsmiaConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public final class OsmiaHud {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	private static final int HOTBAR_BACKGROUND = 0x80000000;
	private static final int SELECTED_SLOT_TINT = 0x80FFFFFF;
	private static final BarStyle HEALTH_STYLE = new BarStyle(
			0xFFFF2D2D,
			0x66000000,
			0xFFFF6A6A,
			0
	);
	private static final BarStyle HUNGER_STYLE = new BarStyle(
			0xFFFFA826,
			0x66000000,
			0xFFFFC26B,
			0xFFFFE066
	);

	private static final int SLOT_SIZE = 20;
	private static final int SLOT_COUNT = 9;
	private static final int HOTBAR_WIDTH = SLOT_SIZE * SLOT_COUNT;
	private static final int HOTBAR_CORNER = 5;
	private static final int BAR_HEIGHT = 8;
	private static final int BAR_CORNER = 4;
	private static final int BAR_GAP_ABOVE_HOTBAR = 3;
	private static final int BAR_CENTER_GAP = 2;

	private OsmiaHud() {
	}

	public static void initialize() {
		HudElementRegistry.replaceElement(
				VanillaHudElements.HOTBAR,
				replaceWhenEnabled(OsmiaHud::renderHotbar)
		);
		HudElementRegistry.replaceElement(
				VanillaHudElements.HEALTH_BAR,
				replaceWhenEnabled(OsmiaHud::renderHealthBar)
		);
		HudElementRegistry.replaceElement(
				VanillaHudElements.FOOD_BAR,
				replaceWhenEnabled(OsmiaHud::renderHungerBar)
		);
		HudElementRegistry.replaceElement(
				VanillaHudElements.EXPERIENCE_LEVEL,
				hideWhenEnabled()
		);
	}

	private static Function<HudElement, HudElement> replaceWhenEnabled(
			Consumer<GuiGraphicsExtractor> renderer
	) {
		return original -> (graphics, deltaTracker) -> {
			if (OsmiaConfig.isHudEnabled()) {
				renderer.accept(graphics);
			} else {
				original.extractRenderState(graphics, deltaTracker);
			}
		};
	}

	private static Function<HudElement, HudElement> hideWhenEnabled() {
		return original -> (graphics, deltaTracker) -> {
			if (!OsmiaConfig.isHudEnabled()) {
				original.extractRenderState(graphics, deltaTracker);
			}
		};
	}

	private static void renderHotbar(GuiGraphicsExtractor graphics) {
		LocalPlayer player = MINECRAFT.player;
		if (player == null) {
			return;
		}

		int left = hotbarLeft();
		int top = hotbarTop();
		int right = left + HOTBAR_WIDTH;
		int bottom = top + SLOT_SIZE;
		fillRounded(graphics, left, top, right, bottom, HOTBAR_CORNER, HOTBAR_BACKGROUND);

		int selectedSlot = player.getInventory().getSelectedSlot();
		if (selectedSlot >= 0 && selectedSlot < SLOT_COUNT) {
			int selectedLeft = left + selectedSlot * SLOT_SIZE;
			fillRoundedSection(
					graphics,
					left,
					top,
					right,
					bottom,
					HOTBAR_CORNER,
					selectedLeft,
					selectedLeft + SLOT_SIZE,
					SELECTED_SLOT_TINT
			);
		}

		for (int slot = 0; slot < SLOT_COUNT; slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}

			int itemX = left + slot * SLOT_SIZE + 2;
			int itemY = top + 2;
			graphics.item(player, stack, itemX, itemY, slot);
			graphics.itemDecorations(MINECRAFT.font, stack, itemX, itemY);
		}
	}

	private static void renderHealthBar(GuiGraphicsExtractor graphics) {
		LocalPlayer player = MINECRAFT.player;
		if (player == null) {
			return;
		}

		float maxHealth = player.getMaxHealth();
		float health = Math.min(player.getHealth(), maxHealth);
		float absorption = player.getAbsorptionAmount();
		float maximum = maxHealth + absorption;
		float ratio = maximum <= 0.0F ? 0.0F : (health + absorption) / maximum;

		int right = MINECRAFT.getWindow().getGuiScaledWidth() / 2 - BAR_CENTER_GAP;
		drawStatBar(graphics, hotbarLeft(), right, ratio, 0.0F, HEALTH_STYLE);
	}

	private static void renderHungerBar(GuiGraphicsExtractor graphics) {
		LocalPlayer player = MINECRAFT.player;
		if (player == null) {
			return;
		}

		final int maximumFood = 20;
		int food = player.getFoodData().getFoodLevel();
		float saturation = Math.min(food, player.getFoodData().getSaturationLevel());
		float foodRatio = clamp01((float) food / maximumFood);
		float saturationRatio = clamp01(saturation / maximumFood);

		int left = MINECRAFT.getWindow().getGuiScaledWidth() / 2 + BAR_CENTER_GAP;
		drawStatBar(
				graphics,
				left,
				hotbarLeft() + HOTBAR_WIDTH,
				foodRatio,
				saturationRatio,
				HUNGER_STYLE
		);
	}

	private static void drawStatBar(
			GuiGraphicsExtractor graphics,
			int left,
			int right,
			float fillRatio,
			float overlayRatio,
			BarStyle style
	) {
		int bottom = hotbarTop() - BAR_GAP_ABOVE_HOTBAR;
		int top = bottom - BAR_HEIGHT;
		int width = right - left;

		fillRounded(graphics, left, top, right, bottom, BAR_CORNER, style.background());
		fillBarSection(graphics, left, top, bottom, width, fillRatio, style.fill());
		fillBarSection(
				graphics,
				left,
				top,
				bottom,
				width,
				Math.min(fillRatio, overlayRatio),
				style.overlay()
		);
		OsmiaRoundedRect.stroke(
				graphics,
				left,
				top,
				right,
				bottom,
				BAR_CORNER,
				style.border(),
				OsmiaRoundedRect.ROUND_BOTH,
				1
		);
	}

	private static void fillBarSection(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int bottom,
			int totalWidth,
			float ratio,
			int color
	) {
		int width = Math.round(totalWidth * clamp01(ratio));
		if (width > 0) {
			fillRounded(graphics, left, top, left + width, bottom, BAR_CORNER, color);
		}
	}

	private static int hotbarLeft() {
		return (MINECRAFT.getWindow().getGuiScaledWidth() - HOTBAR_WIDTH) / 2;
	}

	private static int hotbarTop() {
		return MINECRAFT.getWindow().getGuiScaledHeight() - SLOT_SIZE - 4;
	}

	private static float clamp01(float value) {
		return Math.clamp(value, 0.0F, 1.0F);
	}

	private static void fillRounded(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int color
	) {
		OsmiaRoundedRect.fill(
				graphics,
				left,
				top,
				right,
				bottom,
				radius,
				color,
				OsmiaRoundedRect.ROUND_BOTH
		);
	}

	private static void fillRoundedSection(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int sectionLeft,
			int sectionRight,
			int color
	) {
		int clippedLeft = Math.max(sectionLeft, left);
		int clippedRight = Math.min(sectionRight, right);
		if (clippedRight <= clippedLeft) {
			return;
		}

		int roundedSides = OsmiaRoundedRect.ROUND_NONE;
		if (clippedLeft == left) {
			roundedSides |= OsmiaRoundedRect.ROUND_LEFT;
		}
		if (clippedRight == right) {
			roundedSides |= OsmiaRoundedRect.ROUND_RIGHT;
		}

		OsmiaRoundedRect.fill(
				graphics,
				clippedLeft,
				top,
				clippedRight,
				bottom,
				radius,
				color,
				roundedSides
		);
	}

	private record BarStyle(int fill, int background, int border, int overlay) {
	}
}
