package me.justahuman.claimmaps.claim;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface ClaimSource {
    boolean hasClaimWorld(RegistryKey<World> worldKey);
    boolean hasClaimRegion(RegistryKey<World> worldKey, int regionX, int regionZ);
    default boolean hasClaim(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return hasClaim(worldKey, new ChunkPos(chunkX, chunkZ));
    }
    default boolean hasClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        return getClaim(worldKey, chunk) != null;
    }

    default Claim getClaim(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return getClaim(worldKey, new ChunkPos(chunkX, chunkZ));
    }
    Claim getClaim(RegistryKey<World> worldKey, ChunkPos chunk);
}
