package net.fabricmc.bolu.old_villages.village;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;

public class VillageDoorInfo {
	/**
	 * A block representing the door. Could be either upper or lower part.
	 */
	private final BlockPos doorBlockPos;
	private int lastActivityTimestamp;

	public VillageDoorInfo(BlockPos pos,  int timestamp) {
		this.doorBlockPos = pos;
		this.lastActivityTimestamp = timestamp;
	}

	/**
	 * Returns the squared distance between this door and the given coordinate.
	 */
	public int getDistanceToDoorBlockSq(BlockPos pos) {
		return (int) pos.getSquaredDistance((Position) this.doorBlockPos, false);
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
}
