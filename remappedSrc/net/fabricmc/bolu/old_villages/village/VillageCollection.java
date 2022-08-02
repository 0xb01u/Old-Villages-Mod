package net.fabricmc.bolu.old_villages.village;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class VillageCollection extends PersistentState {
	public static VillageCollection villages = null;

	private ServerWorld world;

	private final List <BlockPos> villagerPositionsList = Lists.newArrayList();
	private final List<VillageDoorInfo> newDoors = Lists.newArrayList();
	private final List<Village> villageList = Lists.newArrayList();
	private int tickCounter;

	public VillageCollection() {
		super("old_villages");
	}

	public VillageCollection(ServerWorld worldIn) {
		super("old_villages");
		this.world = worldIn;
		this.markDirty();
	}

	public void setWorldsForAll(ServerWorld worldIn) {
		this.world = worldIn;

		for (Village village : this.villageList) {
			village.setServerWorld(worldIn);
		}
	}

	public void addToVillagerPositionList(BlockPos pos) {
		if (this.villagerPositionsList.size() <= 64) {
			if (!this.positionInList(pos)) {
				this.villagerPositionsList.add(pos);
			}
		}
	}

	/**
	 * Runs a single tick for the village collection.
	 */
	public void tick() {
		System.out.println("tick");
		++this.tickCounter;

		for (Village village : this.villageList) {
			village.tick(this.tickCounter);
		}

		this.removeAnnihilatedVillages();
		this.detectDoors();
		this.addNewDoorsToVillageOrCreateVillage();

		if (this.tickCounter % 400 == 0) {
			this.markDirty();
		}
	}

	private void removeAnnihilatedVillages() {
		Iterator<Village> it = this.villageList.iterator();

		while (it.hasNext()) {
			Village village = it.next();

			if (village.isInCurrentDimension() && village.isAnnihilated()) {
				it.remove();
				this.markDirty();
			}
		}
	}

	/**
	 * Get a list of villages.
	 */
	public List<Village> getVillageList() {
		return this.villageList;
	}

	@Nullable
	public Village getNearestVillage(BlockPos doorBlock, int radius) {
		Village village = null;
		double dist = 3.4028234663852886E38D;

		for (Village curVillage : this.villageList) {
			if (!curVillage.isInCurrentDimension()) {
				continue;
			}

			double d = curVillage.getCenter().getSquaredDistance((Position) doorBlock, false);

			if (d < dist) {
				float f = radius + curVillage.getVillageRadius();

				if (d <= f * f) {
					village = curVillage;
					dist = d;
				}
			}
		}

		return village;
	}

	@Nullable
	public Village getOldestVillage(BlockPos doorBlock, int radius) {
		for (Village curVillage : this.villageList) {
			if (!curVillage.isInCurrentDimension()) {
				continue;
			}

			int d = (int) curVillage.getCenter().getSquaredDistance((Position) doorBlock, false);
			int f = radius + curVillage.getVillageRadius();

			if (d < f * f) {
				return curVillage;
			}
		}

		return null;
	}

	private void detectDoors() {
		if (!this.villagerPositionsList.isEmpty()) {
			this.addDoorsAround(this.villagerPositionsList.remove(0));
		}
	}

	private void addNewDoorsToVillageOrCreateVillage() {
		for (int i = 0; i < this.newDoors.size(); ++i) {
			VillageDoorInfo door = this.newDoors.get(i);
			BlockPos doorPos = door.getDoorBlockPos();
			Village village;
			if (isOakDoor(door.getDoorBlockPos())) {
				village = getOldestVillage(doorPos, 32);
			} else {
				village = getNearestVillage(doorPos, 32);
			}

			if (village == null) {
				village = new Village(this.world);
				this.villageList.add(village);
				this.markDirty();
			}

			village.addVillageDoorInfo(door);
		}

		this.newDoors.clear();
	}

	private void addDoorsAround(BlockPos central) {
		for (int i = -16; i < 16; ++i) {
			for (int j = -4; j < 4; ++j) {
				for (int k = -16; k < 16; ++k) {
					BlockPos pos = central.add(i, j, k);

					if (this.isWoodDoor(pos)) {
						VillageDoorInfo door = this.checkDoorExistence(pos);

						if (door == null) {
							this.addToNewDoorsList(pos);
						} else {
							door.setLastActivityTimestamp(this.tickCounter);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the {@link VillageDoorInfo} if it exists in any village or in the new doors list,
	 * otherwise returns null.
	 */
	@Nullable
	private VillageDoorInfo checkDoorExistence(BlockPos doorPos) {
		for (VillageDoorInfo door : this.newDoors) {
			BlockPos pos = door.getDoorBlockPos();
			if (pos.equals(doorPos)) {
				return door;
			}
		}

		for (Village village : this.villageList) {
			VillageDoorInfo door = village.getExistedDoor(doorPos);

			if (door != null) {
				return door;
			}
		}

		return null;
	}

	/**
	 * Checks if the given door is a valid village house door i.e. has an interior (with a roof) and
	 * an exterior.
	 */
	private void addToNewDoorsList(BlockPos doorPos) {
		Direction facing = this.world.getBlockState(doorPos).get(DoorBlock.FACING);
		Direction opposite = facing.getOpposite();
		int skyFront = this.countBlocksCanSeeSky(doorPos, facing, 5);
		int skyBack = this.countBlocksCanSeeSky(doorPos, opposite, skyFront + 1);
		System.out.println("Facing: " + facing + " w/ " + skyFront + " - Opposite:" + opposite + " w/ " + skyBack);

		if (skyFront != skyBack) {
			this.newDoors.add(new VillageDoorInfo(doorPos, this.tickCounter));
		}
	}

	/**
	 * Checks up to five blocks in the given direction for sky access.
	 * {@code centerPos} is not checked.
	 */
	private int countBlocksCanSeeSky(BlockPos centerPos, Direction direction, int limitation) {
		int skyAccess = 0;

		for (int i = 1; i <= 5; ++i) {
			if (this.world.isSkyVisible(centerPos.offset(direction, i))) {
				++skyAccess;

				if (skyAccess > limitation) {
					break;
				}
			}
		}

		return skyAccess;
	}

	private boolean positionInList(BlockPos pos) {
		for (BlockPos blockPos : this.villagerPositionsList) {
			if (blockPos.equals(pos)) {
				return true;
			}
		}

		return false;
	}

	private boolean isWoodDoor(BlockPos doorPos) {
		BlockState state = this.world.getBlockState(doorPos);
		Block door = state.getBlock();

		if (door instanceof DoorBlock) {
			return state.getMaterial() == Material.WOOD;
		}

		return false;
	}

	private boolean isOakDoor(BlockPos pos) {
		BlockState state = this.world.getBlockState(pos);
		Block block = state.getBlock();

		if (block instanceof DoorBlock) {
			return state.getBlock() == Blocks.OAK_DOOR;
		}

		return false;
	}

	/**
	 * Read the data from NBT.
	 */
	public void fromTag(CompoundTag compound) {
		this.tickCounter = compound.getInt("Tick");
		ListTag listTag = compound.getList("Villages", 10);

		for (int i = 0; i < listTag.size(); ++i) {
			CompoundTag tag = listTag.getCompound(i);
			Village village = new Village();
			village.readVillageDataFromNBT(tag);
			this.villageList.add(village);
		}
	}

	/**
	 * Write the data from NBT.
	 */
	public CompoundTag toTag(CompoundTag compound) {
		compound.putInt("Tick", this.tickCounter);
		ListTag listTag = new ListTag();

		for (Village village : this.villageList) {
			CompoundTag tag = new CompoundTag();
			village.writeVillageDataToNBT(tag);
			listTag.add(tag);
		}

		compound.put("Villages", listTag);
		return compound;
	}
}
