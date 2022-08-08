# Old Village Mechanics Mod

Minecraft 1.16.5 Java Edition fabric mod that brings back the old village mechanics related to iron
farming to the game.

This mod brings back both the
[1.2-1.7 mechanics and the 1.8-1.13 mechanics at the same time](#using-12-17-or-18-113-mechanics),
so no old iron farm is left out. [No deliberate changes](#bugs-and-changes-to-the-old-village-mechanics)
have been made to those mechanics, and they should work as they did in their respective versions.

To help confirm that the mod is working properly, it also adds a [Village Marker utility](#village-marker),
similar to that found in multiple old Minecraft mods.

This mod also **does not remove** the current village mechanics, only adds back the old ones on top.

## Old village mechanics not added to the mod

Only those mechanics needed for old iron farms to work have been implemented.

Mechanics inconsequential to old iron farming that have **not** been implemented include:
 - Old Villager trading system.
 - Old Villager breeding mechanics.
 - Player reputation system.
 - Village aggressors system.

All those mechanics already have replacements in the newer vanilla versions, so nothing
should feel missing.

## Differences between 1.2-1.7 and 1.8-1.13 village mechanics

In 1.2-1.7 versions of Minecraft Java Edition, a valid village door would be added to the **oldest**
village in range (or create a new village if no other village was in range). In 1.8-1.13 versions,
a valid village door would be added to the **closest** village in range (or create a new village).

## Using 1.2-1.7 or 1.8-1.13 mechanics

To use 1.2-1.7 mechanics, just use Oak Doors as village doors. To use 1.8-1.13 mechanics, use
any other kind of wooden doors.

The reasoning behind this feature is that Oak Doors were the only existing wooden doors in
1.2-1.7, and I feel it is only appropriate to make them the key to those versions' mechanics.

## Village Marker

The mod adds a village marker utility to check existing villages and their properties.
To activate or deactivate the marker, press **F3+V**. The marker is similar to the most popular
old Village Marker mods, and features up to 8 different village colors.

The marker currently only shows the lines connecting village doors to the center of their
village, as well as the door detection sphere for the villages (a sphere centered at the village
center with radius equal to the village radius + 32).

The marker currently does not show:
 - Population cages, due to a bug in the cage rendering code.
 - Golem spawn cages, due to the same bug in the cage rendering code.
 - Village spheres, as they do not add any useful information.

Currently, the marker is not customizable.

## Bugs and changes to the old village mechanics

As stated before, no deliberate changes have been made to the old village mechanics. However, due
to how Minecraft has changed itself, some mechanics behave differently.
 - Chunk loading (namely spawn chunks) was completely reworked in later versions of Minecraft. Thus, village-stacking iron farms are likely to break more easily.
 - Minecraft changed the way it checks whether a block has sky access, and now old villages can only be created in the Overworld.
 - Changes to redstone components have been made that might break the reset of some rebuildable farms if unmodified.
 - The conditions that allow Iron Golems to spawn have changed, so some old spawning platforms don't work as efficiently any more.

Aside from that, some bugs have been noticed:
 - It has been noticed that, [when resetting old iron farms](#old-iron-farms-compatibility-list), sometimes and randomly, newly detected doors are added to an incorrect village. Whether this is caused by the mod itself (i.e. a hidden-from-plain-sight bug in the villager door detection code), lag spikes during the reset process, or any other change to Minecraft from a more recent version is yet to be identified.
 - Concerning the Village Marker, the height (y coordinate) of the vertex of the door lines is not properly computed. It's not a big deal, but it is noticeable.

## Why 1.16.5?

I am aware that the latest Minecraft version at the time of first releasing this mod is 1.19.1.
However, I am currently uninterested in playing Minecraft versions newer to The Nether Update, as I don't find them
engaging or fun enough. I will probably update the mod to 1.19 (not 1.19.1, nobody should play that version,
thank you, Microsoft) as I plan on playing it with a friend. But I will not make a version for 1.17-1.18, or above
1.19, as I don't want to put effort into something I am not going to enjoy whatsoever.

## Old iron farms compatibility list

Currently, the results of trying to rebuild old iron farms seem to be inconsistent. Sometimes full resets
will work just fine, other times it will break halfway through. Here is a list of iron farms fully or
partially tested:
 - [TDL's Project Fe 5](https://youtu.be/kHe6AS23AHw): It sometimes breaks (adds doors incorrectly to previously setup villages) halfway through. It correctly built more than 512 villages (around 680), and the way it sometimes breaks is inconsistent. So **it _should_ work fine**. Maybe it breaks because of an unfortunately timed lag spike, or because over time certain chunks unload momentarily and their villages stop detecting the doors. In any case, it seems to be something out of the reach of this mod. In a powerful enough computer and making sure all the chunks containing the farm are properly loaded, it should be possible to reset it completely (up to the full 2049 villages). However, debugging the problem is quite a hard, time-consuming task, and I don't have the time for that. 
 - [TangoTek's Iron Trench](https://youtu.be/YFaCNsuD01k): Phase 1 works fine. Phase 2 incorrectly adds upper doors to previously setup villages. This is probably because the uppermost villager responsible for detecting the upper village doors performs this detection before the lowermost villager does its (and thus the correct village is out of range for the upper doors). Whether this is a problem with the mod, a change performed to vanilla Minecraft after 1.7, or just a problem with the world download is yet unverified. 
 - [DanielKotes 128-villages Lava Trench](https://youtu.be/j2WjrFoclKI): Phase 1 works fine. Phase 2 has not been tested yet.
 - [Bolu's Iron Farm (based on EDDxample's)](https://youtu.be/UtMBVGGpqZc): Three full resets have finished correctly.

Some other farms have been confirmed to not work unmodified:
 - [DanielKotes 128-villages Iron Trench](https://youtu.be/rX8JHL_Nu78) doesn't work because of changes in the way redstone components behave.

**Any help debugging the mod or verifying which old iron farms do or do not is very appreciated.** 

# Credits

The code for the Village Marker (anything OpenGL-render related) is based on
[EDDxample](https://github.com/EDDxample) 's [1.12 litemod ToyBox](https://github.com/EDDxample/MC-ToyBox-litemod)
and [Irtimaled](https://github.com/irtimaled) 's
[Bounding Box Outline Reloaded](https://github.com/irtimaled/BoundingBoxOutlineReloaded). Special thanks to them.
