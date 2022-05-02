# WorldHeightBooster 2

<img src="https://github.com/AutumnAurelium/WorldHeightBooster2/blob/main/showcase.png?raw=true" width=400 alt="A screenshot of Minecraft with blocks placed at a very high Y coordinate"><br />

<img src="https://github.com/AutumnAurelium/WorldHeightBooster2/blob/main/quilt_supported.png?raw=true" width=200 alt="Supported on Quilt Loader">

WorldHeightBooster 2 is a rewrite of the [original mod](https://github.com/AutumnAurelium/WorldHeightBooster), made for one of the first 1.17 snapshots alongside internal world height changes. It removes limits on the default world height achievable via datapacks.

Since 1.17, a lot has changed and while the world height expansion is fully into effect, internal limitations do not allow world heights larger than a total of 4094 blocks (-2047 to 2047). This mod contains a collection of mixins required to allow larger worlds. **This mod is currently not ready for production use, and should be seen as a technical demonstration before bug fixes and optimization can be implemented. This mod is *not* Cubic Chunks, and does not allow for infinitely tall worlds. World generation above or below the normal values must be implemented through datapacks.**


### Known Issues

- Long save times on very tall worlds.
- Incompatibility with worlds not created with the mod installed.
- Frame drops when falling significant distances.
- Missing chunks beyond X or Z values of roughly 15 million.
