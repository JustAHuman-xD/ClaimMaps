package me.justahuman.claimmaps.api.claim;

import me.justahuman.claimmaps.implementation.claim.ClaimManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface ClaimHighlighter {
    boolean displayClaims();
    int borderOpacity();
    int fillOpacity();
    int[] resultStore();

    default boolean hasHighlights(RegistryKey<World> worldKey, int regionX, int regionZ) {
        return ClaimManager.hasClaimRegion(worldKey, regionX, regionZ);
    }

    default boolean chunkHighlighted(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return ClaimManager.hasClaim(worldKey, new ChunkPos(chunkX, chunkZ));
    }

    default int[] highlightColor(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        if (!displayClaims()) {
            return null;
        }

        Claim claim = ClaimManager.getClaim(worldKey, chunkX, chunkZ);
        if (claim == null) {
            return null;
        }

        Claim topClaim = ClaimManager.getClaim(worldKey, chunkX, chunkZ - 1);
        Claim rightClaim = ClaimManager.getClaim(worldKey, chunkX + 1, chunkZ);
        Claim bottomClaim = ClaimManager.getClaim(worldKey, chunkX, chunkZ + 1);
        Claim leftClaim = ClaimManager.getClaim(worldKey, chunkX - 1, chunkZ);
        int claimColor = claim.color();
        int claimColorFormatted = (claimColor & 255) << 24 | (claimColor >> 8 & 255) << 16 | (claimColor >> 16 & 255) << 8;
        int centerColor = claimColorFormatted | 255 * fillOpacity() / 100;
        int sideColor = claimColorFormatted | 255 * borderOpacity() / 100;
        this.resultStore()[0] = centerColor;
        this.resultStore()[1] = topClaim != claim ? sideColor : centerColor;
        this.resultStore()[2] = rightClaim != claim ? sideColor : centerColor;
        this.resultStore()[3] = bottomClaim != claim ? sideColor : centerColor;
        this.resultStore()[4] = leftClaim != claim ? sideColor : centerColor;
        return this.resultStore();
    }
}
