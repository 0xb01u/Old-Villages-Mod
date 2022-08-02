package net.fabricmc.bolu.old_villages.village;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Village {
	private ServerWorld world;
	private Identifier dim;
	/**
	 * List of VillageDoorInfo objects.
	 */
	private final List<VillageDoorInfo> villageDoorInfoList = Lists.newArrayList();
	/**
	 * Sum of all door coordinates, used to calculate the village center by dividing by
	 * the number of doors.
	 */
	private BlockPos doorCoordsSum = BlockPos.ORIGIN;
	/**
	 * The village center.
	 */
	private BlockPos center = BlockPos.ORIGIN;
	private int radius;
	private int lastAddDoorTimestamp;
	private int tickCounter;
	private int numVillagers;
	private int numIronGolems;

	public Village() {}

	public Village(ServerWorld worldIn) {
		this.world = worldIn;
		DimensionType dimension = this.world.getDimension();
		if (dimension.isUltrawarm()) {
			this.dim = DimensionType.THE_NETHER_ID;
		} else if (dimension.hasEnderDragonFight()) {
			this.dim = DimensionType.THE_END_ID;
		} else {
			this.dim = DimensionType.OVERWORLD_ID;
		}
	}

	public void setServerWorld(ServerWorld worldIn) {
		this.world = worldIn;
	}

	/**
	 * Called periodically by VillageCollection.
	 */
	public void tick(int tickCounterIn) {
		if (!isInCurrentDimension()) {
			return;
		}

		this.tickCounter = tickCounterIn;
		this.removeDeadAndOutofRangeDoors();

		if (tickCounterIn % 20 == 0) {
			this.updateNumVillagers();
		}

		if (tickCounterIn % 30 == 0) {
			this.updateNumIronGolems();
		}

		int i = this.numVillagers / 10;

		if (this.numIronGolems < i && this.villageDoorInfoList.size() > 20
				&& this.world.random.nextInt(7000) == 0) {
			BlockPos spawnPos = this.findRandomSpawnPos(this.center);

			IronGolemEntity ironGolem =
					EntityType.IRON_GOLEM.create(this.world, null, null, null,
							spawnPos, SpawnReason.MOB_SUMMONED, false, false);
			if (ironGolem != null) {
				if (ironGolem.canSpawn(this.world, SpawnReason.MOB_SUMMONED) &&
						ironGolem.canSpawn(this.world)) {
					this.world.spawnEntityAndPassengers(ironGolem);
					++this.numIronGolems;
				} else {
					ironGolem.remove();
				}
			}
		}
	}

	private BlockPos findRandomSpawnPos(BlockPos pos) {
		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos = pos.add(this.world.random.nextInt(16) -  8,
					this.world.random.nextInt(6) - 3,
					this.world.random.nextInt(16) - 8);

			if (this.isBlockPosWithinSqVillageRadius(blockpos)) {
				return blockpos;
			}
		}
		return pos;
	}

	private void updateNumIronGolems() {
		List<IronGolemEntity> list = this.world.getNonSpectatingEntities(IronGolemEntity.class,
				new Box((this.center.getX() - this.radius),
						(this.center.getY() - 4),
						(this.center.getZ() - this.radius),
						(this.center.getX() + this.radius),
						(this.center.getY() + 4),
						(this.center.getZ() + this.radius)));
		this.numIronGolems = list.size();
	}

	private void updateNumVillagers() {
		List<VillagerEntity> list = this.world.getNonSpectatingEntities(VillagerEntity.class,
				new Box((this.center.getX() - this.radius),
						(this.center.getY() - 4),
						(this.center.getZ() - this.radius),
						(this.center.getX() + this.radius),
						(this.center.getY() + 4),
						(this.center.getZ() + this.radius)));
		this.numVillagers = list.size();
	}

	public BlockPos getCenter() {
		return this.center;
	}

	public int getVillageRadius() {
		return this.radius;
	}

	/**
	 * Checks if the distance squared between the BlockPos and the center of the village
	 * is less than the square of the village's radius.
	 */
	public boolean isBlockPosWithinSqVillageRadius(BlockPos pos) {
		return this.isInCurrentDimension()
				&& this.center.getSquaredDistance(pos) < this.radius * this.radius;
	}

	@Nullable
	public VillageDoorInfo getExistedDoor(BlockPos doorBlock) {
		if (!this.isInCurrentDimension()
				|| this.center.getSquaredDistance((Position) doorBlock, false) > this.radius * this.radius) {
			return null;
		}

		for (VillageDoorInfo curDoor : this.villageDoorInfoList) {
			BlockPos curDoorPos = curDoor.getDoorBlockPos();
			if (curDoorPos.getX() == doorBlock.getX() && curDoorPos.getZ() == doorBlock.getZ()
					&& Math.abs(curDoorPos.getY() - doorBlock.getY()) <= 1) {
				return curDoor;
			}
		}
		return null;
	}

	public void addVillageDoorInfo(VillageDoorInfo doorInfo) {
		if (!isInCurrentDimension()) {
			return;
		}

		this.villageDoorInfoList.add(doorInfo);
		this.doorCoordsSum = this.doorCoordsSum.add(doorInfo.getDoorBlockPos());
		this.updateVillageRadiusAndCenter();
		this.lastAddDoorTimestamp = doorInfo.getLastActivityTimestamp();
	}

	/**
	 * Checks whether there are doors left in the village.
	 */
	public boolean isAnnihilated() {
		return this.villageDoorInfoList.isEmpty();
	}

	/**
	 * Checks if the village is in the dimension of the currently loaded world.
	 */
	public boolean isInCurrentDimension() {
		DimensionType dimension = this.world.getDimension();
		if (dimension.isUltrawarm()) {
			return this.dim.equals(DimensionType.THE_NETHER_ID);
		} else if (dimension.hasEnderDragonFight()) {
			return this.dim.equals(DimensionType.THE_END_ID);
		}
		return this.dim.equals(DimensionType.OVERWORLD_ID);
	}

	private void removeDeadAndOutofRangeDoors() {
		boolean doorsRemoved = false;

		for (VillageDoorInfo curDoor : this.villageDoorInfoList) {
			if (!this.isWoodDoor(curDoor.getDoorBlockPos())
					|| Math.abs(this.tickCounter - curDoor.getLastActivityTimestamp()) > 1200) {
				this.doorCoordsSum = this.doorCoordsSum.subtract(curDoor.getDoorBlockPos());
				doorsRemoved = true;
			}
		}

		if (doorsRemoved) {
			this.updateVillageRadiusAndCenter();
		}
	}

	private boolean isWoodDoor(BlockPos pos) {
		BlockState state = this.world.getBlockState(pos);
		Block block = state.getBlock();

		if (block instanceof DoorBlock) {
			return state.getMaterial() == Material.WOOD;
		}

		return false;
	}

	private void updateVillageRadiusAndCenter() {
		int numDoors = this.villageDoorInfoList.size();

		if (numDoors == 0) {
			this.center = BlockPos.ORIGIN;
			this.radius = 0;
		} else {
			this.center = new BlockPos(this.center.getX() / numDoors,
					this.center.getY() / numDoors,
					this.center.getZ() / numDoors);

			int newRadius = 0;
			for (VillageDoorInfo curDoor : this.villageDoorInfoList) {
				newRadius = Math.max(curDoor.getDistanceToDoorBlockSq(this.center), newRadius);
			}

			this.radius = Math.max(32, (int) Math.sqrt(newRadius) + 1);
		}
	}

	/**
	 * Reads this village's data from NBT.
	 */
	public void readVillageDataFromNBT(CompoundTag compound) {
		this.numVillagers = compound.getInt("PopSize");
		this.radius = compound.getInt("Radius");
		this.numIronGolems = compound.getInt("Golems");
		this.lastAddDoorTimestamp = compound.getInt("Stable");
		this.tickCounter = compound.getInt("Tick");
		this.center = new BlockPos(compound.getInt("CX"), compound.getInt("CY"), compound.getInt("CZ"));
		this.doorCoordsSum = new BlockPos(compound.getInt("ACX"), compound.getInt("ACY"), compound.getInt("ACZ"));
		switch (compound.getInt("DIM")) {
			default:
			case 0:
				this.dim = DimensionType.OVERWORLD_ID;
				break;
			case -1:
				this.dim = DimensionType.THE_NETHER_ID;
				break;
			case 1:
				this.dim = DimensionType.THE_END_ID;
				break;
		}

		ListTag listTag = compound.getList("Doors", 10);

		for (int i = 0; i < listTag.size(); ++i) {
			CompoundTag tag = listTag.getCompound(i);
			VillageDoorInfo door = new VillageDoorInfo(
					new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")),
					tag.getInt("TS"));
			this.villageDoorInfoList.add(door);
		}
	}

	/**
	 * Writes this village's data to NBT.
	 */
	public void writeVillageDataToNBT(CompoundTag compound) {
		compound.putInt("PopSize", this.numVillagers);
		compound.putInt("Radius", this.radius);
		compound.putInt("Golems", this.numIronGolems);
		compound.putInt("Stable", this.lastAddDoorTimestamp);
		compound.putInt("Tick", this.tickCounter);
		compound.putInt("CX", this.center.getX());
		compound.putInt("CY", this.center.getY());
		compound.putInt("CZ", this.center.getZ());
		compound.putInt("ACX", this.doorCoordsSum.getX());
		compound.putInt("ACY", this.doorCoordsSum.getY());
		compound.putInt("ACZ", this.doorCoordsSum.getZ());
		if (this.dim.equals(DimensionType.THE_NETHER_ID)) {
			compound.putInt("DIM", -1);
		} else if (this.dim.equals((DimensionType.THE_END_ID))) {
			compound.putInt("DIM", 1);
		} else {
			compound.putInt("DIM", 0);
		}

		ListTag listTag = new ListTag();

		for (VillageDoorInfo curDoor : this.villageDoorInfoList) {
			CompoundTag tag = new CompoundTag();
			compound.putInt("X", curDoor.getDoorBlockPos().getX());
			compound.putInt("Y", curDoor.getDoorBlockPos().getY());
			compound.putInt("Z", curDoor.getDoorBlockPos().getZ());
			compound.putInt("TS", curDoor.getLastActivityTimestamp());
			listTag.add(tag);
		}
		compound.put("Doors", listTag);
	}
}
