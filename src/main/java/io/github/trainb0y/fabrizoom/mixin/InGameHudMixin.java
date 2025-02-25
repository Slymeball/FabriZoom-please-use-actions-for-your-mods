package io.github.trainb0y.fabrizoom.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.trainb0y.fabrizoom.ZoomLogic;
import io.github.trainb0y.fabrizoom.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles rendering the Zoom Overlay
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {
	/**
	 * Internal identifier for the Zoom Overlay png
	 */
	private static final Identifier ZOOM_OVERLAY = new Identifier("fabrizoom:textures/misc/zoom_overlay.png");

	@Shadow
	@Final
	private MinecraftClient client;


	/**
	 * Renders the Zoom Overlay
	 */
	@Inject(
			at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerInventory.getArmorStack(I)Lnet/minecraft/item/ItemStack;"),
			method = "render(Lnet/minecraft/client/gui/DrawContext;F)V"
	)
	public void injectZoomOverlay(DrawContext matrix, float tickDelta, CallbackInfo info) {
		if (!Config.getValues().getZoomOverlayEnabled()) return;
		if (ZoomLogic.getZoomDivisor() == 1.0) return;

		float f;

		if (Config.getValues().getTransition() != Config.Transition.NONE) { // smooth and linear transition
			f = 1 - MathHelper.lerp(tickDelta, ZoomLogic.getLastZoomOverlayAlpha(), ZoomLogic.getZoomOverlayAlpha());
		} else { // no transition
			f = ZoomLogic.getZoomOverlayAlpha();
		}

		// Bunch of rendering wizardry from LibZoomer
		// I'm wayyy too dumb to come up with this myself
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);

		RenderSystem.setShaderTexture(0, ZOOM_OVERLAY);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bufferBuilder.vertex(0.0D, this.client.getWindow().getScaledHeight(), -90.0D).texture(0.0F, 1.0F).next();
		bufferBuilder.vertex(this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), -90.0D).texture(1.0F, 1.0F).next();
		bufferBuilder.vertex(this.client.getWindow().getScaledWidth(), 0.0D, -90.0D).texture(1.0F, 0.0F).next();
		bufferBuilder.vertex(0.0D, 0.0D, -90.0D).texture(0.0F, 0.0F).next();
		tessellator.draw();

		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

	}
}