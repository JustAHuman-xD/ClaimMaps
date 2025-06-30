package me.justahuman.pluginclaims.claim;

import me.justahuman.pluginclaims.PluginClaims;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ClaimSerialization {
    public static void serializeClaims(String worldId, Map<RegistryKey<World>, Map<Long, Claim>> claims, Map<RegistryKey<World>, Set<Long>> deletedClaims) {
        Path worldPath = PluginClaims.saveFolder.resolve(worldId);
        if (!Files.exists(worldPath)) {
            try {
                Files.createDirectories(worldPath);
            } catch (IOException e) {
                PluginClaims.LOGGER.error("Failed to create directory for world '{}': {}", worldId, e);
                return;
            }
        }

        for (Map.Entry<RegistryKey<World>, Map<Long, Claim>> dimEntry : claims.entrySet()) {
            RegistryKey<World> dimKey = dimEntry.getKey();
            String dimId = PluginClaims.getLegacyKey(dimKey);
            Path savePath = worldPath.resolve(dimId);
            try {
                if (!Files.exists(savePath)) {
                    Files.createDirectories(savePath);
                }
            } catch (IOException e) {
                PluginClaims.LOGGER.error("Failed to create directory for dimension '{}': {}", dimId, e);
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
                    PluginClaims.LOGGER.error("Failed to serialize claim {}, {} : {}", worldId, dimId, claim, e);
                }
            }
        }

        for (Map.Entry<RegistryKey<World>, Set<Long>> dimEntry : deletedClaims.entrySet()) {
            RegistryKey<World> dimKey = dimEntry.getKey();
            Path savedPath = worldPath.resolve(dimKey.getValue().toString());
            if (!Files.exists(savedPath)) {
                continue;
            }

            for (Long id : dimEntry.getValue()) {
                try {
                    Files.deleteIfExists(savedPath.resolve(id + ".claim"));
                } catch (IOException e) {
                    PluginClaims.LOGGER.error("Failed to delete claim {}, {} : {}", worldId, dimEntry.getValue(), id, e);
                }
            }
        }
    }

    public static void deserializeClaims(String worldId) {
        Path savePath = PluginClaims.saveFolder.resolve(worldId);
        if (Files.exists(savePath)) {
            try(Stream<Path> dimensions = Files.list(savePath)) {
                dimensions.forEach(file -> deserializeClaims(worldId, file.getFileName().toString()));
            } catch (IOException e) {
                PluginClaims.LOGGER.error("Failed to list dimensions directory {}: {}", savePath, e);
            }
        }
    }

    public static void deserializeClaims(String worldId, String dimId) {
        Path savePath = PluginClaims.saveFolder.resolve(worldId).resolve(dimId);
        if (!Files.exists(savePath)) {
            return;
        }

        try(Stream<Path> regions = Files.list(savePath)) {
            regions.forEach(file -> {
                if (!file.toString().endsWith(".claim")) {
                    PluginClaims.LOGGER.warn("Skipping non-claim file: {}", file.getFileName());
                    return;
                }

                try {
                    ClaimManager.addClaim(Claim.deserialize(Files.readAllBytes(file)));
                } catch (IOException e) {
                    PluginClaims.LOGGER.error("Failed to deserialize claim from file {}: {}", file, e);
                }
            });
        } catch (IOException e) {
            PluginClaims.LOGGER.error("Failed to list claims directory {}: {}", savePath, e);
        }
    }
}
