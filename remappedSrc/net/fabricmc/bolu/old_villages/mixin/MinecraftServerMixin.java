package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.village.VillageCollection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Redirect(method = "createWorlds", at = @At(
			value = "NEW",
			target = "Lnet/minecraft/server/world/ServerWorld;<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/gen/chunk/ChunkGenerator;ZJLjava/util/List;Z)V",
			ordinal = 0
	))
	public ServerWorld addOldVillages(
			MinecraftServer server, Executor workerExecutor,
			LevelStorage.Session session, ServerWorldProperties properties,
			RegistryKey<World> registryKey, DimensionType dimensionType,
			WorldGenerationProgressListener worldGenerationProgressListener,
			ChunkGenerator chunkGenerator, boolean debugWorld,
			long l, List<Spawner> list, boolean bl) {
		ServerWorld serverWorld = new ServerWorld(server, workerExecutor, session, properties, registryKey,
				dimensionType, worldGenerationProgressListener, chunkGenerator, debugWorld, l, list, bl);

		PersistentStateManager persistentStateManager = serverWorld.getPersistentStateManager();
		VillageCollection.villages = persistentStateManager.getOrCreate(VillageCollection::new, "old_villages");

		return serverWorld;
	}
}
