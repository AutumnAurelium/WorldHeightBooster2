# WorldHeightBooster 2

![A screenshot showing a Minecraft world with blocks built at abnormally high Y coordinates.](https://github.com/AutumnAurelium/WorldHeightBooster2/raw/main/showcase.png)

![Supported on Quilt Loader](https://github.com/AutumnAurelium/WorldHeightBooster2/blob/main/quilt_supported.png?raw=true)

WorldHeightBooster 2 is a rewrite of the [original mod](https://github.com/AutumnAurelium/WorldHeightBooster), originally made for one of the first 1.17 snapshots alongside internal world height changes.

Since 1.17, a lot has changed and while the world height expansion is fully into effect, internal limitations do not allow world heights larger than a total of 4094 blocks (-2047 to 2047). This mod contains a collection of mixins required to allow larger worlds. **This mod is currently not ready for production use, and should be seen as a technical demonstration before bug fixes and optimization can be implemented. This mod is *not* Cubic Chunks, and does not allow for infinitely tall worlds. Worldgen above or below the normal values must be implemented with datapacks.**

### Known Issues

- Long save times on very tall worlds.
- Incompatibility with worlds not created with the mod installed.
- Frame drops when falling significant distances.
