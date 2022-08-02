package net.fabricmc.bolu.old_villages.village;

import com.google.common.collect.Lists;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class VillageCollection extends PersistentState {
	public static VillageCollection villages = null;
    private static final String OLD_VILLAGES = "old_villages";

	private ServerWorld world;

	private final List<VillagerWorldInfo> villagerPositionsList = Lists.newArrayList();
	private final List<VillageDoorInfo> newDoors = Lists.newArrayList();
	private final List<Village> villageList = Lists.newArrayList();
	private int tickCounter = 0;

	public VillageCollection() {}

	public void addToVillagerPositionList(BlockPos pos, ServerWorld worldIn) {
		VillagerWorldInfo villager = new VillagerWorldInfo(pos, worldIn);
		if (this.villagerPositionsList.size() <= 64 && !this.positionInList(villager)) {
			this.villagerPositionsList.add(villager);
		}
	}

	/**
	 * Runs a single tick for the village collection.
	 */
	public void tick(ServerWorld worldIn) {
		this.world = worldIn;
		if (this.world.getDimension().bedWorks()) {
			++this.tickCounter;
		}

		for (Village village : this.villageList) {
			village.tick(this.tickCounter, this.world);
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

			if (village.isInSameDimension(this.world) && village.isAnnihilated()) {
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
	public Village getNearestVillage(VillageDoorInfo door, int radius) {
		Village village = null;
		double dist = 3.4028234663852886E38D;

		for (Village curVillage : this.villageList) {
			if (!curVillage.isInSameDimension(door.getDimension())) {
				continue;
			}

			BlockPos doorPos = door.getDoorBlockPos();
			double d = curVillage.getCenter()
					.getSquaredDistance(doorPos.getX(), doorPos.getY(), doorPos.getZ());

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
	public Village getOldestVillage(VillageDoorInfo door, int radius) {
		for (Village curVillage : this.villageList) {
			if (!curVillage.isInSameDimension(door.getDimension())) {
				continue;
			}

			BlockPos doorPos = door.getDoorBlockPos();
			int d = (int) curVillage.getCenter()
					.getSquaredDistance(doorPos.getX(), doorPos.getY(), doorPos.getZ());
			int f = radius + curVillage.getVillageRadius();

			if (d <= f * f) {
				return curVillage;
			}
		}

		return null;
	}

	private void detectDoors() {
		if (!this.villagerPositionsList.isEmpty()) {
			for (int i = 0; i < this.villagerPositionsList.size(); ++i) {
				VillagerWorldInfo villager = this.villagerPositionsList.get(i);
				if (villager.world.getDimension().equals(this.world.getDimension())) {
					this.addDoorsAround(this.villagerPositionsList.remove(i));
					break;
				}
			}
		}
	}

	private void addNewDoorsToVillageOrCreateVillage() {
		for (VillageDoorInfo door : this.newDoors) {
			System.out.println("Adding door: " + door.getDoorBlockPos());
			Village village;
			if (isOakDoor(door.getDoorBlockPos())) {
				village = getOldestVillage(door, 32);
			} else {
				village = getNearestVillage(door, 32);
			}

			if (village == null) {
				village = new Village(this.world);
				this.villageList.add(village);
				this.markDirty();
				System.out.println("New village created. Total: " + villageList.size());
			}

			village.addVillageDoorInfo(door);
		}

		this.newDoors.clear();
	}

	private void addDoorsAround(VillagerWorldInfo villager) {
		BlockPos central = villager.pos;
		ServerWorld worldIn = villager.world;

		/* This check should be unnecessary */
		if (!worldIn.getDimension().equals(this.world.getDimension())) {
			this.villagerPositionsList.add(villager);
			return;
		}

		for (int i = -16; i < 16; ++i) {
			for (int j = -4; j < 4; ++j) {
				for (int k = -16; k < 16; ++k) {
					BlockPos pos = central.add(i, j, k);

					if (this.isWoodDoor(pos)) {
						VillageDoorInfo door = this.checkDoorExistence(pos);

						if (door == null) {
							this.addToNewDoorsList(pos, worldIn);
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
			if (door.isInSameDimension(this.world)
					&& pos.getX() == doorPos.getX()
					&& pos.getZ() == doorPos.getZ()
					&& Math.abs(pos.getY() - doorPos.getY()) <= 1) {
				return door;
			}
		}

		for (Village village : this.villageList) {
			VillageDoorInfo door = village.getExistedDoor(doorPos, this.world);

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
	private void addToNewDoorsList(BlockPos doorPos, ServerWorld worldIn) {
		Direction facing = worldIn.getBlockState(doorPos).get(DoorBlock.FACING);
		Direction opposite = facing.getOpposite();
		int skyFront = this.countBlocksCanSeeSky(worldIn, doorPos, facing, 5);
		int skyBack = this.countBlocksCanSeeSky(worldIn, doorPos, opposite, skyFront + 1);

		if (skyFront != skyBack) {
			this.newDoors.add(new VillageDoorInfo(doorPos, this.tickCounter, worldIn)); // Created with villager world data
		}
	}

	/**
	 * Checks up to five blocks in the given direction for sky access.
	 * {@code centerPos} is not checked.
	 */
	private int countBlocksCanSeeSky(ServerWorld worldIn, BlockPos centerPos, Direction direction,
	                                 int limitation) {
		int skyAccess = 0;

		for (int i = 1; i <= 5; ++i) {
			if (worldIn.isSkyVisible(centerPos.offset(direction, i))) {
				++skyAccess;

				if (skyAccess > limitation) {
					break;
				}
			}
		}

		return skyAccess;
	}

	private boolean positionInList(VillagerWorldInfo pos) {
		for (VillagerWorldInfo villager : this.villagerPositionsList) {
			if (villager.equals(pos)) {
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

	public static String name() {
		return OLD_VILLAGES;
	}

	/**
	 * Read the data from NBT.
	 */
	public static VillageCollection fromNbt(NbtCompound compound) {
		VillageCollection vc = new VillageCollection();

		vc.tickCounter = compound.getInt("Tick");
		NbtList nbtList = compound.getList("Villages", 10);

		for (int i = 0; i < nbtList.size(); ++i) {
			NbtCompound tag = nbtList.getCompound(i);
			Village village = new Village();
			village.readVillageDataFromNBT(tag);
			vc.villageList.add(village);
		}

		System.out.println("Loaded village collection from NBT");
		return vc;
	}

	/**
	 * Write the data from NBT.
	 */
	public NbtCompound writeNbt(NbtCompound compound) {
		compound.putInt("Tick", this.tickCounter);
		NbtList nbtList = new NbtList();

		for (Village village : this.villageList) {
			NbtCompound tag = new NbtCompound();
			village.writeVillageDataToNBT(tag);
			nbtList.add(tag);
		}
		compound.put("Villages", nbtList);

		return compound;
	}
}

/**
 * Contains the BlockPos and the world (dimension) a Villager is, to detect doors.
 */
class VillagerWorldInfo {
	BlockPos pos;
	ServerWorld world;

	VillagerWorldInfo(BlockPos pos, ServerWorld world) {
		this.pos = pos;
		this.world = world;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof VillagerWorldInfo)) {
			return false;
		}
		VillagerWorldInfo that = (VillagerWorldInfo) other;
		return this.pos.equals(that.pos) && this.world.getDimension().equals(that.world.getDimension());
	}
}
