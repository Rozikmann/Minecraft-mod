package com.example.indwave.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlotChunkGenerator extends ChunkGenerator {

    public static final MapCodec<PlotChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
            ).apply(instance, PlotChunkGenerator::new)
    );

    public PlotChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;

        // Сетка: шаг 352, размер 256
        int gridStep = 512;
        int plotSize = 256;

        int gridX = Math.floorDiv(startX, gridStep);
        int gridZ = Math.floorDiv(startZ, gridStep);

        int plotStartX = gridX * gridStep;
        int plotStartZ = gridZ * gridStep;

        boolean inPlot = (startX + 15 >= plotStartX && startX < plotStartX + plotSize)
                && (startZ + 15 >= plotStartZ && startZ < plotStartZ + plotSize);

        if (inPlot) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            RandomSource random = RandomSource.create(chunk.getPos().toLong() ^ 12345L);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = startX + x;
                    int worldZ = startZ + z;

                    if (worldX >= plotStartX && worldX < plotStartX + plotSize
                            && worldZ >= plotStartZ && worldZ < plotStartZ + plotSize) {

                        for (int y = -64; y <= 62; y++) {
                            pos.set(x, y, z);
                            if (y <= -62) {
                                chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                            } else if (y < 63 && random.nextFloat() < 0.02f) {
                                chunk.setBlockState(pos, Blocks.IRON_ORE.defaultBlockState(), false);
                            } else {
                                chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), false);
                            }
                        }
                        pos.set(x, 63, z);
                        chunk.setBlockState(pos, Blocks.DIRT.defaultBlockState(), false);
                        pos.set(x, 64, z);
                        chunk.setBlockState(pos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {}

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomeManager, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) { return 64; }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(-64, new BlockState[]{ Blocks.STONE.defaultBlockState() });
    }

    @Override
    public int getGenDepth() { return 384; }
    @Override
    public int getSeaLevel() { return 63; }
    @Override
    public int getMinY() { return -64; }
    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {}
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}
}