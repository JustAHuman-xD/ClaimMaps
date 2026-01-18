package me.justahuman.claimmaps.implementation.sources;

import me.justahuman.claimmaps.ClaimMaps;
import me.justahuman.claimmaps.api.claim.Claim;
import me.justahuman.claimmaps.implementation.claim.ClaimManager;
import me.justahuman.claimmaps.api.claim.ClaimSource;
import me.justahuman.claimmaps.util.ClaimMapUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PluginClaims implements ClaimSource {
    private static final Map<RegistryKey<World>, Map<Long, Claim>> CLAIM_BY_ID = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Map<ChunkPos, Claim>>> CLAIM_BY_CHUNK = new HashMap<>();
    private static final Map<RegistryKey<World>, Set<Long>> DELETED_CLAIMS = new HashMap<>();

    private static final Path SAVE_FOLDER;
    static {
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir().normalize();
            SAVE_FOLDER = gameDir.resolve(ClaimMaps.MOD_ID);
            if (!Files.exists(SAVE_FOLDER)) {
                ClaimMaps.LOGGER.info("Creating save folder for Plugin Claim Maps at {}", SAVE_FOLDER);
                Files.createDirectories(SAVE_FOLDER);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create save folder for Plugin Claim Maps", e);
        }
    }

    public static final PluginClaims INSTANCE = new PluginClaims();

    private PluginClaims() {
        ClaimManager.registerClaimSource(this);
        ClaimManager.onWorldChanged((oldWorldId, world) -> {
            if (world == null) {
                if (oldWorldId != null) {
                    serializeClaims(oldWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
                }
                CLAIM_BY_ID.clear();
                CLAIM_BY_CHUNK.clear();
                DELETED_CLAIMS.clear();
                return;
            }

            String worldId = ClaimMapUtils.getWorldId(world);
            if (!worldId.equals(oldWorldId)) {
                if (oldWorldId != null) {
                    serializeClaims(oldWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
                }
                CLAIM_BY_ID.clear();
                CLAIM_BY_CHUNK.clear();
                DELETED_CLAIMS.clear();
                deserializeClaims(worldId);
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
        Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(ClaimMapUtils.pack(regionX, regionZ));
        return claimsInRegion != null && !claimsInRegion.isEmpty();
    }

    @Override
    public Claim getClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
        if (worldClaims != null) {
            Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(ClaimMapUtils.pack(chunk.getRegionX(), chunk.getRegionZ()));
            return claimsInRegion != null ? claimsInRegion.get(chunk) : null;
        }
        return null;
    }

    public void addClaim(Claim claim) {
        deleteClaim(claim.worldKey(), claim.id());
        CLAIM_BY_ID.computeIfAbsent(claim.worldKey(), k -> new HashMap<>()).put(claim.id(), claim);
        DELETED_CLAIMS.computeIfAbsent(claim.worldKey(), k -> new HashSet<>()).remove(claim.id());
        for (ChunkPos chunk : claim.chunks()) {
            long region = ClaimMapUtils.pack(chunk.getRegionX(), chunk.getRegionZ());
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
            long region = ClaimMapUtils.pack(chunk.getRegionX(), chunk.getRegionZ());
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

    public void serializeClaims(String worldId, Map<RegistryKey<World>, Map<Long, Claim>> claims, Map<RegistryKey<World>, Set<Long>> deletedClaims) {
        Path worldPath = SAVE_FOLDER.resolve(worldId);
        if (!Files.exists(worldPath)) {
            try {
                Files.createDirectories(worldPath);
            } catch (IOException e) {
                ClaimMaps.LOGGER.error("Failed to create directory for world '{}': {}", worldId, e);
                return;
            }
        }

        for (Map.Entry<RegistryKey<World>, Map<Long, Claim>> dimEntry : claims.entrySet()) {
            RegistryKey<World> dimKey = dimEntry.getKey();
            Path savePath = worldPath.resolve(ClaimMapUtils.getDimensionId(dimKey));
            try {
                if (!Files.exists(savePath)) {
                    Files.createDirectories(savePath);
                }
            } catch (IOException e) {
                ClaimMaps.LOGGER.error("Failed to create directory for dimension '{}': {}", dimKey.getValue(), e);
                continue;
            }
            for (Map.Entry<Long, Claim> claimEntry : dimEntry.getValue().entrySet()) {
                long id = claimEntry.getKey();
                Claim claim = claimEntry.getValue();
                Path claimPath = savePath.resolve(id + ".claim");
                try {
                    if (!Files.exists(savePath)) {
                        Files.createDirectories(savePath);
                    }
                    Files.write(claimPath, claim.serialize());
                } catch (IOException e) {
                    ClaimMaps.LOGGER.error("Failed to serialize claim {}, {} : {}", worldId, dimKey.getValue(), claim, e);
                }
            }
        }

        for (Map.Entry<RegistryKey<World>, Set<Long>> dimEntry : deletedClaims.entrySet()) {
            Path savedPath = worldPath.resolve(ClaimMapUtils.getDimensionId(dimEntry.getKey()));
            if (!Files.exists(savedPath)) {
                continue;
            }

            for (Long id : dimEntry.getValue()) {
                try {
                    Files.deleteIfExists(savedPath.resolve(id + ".claim"));
                } catch (IOException e) {
                    ClaimMaps.LOGGER.error("Failed to delete claim {}, {} : {}", worldId, dimEntry.getValue(), id, e);
                }
            }
        }
    }

    public void deserializeClaims(String worldId) {
        Path savePath = SAVE_FOLDER.resolve(worldId);
        if (Files.exists(savePath)) {
            try(Stream<Path> dimensions = Files.list(savePath)) {
                dimensions.forEach(file -> deserializeClaims(worldId, file.getFileName().toString()));
            } catch (IOException e) {
                ClaimMaps.LOGGER.error("Failed to list dimensions directory {}: {}", savePath, e);
            }
        }
    }

    public void deserializeClaims(String worldId, String dimId) {
        Path savePath = SAVE_FOLDER.resolve(worldId).resolve(dimId);
        if (!Files.exists(savePath)) {
            return;
        }

        try(Stream<Path> regions = Files.list(savePath)) {
            regions.forEach(file -> {
                if (!file.toString().endsWith(".claim")) {
                    ClaimMaps.LOGGER.warn("Skipping non-claim file: {}", file.getFileName());
                    return;
                }

                try {
                    addClaim(Claim.deserialize(Files.readAllBytes(file)));
                } catch (IOException e) {
                    ClaimMaps.LOGGER.error("Failed to deserialize claim from file {}: {}", file, e);
                }
            });
        } catch (IOException e) {
            ClaimMaps.LOGGER.error("Failed to list claims directory {}: {}", savePath, e);
        }
    }
}
