package net.fabricmc.bolu.old_villages.village;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class VillageDoorInfo {
	/**
	 * A block representing the door. Could be either upper or lower part.
	 */
	private final BlockPos doorBlockPos;
	private final Identifier dim;
	private int lastActivityTimestamp;

	public VillageDoorInfo(BlockPos pos, int timestamp, ServerWorld worldIn) {
		this.doorBlockPos = pos;
		this.lastActivityTimestamp = timestamp;
		DimensionType dimension = worldIn.getDimension();
		if (dimension.ultrawarm()) {
			this.dim = DimensionTypes.THE_NETHER_ID;
		} else if (!dimension.hasSkyLight()) {
			this.dim = DimensionTypes.THE_END_ID;
		} else {
			this.dim = DimensionTypes.OVERWORLD_ID;
		}
	}

	public VillageDoorInfo(BlockPos pos, int timestamp, Identifier dim) {
		this.doorBlockPos = pos;
		this.lastActivityTimestamp = timestamp;
		this.dim = dim;
	}

	/**
	 * Returns the squared distance between this door and the given coordinate.
	 */
	public int getDistanceToDoorBlockSq(BlockPos pos) {
		return (int) pos.getSquaredDistance(this.doorBlockPos);
	}

	public BlockPos getDoorBlockPos() {
		return this.doorBlockPos;
	}

	public int getLastActivityTimestamp() {
		return this.lastActivityTimestamp;
	}

	public void setLastActivityTimestamp(int timestamp) {
		this.lastActivityTimestamp = timestamp;
	}

	public Identifier getDimension() {
		return this.dim;
	}

	/**
	 * Checks if the village is in the dimension of the currently loaded world.
	 */
	public boolean isInSameDimension(ServerWorld worldIn) {
		DimensionType dimension = worldIn.getDimension();
		if (dimension.ultrawarm()) {
			return this.dim.equals(DimensionTypes.THE_NETHER_ID);
		} else if (!dimension.hasSkyLight()) {
			return this.dim.equals(DimensionTypes.THE_END_ID);
		}
		return this.dim.equals(DimensionTypes.OVERWORLD_ID);
	}
}
