package me.aurelium.worldheightbooster.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.piece.StructurePieceSerializationContext;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.chunk.palette.PalettedContainer;
import net.minecraft.world.gen.BelowZeroRetrogen;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.chunk.BlendingData;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Final
	private static Codec<PalettedContainer<BlockState>> CODEC;

	@Shadow
	private static Codec<PalettedContainer<Holder<Biome>>> method_39036(Registry<Biome> registry) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	public static NbtList toNbt(ShortList[] lists) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	private static NbtCompound writeStructures(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<ConfiguredStructureFeature<?, ?>, StructureStart> map, Map<ConfiguredStructureFeature<?, ?>, LongSet> map2) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	private static void method_39311(ServerWorld serverWorld, NbtCompound nbtCompound, Chunk.TicksToSave ticksToSave) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	protected static void method_39035(ChunkPos chunkPos, int i, String string) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	public static ChunkStatus.ChunkType getChunkType(@Nullable NbtCompound nbt) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	@Nullable
	protected static WorldChunk.PostLoadProcessor loadEntities(ServerWorld world, NbtCompound nbt) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	protected static Map<ConfiguredStructureFeature<?, ?>, StructureStart> readStructureStarts(StructurePieceSerializationContext structurePieceSerializationContext, NbtCompound nbt, long worldSeed) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	@Shadow
	protected static Map<ConfiguredStructureFeature<?, ?>, LongSet> readStructureReferences(DynamicRegistryManager dynamicRegistryManager, ChunkPos pos, NbtCompound nbt) {
		throw new RuntimeException("Uh... what? Mixin is supposed to annihilate this method body.");
	}

	/**
	 * @author Aurelium
	 * @reason Y is stored as a byte, limiting world height.
	 * I could probably do some complicated mixin wizardry. But who actually mixes in to this method?
	 */
	@Overwrite
	public static NbtCompound serialize(ServerWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putInt("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
		nbtCompound.putInt("xPos", chunkPos.x);
		nbtCompound.putInt("yPos", chunk.getBottomSectionCoord());
		nbtCompound.putInt("zPos", chunkPos.z);
		nbtCompound.putLong("LastUpdate", world.getTime());
		nbtCompound.putLong("InhabitedTime", chunk.getInhabitedTime());
		nbtCompound.putString("Status", chunk.getStatus().getId());
		BlendingData blendingData = chunk.getBlendingData();
		if (blendingData != null) {
			BlendingData.CODEC
					.encodeStart(NbtOps.INSTANCE, blendingData)
					.resultOrPartial(LOGGER::error)
					.ifPresent(nbtElement -> nbtCompound.put("blending_data", nbtElement));
		}

		BelowZeroRetrogen belowZeroRetrogen = chunk.getBelowZeroRetrogen();
		if (belowZeroRetrogen != null) {
			BelowZeroRetrogen.CODEC
					.encodeStart(NbtOps.INSTANCE, belowZeroRetrogen)
					.resultOrPartial(LOGGER::error)
					.ifPresent(nbtElement -> nbtCompound.put("below_zero_retrogen", nbtElement));
		}

		UpgradeData upgradeData = chunk.getUpgradeData();
		if (!upgradeData.isDone()) {
			nbtCompound.put("UpgradeData", upgradeData.toNbt());
		}

		ChunkSection[] chunkSections = chunk.getSectionArray();
		NbtList nbtList = new NbtList();
		LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
		Registry<Biome> registry = world.getRegistryManager().get(Registry.BIOME_KEY);
		Codec<PalettedContainer<Holder<Biome>>> codec = method_39036(registry);
		boolean bl = chunk.isLightCorrect();

		for(int i = lightingProvider.getBottomY(); i < lightingProvider.getTopY(); ++i) {
			int j = chunk.sectionCoordToIndex(i);
			boolean bl2 = j >= 0 && j < chunkSections.length;
			ChunkNibbleArray chunkNibbleArray = lightingProvider.get(LightType.BLOCK).getLightSection(ChunkSectionPos.from(chunkPos, i));
			ChunkNibbleArray chunkNibbleArray2 = lightingProvider.get(LightType.SKY).getLightSection(ChunkSectionPos.from(chunkPos, i));
			if (bl2 || chunkNibbleArray != null || chunkNibbleArray2 != null) {
				NbtCompound nbtCompound2 = new NbtCompound();
				if (bl2) {
					ChunkSection chunkSection = chunkSections[j];
					nbtCompound2.put("block_states", CODEC.encodeStart(NbtOps.INSTANCE, chunkSection.getContainer()).getOrThrow(false, LOGGER::error));
					nbtCompound2.put("biomes", codec.encodeStart(NbtOps.INSTANCE, chunkSection.getBiomeContainer()).getOrThrow(false, LOGGER::error));
				}

				if (chunkNibbleArray != null && !chunkNibbleArray.isUninitialized()) {
					nbtCompound2.putByteArray("BlockLight", chunkNibbleArray.asByteArray());
				}

				if (chunkNibbleArray2 != null && !chunkNibbleArray2.isUninitialized()) {
					nbtCompound2.putByteArray("SkyLight", chunkNibbleArray2.asByteArray());
				}

				// MODIFIED SECTION BEGINS

				if (!nbtCompound2.isEmpty()) {
					nbtCompound2.putInt("Y", i);
					nbtList.add(nbtCompound2);
				}

				// MODIFIED SECTION ENDS
			}
		}

		nbtCompound.put("sections", nbtList);
		if (bl) {
			nbtCompound.putBoolean("isLightOn", true);
		}

		NbtList nbtList2 = new NbtList();

		for(BlockPos blockPos : chunk.getBlockEntityPositions()) {
			NbtCompound nbtCompound3 = chunk.getPackedBlockEntityNbt(blockPos);
			if (nbtCompound3 != null) {
				nbtList2.add(nbtCompound3);
			}
		}

		nbtCompound.put("block_entities", nbtList2);
		if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
			ProtoChunk protoChunk = (ProtoChunk)chunk;
			NbtList nbtList3 = new NbtList();
			nbtList3.addAll(protoChunk.getEntities());
			nbtCompound.put("entities", nbtList3);
			nbtCompound.put("Lights", toNbt(protoChunk.getLightSourcesBySection()));
			NbtCompound nbtCompound3 = new NbtCompound();

			for(GenerationStep.Carver carver : GenerationStep.Carver.values()) {
				CarvingMask carvingMask = protoChunk.getCarvingMask(carver);
				if (carvingMask != null) {
					nbtCompound3.putLongArray(carver.toString(), carvingMask.getMask());
				}
			}

			nbtCompound.put("CarvingMasks", nbtCompound3);
		}

		method_39311(world, nbtCompound, chunk.getTicksForSerialization());
		nbtCompound.put("PostProcessing", toNbt(chunk.getPostProcessingLists()));
		NbtCompound nbtCompound4 = new NbtCompound();

		for(Map.Entry<Heightmap.Type, Heightmap> entry : chunk.getHeightmaps()) {
			if (chunk.getStatus().getHeightmapTypes().contains(entry.getKey())) {
				nbtCompound4.put(((Heightmap.Type)entry.getKey()).getName(), new NbtLongArray(((Heightmap)entry.getValue()).asLongArray()));
			}
		}

		nbtCompound.put("Heightmaps", nbtCompound4);
		nbtCompound.put(
				"structures",
				writeStructures(StructurePieceSerializationContext.fromServerWorld(world), chunkPos, chunk.getStructureStarts(), chunk.getStructureReferences())
		);
		return nbtCompound;
	}

	/**
	 * @author Y is stored as a byte, limiting world height.
	 * I could probably do some complicated mixin wizardry. But who actually mixes in to this method?
	 */
	@Overwrite
	public static ProtoChunk deserialize(ServerWorld world, PointOfInterestStorage poiStorage, ChunkPos pos, NbtCompound nbt) {
		ChunkPos chunkPos = new ChunkPos(nbt.getInt("xPos"), nbt.getInt("zPos"));
		if (!Objects.equals(pos, chunkPos)) {
			LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", pos, pos, chunkPos);
		}

		UpgradeData upgradeData = nbt.contains("UpgradeData", 10) ? new UpgradeData(nbt.getCompound("UpgradeData"), world) : UpgradeData.NO_UPGRADE_DATA;
		boolean bl = nbt.getBoolean("isLightOn");
		NbtList nbtList = nbt.getList("sections", 10);
		int i = world.countVerticalSections();
		ChunkSection[] chunkSections = new ChunkSection[i];
		boolean bl2 = world.getDimension().hasSkyLight();
		ChunkManager chunkManager = world.getChunkManager();
		LightingProvider lightingProvider = chunkManager.getLightingProvider();
		if (bl) {
			lightingProvider.setRetainData(pos, true);
		}

		Registry<Biome> registry = world.getRegistryManager().get(Registry.BIOME_KEY);
		Codec<PalettedContainer<Holder<Biome>>> codec = method_39036(registry);

		for(int j = 0; j < nbtList.size(); ++j) {
			NbtCompound nbtCompound = nbtList.getCompound(j);
			// MODIFIED SECTION BEGINS
			int k = nbtCompound.getInt("Y");
			// MODIFIED SECTION ENDS
			int l = world.sectionCoordToIndex(k);
			if (l >= 0 && l < chunkSections.length) {
				PalettedContainer<BlockState> palettedContainer;
				if (nbtCompound.contains("block_states", 10)) {
					palettedContainer = CODEC.parse(NbtOps.INSTANCE, nbtCompound.getCompound("block_states"))
							.promotePartial(string -> method_39035(pos, k, string))
							.getOrThrow(false, LOGGER::error);
				} else {
					palettedContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
				}

				PalettedContainer<Holder<Biome>> palettedContainer2;
				if (nbtCompound.contains("biomes", 10)) {
					palettedContainer2 = codec.parse(NbtOps.INSTANCE, nbtCompound.getCompound("biomes"))
							.promotePartial(string -> method_39035(pos, k, string))
							.getOrThrow(false, LOGGER::error);
				} else {
					palettedContainer2 = new PalettedContainer<>(
							registry.asHolderIdMap(), registry.getHolderOrThrow(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME
					);
				}

				ChunkSection chunkSection = new ChunkSection(k, palettedContainer, palettedContainer2);
				chunkSections[l] = chunkSection;
				poiStorage.initForPalette(pos, chunkSection);
			}

			if (bl) {
				if (nbtCompound.contains("BlockLight", 7)) {
					lightingProvider.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(nbtCompound.getByteArray("BlockLight")), true);
				}

				if (bl2 && nbtCompound.contains("SkyLight", 7)) {
					lightingProvider.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, k), new ChunkNibbleArray(nbtCompound.getByteArray("SkyLight")), true);
				}
			}
		}

		long m = nbt.getLong("InhabitedTime");
		ChunkStatus.ChunkType chunkType = getChunkType(nbt);
		BlendingData blendingData;
		if (nbt.contains("blending_data", 10)) {
			blendingData = (BlendingData)BlendingData.CODEC
					.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("blending_data")))
					.resultOrPartial(LOGGER::error)
					.orElse(null);
		} else {
			blendingData = null;
		}

		Chunk chunk;
		if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
			ChunkTickScheduler<Block> chunkTickScheduler = ChunkTickScheduler.create(
					nbt.getList("block_ticks", 10), string -> Registry.BLOCK.getOrEmpty(Identifier.tryParse(string)), pos
			);
			ChunkTickScheduler<Fluid> chunkTickScheduler2 = ChunkTickScheduler.create(
					nbt.getList("fluid_ticks", 10), string -> Registry.FLUID.getOrEmpty(Identifier.tryParse(string)), pos
			);
			chunk = new WorldChunk(
					world.toServerWorld(), pos, upgradeData, chunkTickScheduler, chunkTickScheduler2, m, chunkSections, loadEntities(world, nbt), blendingData
			);
		} else {
			ProtoChunkTicks<Block> protoChunkTicks = ProtoChunkTicks.load(
					nbt.getList("block_ticks", 10), string -> Registry.BLOCK.getOrEmpty(Identifier.tryParse(string)), pos
			);
			ProtoChunkTicks<Fluid> protoChunkTicks2 = ProtoChunkTicks.load(
					nbt.getList("fluid_ticks", 10), string -> Registry.FLUID.getOrEmpty(Identifier.tryParse(string)), pos
			);
			ProtoChunk protoChunk = new ProtoChunk(pos, upgradeData, chunkSections, protoChunkTicks, protoChunkTicks2, world, registry, blendingData);
			chunk = protoChunk;
			protoChunk.setInhabitedTime(m);
			if (nbt.contains("below_zero_retrogen", 10)) {
				BelowZeroRetrogen.CODEC
						.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("below_zero_retrogen")))
						.resultOrPartial(LOGGER::error)
						.ifPresent(protoChunk::setBelowZeroRetrogen);
			}

			ChunkStatus chunkStatus = ChunkStatus.byId(nbt.getString("Status"));
			protoChunk.setStatus(chunkStatus);
			if (chunkStatus.isAtLeast(ChunkStatus.FEATURES)) {
				protoChunk.setLightingProvider(lightingProvider);
			}

			BelowZeroRetrogen belowZeroRetrogen = protoChunk.getBelowZeroRetrogen();
			boolean bl3 = chunkStatus.isAtLeast(ChunkStatus.LIGHT) || belowZeroRetrogen != null && belowZeroRetrogen.status().isAtLeast(ChunkStatus.LIGHT);
			if (!bl && bl3) {
				for(BlockPos blockPos : BlockPos.iterate(pos.getStartX(), world.getBottomY(), pos.getStartZ(), pos.getEndX(), world.getTopY() - 1, pos.getEndZ())) {
					if (chunk.getBlockState(blockPos).getLuminance() != 0) {
						protoChunk.addLightSource(blockPos);
					}
				}
			}
		}

		chunk.setLightCorrect(bl);
		NbtCompound nbtCompound2 = nbt.getCompound("Heightmaps");
		EnumSet<Heightmap.Type> enumSet = EnumSet.noneOf(Heightmap.Type.class);

		for(Heightmap.Type type : chunk.getStatus().getHeightmapTypes()) {
			String string = type.getName();
			if (nbtCompound2.contains(string, 12)) {
				chunk.setHeightmap(type, nbtCompound2.getLongArray(string));
			} else {
				enumSet.add(type);
			}
		}

		Heightmap.populateHeightmaps(chunk, enumSet);
		NbtCompound nbtCompound3 = nbt.getCompound("structures");
		chunk.setStructureStarts(readStructureStarts(StructurePieceSerializationContext.fromServerWorld(world), nbtCompound3, world.getSeed()));
		chunk.setStructureReferences(readStructureReferences(world.getRegistryManager(), pos, nbtCompound3));
		if (nbt.getBoolean("shouldSave")) {
			chunk.setNeedsSaving(true);
		}

		NbtList nbtList2 = nbt.getList("PostProcessing", 9);

		for(int n = 0; n < nbtList2.size(); ++n) {
			NbtList nbtList3 = nbtList2.getList(n);

			for(int o = 0; o < nbtList3.size(); ++o) {
				chunk.markBlockForPostProcessing(nbtList3.getShort(o), n);
			}
		}

		if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
			return new ReadOnlyChunk((WorldChunk)chunk, false);
		} else {
			ProtoChunk protoChunk2 = (ProtoChunk)chunk;
			NbtList nbtList3 = nbt.getList("entities", 10);

			for(int o = 0; o < nbtList3.size(); ++o) {
				protoChunk2.addEntity(nbtList3.getCompound(o));
			}

			NbtList nbtList4 = nbt.getList("block_entities", 10);

			for(int p = 0; p < nbtList4.size(); ++p) {
				NbtCompound nbtCompound4 = nbtList4.getCompound(p);
				chunk.setBlockEntityNbt(nbtCompound4);
			}

			NbtList nbtList5 = nbt.getList("Lights", 9);

			for(int q = 0; q < nbtList5.size(); ++q) {
				NbtList nbtList6 = nbtList5.getList(q);

				for(int r = 0; r < nbtList6.size(); ++r) {
					protoChunk2.addLightSource(nbtList6.getShort(r), q);
				}
			}

			NbtCompound nbtCompound4 = nbt.getCompound("CarvingMasks");

			for(String string2 : nbtCompound4.getKeys()) {
				GenerationStep.Carver carver = GenerationStep.Carver.valueOf(string2);
				protoChunk2.setCarvingMask(carver, new CarvingMask(nbtCompound4.getLongArray(string2), chunk.getBottomY()));
			}

			return protoChunk2;
		}
	}
}
