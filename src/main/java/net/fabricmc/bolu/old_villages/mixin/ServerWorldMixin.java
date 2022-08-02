package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.village.VillageCollection;
import net.fabricmc.bolu.old_villages.village.VillageMarker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
	private final ServerWorld THIS = ((ServerWorld) (Object) this);

	@Inject(method = "<init>", at = @At("RETURN"))
	public void addOldVillages(
			MinecraftServer server, Executor workerExecutor,
			LevelStorage.Session session, ServerWorldProperties properties,
			RegistryKey<World> worldKey, DimensionOptions dimensionOptions,
			WorldGenerationProgressListener worldGenerationProgressListener,
			boolean debugWorld, long seed, List<Spawner> spawners,
			boolean shouldTickTime, CallbackInfo ci) {
		if (THIS.getDimension().bedWorks()) {
			return;
		}

		PersistentStateManager persistentStateManager = THIS.getPersistentStateManager();
		VillageCollection.villages = persistentStateManager.getOrCreate(
				VillageCollection::fromNbt, VillageCollection::new, VillageCollection.name());

		System.out.println("[Old Villages Mod] Loaded villages data.");
	}

	@Inject(method = "tick", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/village/raid/RaidManager;tick()V",
			ordinal = 0
	))
	public void tickVillages(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (VillageCollection.villages != null) {
			VillageCollection.villages.tick(THIS);

			Lock mutex = VillageMarker.villageMarkerMutex;
			mutex.lock();
			try {
				VillageMarker.genLists();
			} finally {
				mutex.unlock();
			}
		}
	}
}
