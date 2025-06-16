package me.justahuman.xaeropluginclaims.claim;

import com.mojang.authlib.yggdrasil.ProfileResult;
import me.justahuman.xaeropluginclaims.XaeroPluginClaims;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ClaimManager {
    private static final Map<UUID, String> OWNER_CACHE = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Claim>> CLAIM_BY_ID = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Map<ChunkPos, Claim>>> CLAIMS = new HashMap<>();
    private static String currentWorldId = null;
    private static String currentDimensionId = null;
    private static RegistryKey<World> currentWorldKey = null;
    private static Consumer<Claim> onClaimAdded = claim -> {};
    private static Consumer<Claim> onClaimRemoved = claim -> {};
    private static Consumer<RegistryKey<World>> onWorldChanged = worldKey -> {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (worldKey == null || handler == null) {
            if (currentWorldId != null && currentDimensionId != null && currentWorldKey != null) {
                ClaimSerialization.serializeClaims(currentWorldId, currentDimensionId, CLAIMS.getOrDefault(currentWorldKey, new HashMap<>()));
            }
            currentWorldId = null;
            currentDimensionId = null;
            currentWorldKey = null;
            CLAIMS.clear();
            return;
        }

        String oldWorldId = currentWorldId;
        String oldDimensionId = currentDimensionId;
        RegistryKey<World> oldWorldKey = currentWorldKey;
        currentWorldId = XaeroPluginClaims.getWorldId(handler);
        currentDimensionId = XaeroPluginClaims.getDimensionId(worldKey);
        currentWorldKey = worldKey;
        if (!currentWorldId.equals(oldWorldId)) {
            if (oldWorldId != null && oldDimensionId != null && oldWorldKey != null) {
                ClaimSerialization.serializeClaims(oldWorldId, oldDimensionId, CLAIMS.getOrDefault(oldWorldKey, new HashMap<>()));
            }
            CLAIMS.clear();
            ClaimSerialization.deserializeClaims(currentWorldId, currentDimensionId);
        } else if (!Objects.equals(currentDimensionId, oldDimensionId)) {
            ClaimSerialization.deserializeClaims(currentWorldId, currentDimensionId);
        }
    };

    public static String getOwnerName(UUID owner) {
        if (owner == null) {
            return "Admin";
        }

        String name = OWNER_CACHE.computeIfAbsent(owner, k -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ProfileResult result = client.getSessionService().fetchProfile(owner, false);
            return result != null && result.profile() != null ? result.profile().getName() : null;
        });
        return name != null ? name : owner.toString();
    }

    public static boolean hasClaimWorld(RegistryKey<World> worldKey) {
        Map<Long, Claim> claimsById = CLAIM_BY_ID.get(worldKey);
        return claimsById != null && !claimsById.isEmpty();
    }

    public static boolean hasClaimRegion(RegistryKey<World> worldKey, int regionX, int regionZ) {
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIMS.get(worldKey);
        if (worldClaims == null) {
            return false;
        }
        Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(XaeroPluginClaims.pack(regionX, regionZ));
        return claimsInRegion != null && !claimsInRegion.isEmpty();
    }

    public static boolean hasClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        return getClaim(worldKey, chunk) != null;
    }

    public static Claim getClaim(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return getClaim(worldKey, new ChunkPos(chunkX, chunkZ));
    }

    public static Claim getClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIMS.get(worldKey);
        if (worldClaims != null) {
            Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ()));
            return claimsInRegion != null ? claimsInRegion.get(chunk) : null;
        }
        return null;
    }

    public static void addClaim(Claim claim) {
        deleteClaim(claim.worldKey(), claim.id());
        CLAIM_BY_ID.computeIfAbsent(claim.worldKey(), k -> new HashMap<>()).put(claim.id(), claim);
        for (ChunkPos chunk : claim.chunks()) {
            long region = XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ());
            CLAIMS.computeIfAbsent(claim.worldKey(), k -> new HashMap<>())
                    .computeIfAbsent(region, k -> new HashMap<>())
                    .put(chunk, claim);
        }
        onClaimAdded.accept(claim);
    }

    public static void deleteClaim(RegistryKey<World> worldKey, long id) {
        Map<Long, Claim> claimsById = CLAIM_BY_ID.get(worldKey);
        if (claimsById == null) {
            return;
        }
        Claim claim = claimsById.remove(id);
        if (claim == null) {
            return;
        }
        for (ChunkPos chunk : claim.chunks()) {
            long region = XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ());
            Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIMS.get(worldKey);
            if (worldClaims != null) {
                Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(region);
                if (claimsInRegion != null) {
                    claimsInRegion.remove(chunk);
                }
            }
        }
        onClaimRemoved.accept(claim);
    }

    public static void acceptWorldChanged(RegistryKey<World> worldKey) {
        onWorldChanged.accept(worldKey);
    }

    public static void onClaimAdded(Consumer<Claim> consumer) {
        onClaimAdded = onClaimAdded.andThen(consumer);
    }

    public static void onClaimRemoved(Consumer<Claim> consumer) {
        onClaimRemoved = onClaimRemoved.andThen(consumer);
    }

    public static void onWorldChanged(Consumer<RegistryKey<World>> consumer) {
        onWorldChanged = onWorldChanged.andThen(consumer);
    }
}
