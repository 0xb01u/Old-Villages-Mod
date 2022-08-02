package net.fabricmc.bolu.old_villages.village;

import com.google.common.collect.Lists;
import net.fabricmc.bolu.old_villages.render.RenderUtils;
import net.fabricmc.bolu.old_villages.render.SphereDrawMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VillageMarker {
	private VillageMarker() {}

	public static final Lock villageMarkerMutex = new ReentrantLock();

	public static final Color[] colors = {
			new Color(0xff0000),
			new Color(0xffa000),
			new Color(0xffff00),
			new Color(0x00ff00),
			new Color(0x00ffff),
			new Color(0x0000ff),
			new Color(0x8000ff),
			new Color(0xff00ff),
	};

	static HashMap<VillageInfo, Color> villageInfo = new HashMap<>();
	static List<DoorInfo> doors = new ArrayList<>();

	/* Settings */

	public static final int sphereDensity = 40;
	public static boolean drawPopulationDetectVolume = false;
	public static boolean drawGolemSpawnVolume = false;
	public static boolean drawDoorLines = false;

	public static SphereDrawMode villageSphereMode = SphereDrawMode.OFF;
	public static SphereDrawMode doorDetectSphereMode = SphereDrawMode.OFF;

	/**
	 * Draws the villages every frame.
	 */
	public static void renderVillages(float partialTicks) {
		final boolean drawRadius = drawPopulationDetectVolume
				|| villageSphereMode != SphereDrawMode.OFF
				|| doorDetectSphereMode != SphereDrawMode.OFF;
		if (!drawGolemSpawnVolume && drawRadius && drawDoorLines) {
			return;
		}

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			return;
		}

		DimensionType dim = player.world.getDimension();

		Vec3d playerPos = player.getPos();
		final double dx = player.prevX + (playerPos.x - player.prevX) * partialTicks;
		final double dy = player.prevY + (playerPos.y - player.prevY) * partialTicks;
		final double dz = player.prevZ + (playerPos.z - player.prevZ) * partialTicks;

		Color color = null;
		VillageInfo center = null;

		/* Render begins */

		RenderUtils.prepareOpenGL(true);

		/* Golem cage */ /* CURRENTLY UNUSED (bug in drawBox()) */
		if (drawGolemSpawnVolume && !villageInfo.isEmpty()) {
			for (Map.Entry<VillageInfo, Color> entry : villageInfo.entrySet()) {
				center = entry.getKey();
				color = entry.getValue();

				if ((center.dim == DimensionTypes.OVERWORLD_ID && dim.bedWorks())
						|| (center.dim == DimensionTypes.THE_NETHER_ID && dim.ultrawarm())
						|| (center.dim == DimensionTypes.THE_END_ID
							&& (!dim.hasSkyLight() && !dim.piglinSafe()))) {

					RenderUtils.drawBox(dx, dy, dz,
							center.pos.getX() - 8, center.pos.getY() - 3,
							center.pos.getZ() - 8,
							center.pos.getX() + 8, center.pos.getY() + 3,
							center.pos.getZ() + 8,
							color);
				}
			}
		}

		/* Things that use radius information */

		if (drawRadius && !villageInfo.isEmpty()) {
			for (Map.Entry<VillageInfo, Color> entry : villageInfo.entrySet()) {
				center = entry.getKey();
				int r = center.radius;
				color = entry.getValue();

				if ((center.dim == DimensionTypes.OVERWORLD_ID && dim.bedWorks())
						|| (center.dim == DimensionTypes.THE_NETHER_ID && dim.ultrawarm())
						|| (center.dim == DimensionTypes.THE_END_ID
							&& (!dim.hasSkyLight() && !dim.piglinSafe()))) {

					/* Population cage */ /* CURRENTLY UNUSED (bug in drawBox()) */
					if (drawPopulationDetectVolume) {
						RenderUtils.drawBox(dx, dy, dz,
								center.pos.getX() - r, center.pos.getY() - 4,
								center.pos.getZ() - r,
								center.pos.getX() + r, center.pos.getY() + 4,
								center.pos.getZ() + r,
								color);
					}

					/* Village and door detection spheres */
					if (villageSphereMode != SphereDrawMode.OFF) {
						RenderUtils.drawSphere(r, sphereDensity * 2, dx, dy, dz,
								center.pos.getX(), center.pos.getY(), center.pos.getZ(),
								color, villageSphereMode);
					}
					if (doorDetectSphereMode != SphereDrawMode.OFF) {
						RenderUtils.drawSphere(r + 32, sphereDensity * 2, dx,
								dy, dz,
								center.pos.getX(), center.pos.getY(), center.pos.getZ(),
								color, doorDetectSphereMode);
					}
				}
			}

			/* Doors */
			if (drawDoorLines) {
				for (DoorInfo doorInfo : doors) {
					center = doorInfo.village;
					color = doorInfo.color;

					for (BlockPos door : doorInfo.doorPos) {
						if ((center.dim == DimensionTypes.OVERWORLD_ID && dim.bedWorks())
								|| (center.dim == DimensionTypes.THE_NETHER_ID && dim.ultrawarm())
								|| (center.dim == DimensionTypes.THE_END_ID
									&& (!dim.hasSkyLight() && !dim.piglinSafe()))) {

							RenderUtils.drawLine(dx, dy, dz,
									door.getX(), door.getY(), door.getZ(),
									center.pos.getX(), center.pos.getY() + 0.01, center.pos.getZ(),
									color);
						}
					}
				}
			}

			/* Render ends */
			RenderUtils.prepareOpenGL(false);
		}
	}

	/**
	 * Fills the village render information list to draw the villages once per tick.
	 */
	public static void genLists() {
		villageInfo.clear();
		doors.clear();

		boolean drawRadius = drawPopulationDetectVolume
				|| villageSphereMode != SphereDrawMode.OFF || doorDetectSphereMode != SphereDrawMode.OFF;

		if (drawGolemSpawnVolume || drawRadius ||drawDoorLines) {
			List<Village> villages = VillageCollection.villages.getVillageList();

			int colorIndex = 0;
			for (Village village : villages) {
				VillageInfo
						center = new VillageInfo(village.getCenter(), village.getDimension(),
						village.getVillageRadius());
				Color color = colors[colorIndex++ % colors.length];

				if (drawGolemSpawnVolume || drawRadius) {
					villageInfo.put(center, color);
				}

				if (drawDoorLines) {
					DoorInfo door = new DoorInfo(center, color);

					for (VillageDoorInfo curDoor : village.villageDoorInfoList) {
						door.addDoor(curDoor.getDoorBlockPos());
					}
					doors.add(door);
				}
			}
		}
	}
}

class VillageInfo {
	final BlockPos pos;
	final Identifier dim;
	final int radius;

	VillageInfo(BlockPos pos, Identifier dim, int r) {
		this.pos = pos;
		this.dim = dim;
		this.radius = r;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof VillageInfo)) return false;
		VillageInfo that = (VillageInfo) o;
		return pos.equals(that.pos) && dim.equals(that.dim) && radius == that.radius;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, dim, radius);
	}
}

class DoorInfo {
	final VillageInfo village;
	final List<BlockPos> doorPos;
	final Color color;

	DoorInfo(VillageInfo center, Color color) {
		this.village = center;
		this.doorPos = Lists.newArrayList();
		this.color = color;
	}

	public void addDoor(BlockPos door) {
		doorPos.add(door);
	}
}
