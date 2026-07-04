package dev.osmia.mixin;

import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import dev.osmia.OsmiaClient;
import dev.osmia.gui.OsmiaSettingsScreen;
import dev.osmia.gui.OsmiaTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
	private static final Identifier OSMIA_LOGO = Identifier.fromNamespaceAndPath(
			OsmiaClient.MOD_ID,
			"textures/gui/osmia_title.png"
	);
	private static final int OSMIA_LOGO_WIDTH = 256;
	private static final int OSMIA_LOGO_HEIGHT = 71;
	private static final int OSMIA_LOGO_TEXTURE_WIDTH = 2048;
	private static final int OSMIA_LOGO_TEXTURE_HEIGHT = 567;

	@Shadow
	private RealmsNotificationsScreen realmsNotificationsScreen;

	protected TitleScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void osmia$removeFooterIconButtons(CallbackInfo callbackInfo) {
		int buttonY = -1;
		Component realmsLabel = Component.translatable("menu.online");

		for (GuiEventListener child : List.copyOf(children())) {
			if (child instanceof SpriteIconButton iconButton) {
				buttonY = iconButton.getY();
				removeWidget(child);
			} else if (child instanceof Button button && button.getMessage().equals(realmsLabel)) {
				removeWidget(child);
			} else if (child instanceof PlainTextButton) {
				removeWidget(child);
			}
		}

		realmsNotificationsScreen = null;

		if (buttonY >= 0) {
			addRenderableWidget(Button.builder(
					osmia$settingsLabel(),
					button -> minecraft.gui.setScreen(new OsmiaSettingsScreen(this))
			).bounds(width / 2 - 100, buttonY, 200, 20).build());
		}

		osmia$joinMenuButtons();
	}

	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
			)
	)
	private void osmia$replaceVersionText(
			GuiGraphicsExtractor graphics,
			Font font,
			String vanillaVersion,
			int x,
			int y,
			int color
	) {
		graphics.text(font, OsmiaTheme.gradient("Osmia 26.2 (0.1v)"), x, y, color);
	}

	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/LogoRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V"
			)
	)
	private void osmia$renderLogo(
			LogoRenderer vanillaLogo,
			GuiGraphicsExtractor graphics,
			int screenWidth,
			float alpha
	) {
		int x = (screenWidth - OSMIA_LOGO_WIDTH) / 2;
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				OSMIA_LOGO,
				x,
				24,
				0.0F,
				0.0F,
				OSMIA_LOGO_WIDTH,
				OSMIA_LOGO_HEIGHT,
				OSMIA_LOGO_TEXTURE_WIDTH,
				OSMIA_LOGO_TEXTURE_HEIGHT,
				OSMIA_LOGO_TEXTURE_WIDTH,
				OSMIA_LOGO_TEXTURE_HEIGHT,
				ARGB.white(alpha)
		);
	}

	@Redirect(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/SplashRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/client/gui/Font;F)V"
			)
	)
	private void osmia$hideSplashText(
			SplashRenderer splash,
			GuiGraphicsExtractor graphics,
			int screenWidth,
			Font font,
			float alpha
	) {
	}

	private static Component osmia$settingsLabel() {
		return OsmiaTheme.gradient("Osmia")
				.append(Component.literal(" Settings"));
	}

	private void osmia$joinMenuButtons() {
		int menuTop = height / 4 + 44;
		int menuLeft = width / 2 - 100;
		Component singleplayerLabel = Component.translatable("menu.singleplayer");
		Component multiplayerLabel = Component.translatable("menu.multiplayer");

		List<Button> primaryButtons = children().stream()
				.filter(Button.class::isInstance)
				.map(Button.class::cast)
				.filter(button -> button.getWidth() == 200)
				.sorted(Comparator.comparingInt(Button::getY))
				.toList();

		int fullWidthRow = 1;
		for (Button button : primaryButtons) {
			if (button.getMessage().equals(singleplayerLabel)) {
				button.setX(menuLeft);
				button.setY(menuTop);
				button.setWidth(100);
			} else if (button.getMessage().equals(multiplayerLabel)) {
				button.setX(menuLeft + 100);
				button.setY(menuTop);
				button.setWidth(100);
			} else {
				button.setX(menuLeft);
				button.setY(menuTop + fullWidthRow * 20);
				button.setWidth(200);
				fullWidthRow++;
			}
		}

		List<Button> bottomButtons = children().stream()
				.filter(Button.class::isInstance)
				.map(Button.class::cast)
				.filter(button -> button.getWidth() == 98)
				.sorted(Comparator.comparingInt(Button::getX))
				.toList();

		for (int index = 0; index < bottomButtons.size(); index++) {
			Button button = bottomButtons.get(index);
			button.setX(menuLeft + index * 100);
			button.setY(menuTop + fullWidthRow * 20);
			button.setWidth(100);
		}
	}
}
