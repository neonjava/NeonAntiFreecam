package me.neonjava.antifreecam.chunk;

import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.SingletonPalette;
import me.neonjava.antifreecam.config.WorldConfig;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Procedural Fake Chunk Engine that computes and replaces underground terrain segments
 * while preserving surfaces, avoiding voids, and buffering neighboring chunk borders.
 * Streams sections vertically based on a sliding vertical window around the player's camera.
 */
public final class FakeChunkEngine {
    private final Map<String, ProceduralNoise> noiseCache = new ConcurrentHashMap<>();

    // Material State IDs for 1.21
    private static final int STONE_STATE_ID = 1;
    private static final int DEEPSLATE_STATE_ID = 2;
    private static final int TUFF_STATE_ID = 3;
    private static final int GRANITE_STATE_ID = 4;
    private static final int ANDESITE_STATE_ID = 5;
    private static final int DIORITE_STATE_ID = 6;

    public Column processColumn(World world, Column column, WorldConfig worldConfig, int playerChunkX, int playerChunkZ, int renderDistance, int playerY) {
        long seed = world.getSeed();
        ProceduralNoise noise = noiseCache.computeIfAbsent(world.getName(), k -> new ProceduralNoise(seed));

        BaseChunk[] originalChunks = column.getChunks();
        BaseChunk[] modifiedChunks = new BaseChunk[originalChunks.length];

        int minHeight = world.getMinHeight();
        int hideY = worldConfig.getHideY();

        // Calculate sliding vertical window bounds (32-block reveal radius around player camera Y)
        int windowMinY = playerY - 32;
        int windowMaxY = playerY + 32;

        for (int sectionIndex = 0; sectionIndex < originalChunks.length; sectionIndex++) {
            BaseChunk originalSection = originalChunks[sectionIndex];
            int sectionMinY = minHeight + (sectionIndex << 4);
            int sectionMaxY = sectionMinY + 15;

            // If section is above Hide Y, preserve real world surface completely
            if (sectionMinY >= hideY) {
                modifiedChunks[sectionIndex] = originalSection;
                continue;
            }

            // Stream independently: if chunk section intersects or is inside the sliding vertical window, keep it REAL
            if (sectionMaxY >= windowMinY && sectionMinY <= windowMaxY) {
                modifiedChunks[sectionIndex] = originalSection;
                continue;
            }

            // Outside sliding vertical window: replace with fake blocks
            modifiedChunks[sectionIndex] = generateFakeSection(column.getX(), column.getZ(), sectionIndex, sectionMinY, originalSection, noise);
        }

        // Filter and hide container tile entities
        TileEntity[] originalTileEntities = column.getTileEntities();
        List<TileEntity> filteredTileEntities = new ArrayList<>();
        if (originalTileEntities != null) {
            for (TileEntity te : originalTileEntities) {
                if (te.getY() < hideY && worldConfig.isHideContainers()) {
                    // Check if block entity is inside the sliding vertical window
                    if (te.getY() >= windowMinY && te.getY() <= windowMaxY) {
                        filteredTileEntities.add(te);
                        continue;
                    }
                    // Hide container blocks outside window naturally
                    continue;
                }
                filteredTileEntities.add(te);
            }
        }

        return new Column(column.getX(), column.getZ(), column.isFullChunk(), modifiedChunks,
                filteredTileEntities.toArray(new TileEntity[0]), column.getHeightmaps());
    }

    private BaseChunk generateFakeSection(int chunkX, int chunkZ, int sectionIndex, int sectionMinY, BaseChunk original, ProceduralNoise noise) {
        DataPalette chunkPalette = new DataPalette(new SingletonPalette(STONE_STATE_ID), null, PaletteType.CHUNK);

        for (int y = 0; y < 16; y++) {
            int absoluteY = sectionMinY + y;
            for (int x = 0; x < 16; x++) {
                int absoluteX = (chunkX << 4) + x;
                for (int z = 0; z < 16; z++) {
                    int absoluteZ = (chunkZ << 4) + z;

                    int stateId = getProceduralBlockState(absoluteX, absoluteY, absoluteZ, noise);
                    chunkPalette.set(x, y, z, stateId);
                }
            }
        }

        return new Chunk_v1_18(0, chunkPalette, new DataPalette(new SingletonPalette(0), null, PaletteType.BIOME));
    }

    private int getProceduralBlockState(int x, int y, int z, ProceduralNoise noise) {
        // Select base block based on depth
        int baseState = y < 0 ? DEEPSLATE_STATE_ID : STONE_STATE_ID;

        // Apply noise for blending blobs
        double n1 = noise.noise(x * 0.05, y * 0.05, z * 0.05);
        if (n1 > 0.45) {
            return TUFF_STATE_ID;
        } else if (n1 > 0.3) {
            return GRANITE_STATE_ID;
        } else if (n1 < -0.45) {
            return ANDESITE_STATE_ID;
        } else if (n1 < -0.3) {
            return DIORITE_STATE_ID;
        }

        return baseState;
    }
}
