package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.render.SphereDrawMode;
import net.fabricmc.bolu.old_villages.village.VillageMarker;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "processF3", at = @At("HEAD"), cancellable = true)
	public void processVillageMarker(int key, CallbackInfoReturnable<Boolean> cir) {
		if (key == 86) {
			if (!VillageMarker.drawDoorLines) {
				this.client.inGameHud.getChatHud().addMessage(MutableText.of(
						new LiteralTextContent("Debug village information: shown")));
				VillageMarker.drawDoorLines = true;
				VillageMarker.drawGolemSpawnVolume = true;
				VillageMarker.drawPopulationDetectVolume = true;
				VillageMarker.doorDetectSphereMode = SphereDrawMode.LINES;
				cir.setReturnValue(true);
			} else {
				this.client.inGameHud.getChatHud().addMessage(MutableText.of(
						new LiteralTextContent("Debug village information: hidden")));
				VillageMarker.drawDoorLines = false;
				VillageMarker.drawGolemSpawnVolume = false;
				VillageMarker.drawPopulationDetectVolume = false;
				VillageMarker.doorDetectSphereMode = SphereDrawMode.OFF;
				cir.setReturnValue(true);
			}
		}
	}
}
