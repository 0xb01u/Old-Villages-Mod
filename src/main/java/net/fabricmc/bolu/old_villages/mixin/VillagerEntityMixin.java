package net.fabricmc.bolu.old_villages.mixin;

import net.fabricmc.bolu.old_villages.village.VillageCollection;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {
	private VillagerEntityMixin(EntityType a, World b) { super(a, b); }

	private int randomTickDivider = 0;

	@Inject(method = "mobTick", at = @At("HEAD"))
	public void detectDoords(CallbackInfo ci) {
		if (--this.randomTickDivider <= 0) {
			BlockPos pos = this.getBlockPos();
			VillageCollection.villages.addToVillagerPositionList(pos, (ServerWorld) this.getEntityWorld());
			this.randomTickDivider = 70 + this.random.nextInt(50);
		}
	}
}
