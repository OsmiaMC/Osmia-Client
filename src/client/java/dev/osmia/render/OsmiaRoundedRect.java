package dev.osmia.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.osmia.OsmiaClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

public final class OsmiaRoundedRect {
	public static final int ROUND_NONE = 0;
	public static final int ROUND_LEFT = 1;
	public static final int ROUND_RIGHT = 2;
	public static final int ROUND_BOTH = ROUND_LEFT | ROUND_RIGHT;

	private static final VertexFormat VERTEX_FORMAT = VertexFormat.builder(0)
			.addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
			.addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
			.addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
			.addAttribute(DefaultVertexFormat.UV1_SEMANTIC_NAME, GpuFormat.RG16_SINT)
			.addAttribute(DefaultVertexFormat.UV2_SEMANTIC_NAME, GpuFormat.RG16_SINT)
			.build();

	private static final Identifier SHADER = Identifier.fromNamespaceAndPath(
			OsmiaClient.MOD_ID, "core/smooth_rounded_rect"
	);

	private static RenderPipeline pipeline;

	private OsmiaRoundedRect() {
	}

	public static void initialize() {
		if (pipeline != null) {
			return;
		}

		pipeline = RenderPipelines.register(
				RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
						.withLocation(Identifier.fromNamespaceAndPath(
								OsmiaClient.MOD_ID, "pipeline/smooth_rounded_rect"
						))
						.withVertexShader(SHADER)
						.withFragmentShader(SHADER)
						.withVertexBinding(0, VERTEX_FORMAT)
						.withPrimitiveTopology(PrimitiveTopology.QUADS)
						.withCull(false)
						.build()
		);
	}

	private static RenderPipeline pipeline() {
		if (pipeline == null) {
			throw new IllegalStateException("Rounded rectangle renderer is not initialized");
		}
		return pipeline;
	}

	public static void fill(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int color,
			int roundedSides
	) {
		submit(graphics, left, top, right, bottom, radius, color, roundedSides, 0);
	}

	public static void stroke(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int color,
			int roundedSides,
			int strokeWidth
	) {
		submit(
				graphics,
				left,
				top,
				right,
				bottom,
				radius,
				color,
				roundedSides,
				Math.max(1, strokeWidth)
		);
	}

	private static void submit(
			GuiGraphicsExtractor graphics,
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int color,
			int roundedSides,
			int strokeWidth
	) {
		int width = right - left;
		int height = bottom - top;
		if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
			return;
		}

		int safeRadius = Math.clamp(radius, 0, Math.min(width, height) / 2);
		int safeSides = roundedSides & ROUND_BOTH;
		Matrix3x2f pose = new Matrix3x2f(graphics.pose());
		ScreenRectangle scissor = graphics.scissorStack.peek();
		ScreenRectangle bounds = new ScreenRectangle(left, top, width, height)
				.transformMaxBounds(pose);
		if (scissor != null) {
			bounds = scissor.intersection(bounds);
			if (bounds == null) {
				return;
			}
		}

		graphics.guiRenderState.addGuiElement(new RoundedRectState(
				left,
				top,
				right,
				bottom,
				safeRadius,
				safeSides,
				strokeWidth,
				color,
				pose,
				scissor,
				bounds
		));
	}

	private record RoundedRectState(
			int left,
			int top,
			int right,
			int bottom,
			int radius,
			int roundedSides,
			int strokeWidth,
			int color,
			Matrix3x2f pose,
			ScreenRectangle scissorArea,
			ScreenRectangle bounds
	) implements GuiElementRenderState {
		@Override
		public void buildVertices(VertexConsumer vertices) {
			int width = right - left;
			int height = bottom - top;
			int parameters = roundedSides | (strokeWidth << 2);

			vertex(vertices, left, top, 0.0F, 0.0F, width, height, parameters);
			vertex(vertices, left, bottom, 0.0F, 1.0F, width, height, parameters);
			vertex(vertices, right, bottom, 1.0F, 1.0F, width, height, parameters);
			vertex(vertices, right, top, 1.0F, 0.0F, width, height, parameters);
		}

		private void vertex(
				VertexConsumer vertices,
				int x,
				int y,
				float u,
				float v,
				int width,
				int height,
				int parameters
		) {
			vertices.addVertexWith2DPose(pose, x, y)
					.setUv(u, v)
					.setColor(color)
					.setUv1(width, height)
					.setUv2(radius, parameters);
		}

		@Override
		public RenderPipeline pipeline() {
			return OsmiaRoundedRect.pipeline();
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.noTexture();
		}
	}
}
