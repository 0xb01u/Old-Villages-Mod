package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.village.VillageMarker;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.locks.Lock;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Inject(method = "render", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/render/Camera;)V",
			shift = At.Shift.BEFORE))
	public void renderVillages(MatrixStack matrices, float tickDelta, long limitTime,
	                           boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
	                           LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f,
	                           CallbackInfo ci) {
		Lock mutex = VillageMarker.villageMarkerMutex;
		mutex.lock();
		try {
			VillageMarker.renderVillages(tickDelta);
		} finally {
			mutex.unlock();
		}
	}
}
