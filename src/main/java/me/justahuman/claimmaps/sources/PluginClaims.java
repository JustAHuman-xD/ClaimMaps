package me.justahuman.claimmaps.sources;

import me.justahuman.claimmaps.ClaimMaps;
import me.justahuman.claimmaps.claim.Claim;
import me.justahuman.claimmaps.claim.ClaimManager;
import me.justahuman.claimmaps.claim.ClaimSerialization;
import me.justahuman.claimmaps.claim.ClaimSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PluginClaims implements ClaimSource {
    private static final Map<RegistryKey<World>, Map<Long, Claim>> CLAIM_BY_ID = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Map<ChunkPos, Claim>>> CLAIM_BY_CHUNK = new HashMap<>();
    private static final Map<RegistryKey<World>, Set<Long>> DELETED_CLAIMS = new HashMap<>();

    public static final PluginClaims INSTANCE = new PluginClaims();

    private PluginClaims() {
        ClaimManager.registerClaimSource(this);
        ClaimManager.onWorldChanged((oldWorldId, world) -> {
            if (world == null) {
                if (oldWorldId != null) {
                    ClaimSerialization.serializeClaims(oldWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
                }
                CLAIM_BY_ID.clear();
                CLAIM_BY_CHUNK.clear();
                DELETED_CLAIMS.clear();
                return;
            }

            String worldId = ClaimMaps.getWorldId(world);
            if (!worldId.equals(oldWorldId)) {
                if (oldWorldId != null) {
                    ClaimSerialization.serializeClaims(oldWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
                }
                CLAIM_BY_ID.clear();
                CLAIM_BY_CHUNK.clear();
                DELETED_CLAIMS.clear();
                ClaimSerialization.deserializeClaims(worldId);
            }
        });
    }

    @Override
    public boolean hasClaimWorld(RegistryKey<World> worldKey) {
        Map<Long, Claim> claimsById = CLAIM_BY_ID.get(worldKey);
        return claimsById != null && !claimsById.isEmpty();
    }

    @Override
    public boolean hasClaimRegion(RegistryKey<World> worldKey, int regionX, int regionZ) {
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
        if (worldClaims == null) {
            return false;
        }
        Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(ClaimMaps.pack(regionX, regionZ));
        return claimsInRegion != null && !claimsInRegion.isEmpty();
    }

    @Override
    public Claim getClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
        if (worldClaims != null) {
            Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(ClaimMaps.pack(chunk.getRegionX(), chunk.getRegionZ()));
            return claimsInRegion != null ? claimsInRegion.get(chunk) : null;
        }
        return null;
    }

    public void addClaim(Claim claim) {
        deleteClaim(claim.worldKey(), claim.id());
        CLAIM_BY_ID.computeIfAbsent(claim.worldKey(), k -> new HashMap<>()).put(claim.id(), claim);
        DELETED_CLAIMS.computeIfAbsent(claim.worldKey(), k -> new HashSet<>()).remove(claim.id());
        for (ChunkPos chunk : claim.chunks()) {
            long region = ClaimMaps.pack(chunk.getRegionX(), chunk.getRegionZ());
            CLAIM_BY_CHUNK.computeIfAbsent(claim.worldKey(), k -> new HashMap<>())
                    .computeIfAbsent(region, k -> new HashMap<>())
                    .put(chunk, claim);
        }
        ClaimManager.onClaimAdded(claim);
    }

    public void removeClaims(RegistryKey<World> worldKey, ChunkPos chunkPos) {
        Claim claim = ClaimManager.getClaim(worldKey, chunkPos);
        if (claim == null) {
            return;
        }
        claim.chunks().remove(chunkPos);
        deleteClaim(worldKey, claim.id());
        if (!claim.chunks().isEmpty()) {
            addClaim(claim);
        }
    }

    public void deleteClaim(RegistryKey<World> worldKey, long id) {
        DELETED_CLAIMS.computeIfAbsent(worldKey, k -> new HashSet<>()).add(id);
        Map<Long, Claim> claimsById = CLAIM_BY_ID.get(worldKey);
        if (claimsById == null) {
            return;
        }
        Claim claim = claimsById.remove(id);
        if (claim == null) {
            return;
        }
        for (ChunkPos chunk : claim.chunks()) {
            long region = ClaimMaps.pack(chunk.getRegionX(), chunk.getRegionZ());
            Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
            if (worldClaims != null) {
                Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(region);
                if (claimsInRegion != null) {
                    claimsInRegion.remove(chunk);
                }
            }
        }
        ClaimManager.onClaimRemoved(claim);
    }
}
