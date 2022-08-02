package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.village.VillageCollection;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

	@Inject(method = "tick", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/village/raid/RaidManager;tick()V",
			ordinal = 0
	))
	public void tickVillages(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (VillageCollection.villages != null) {
			VillageCollection.villages.tick();
		}
	}
}
