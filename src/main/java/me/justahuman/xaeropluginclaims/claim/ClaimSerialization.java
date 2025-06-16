package me.justahuman.xaeropluginclaims.claim;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.justahuman.xaeropluginclaims.XaeroPluginClaims;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class ClaimSerialization {
    public static void serializeClaims(String worldId, String dimId, Map<Long, Map<ChunkPos, Claim>> claims) {
        Path savePath = XaeroPluginClaims.saveFolder.resolve(worldId).resolve(dimId);
        for (long region : claims.keySet()) {
            Map<ChunkPos, Claim> chunkClaims = claims.get(region);
            if (chunkClaims != null && !chunkClaims.isEmpty()) {
                int[] coords = XaeroPluginClaims.unpack(region);
                Path regionPath = savePath.resolve(coords[0] + "," + coords[1] + ".json");
                ByteArrayDataOutput output = ByteStreams.newDataOutput();
                output.writeInt(chunkClaims.size());
                for (Claim claim : chunkClaims.values()) {
                    claim.serialize(output);
                }

                try {
                    Files.write(regionPath, output.toByteArray());
                } catch (IOException e) {
                    XaeroPluginClaims.LOGGER.error("Failed to serialize claims for region {} in world {}: {}", region, worldId, e);
                }
            }
        }
    }

    public static void deserializeClaims(String worldId, String dimId) {
        Path savePath = XaeroPluginClaims.saveFolder.resolve(worldId).resolve(dimId);
        if (Files.exists(savePath)) {
            try(Stream<Path> regions = Files.list(savePath)) {
                regions.forEach(file -> {
                    if (file.toString().endsWith(".json")) {
                        try {
                            byte[] data = Files.readAllBytes(file);
                            ByteArrayDataInput input = ByteStreams.newDataInput(data);
                            int size = input.readInt();
                            for (int i = 0; i < size; i++) {
                                Claim claim = Claim.deserialize(input);
                                ClaimManager.addClaim(claim);
                            }
                        } catch (IOException e) {
                            XaeroPluginClaims.LOGGER.error("Failed to deserialize claims from file {}: {}", file, e);
                        }
                    }
                });
            } catch (IOException e) {
                XaeroPluginClaims.LOGGER.error("Failed to list claims directory {}: {}", savePath, e);
            }
        }
    }
}
