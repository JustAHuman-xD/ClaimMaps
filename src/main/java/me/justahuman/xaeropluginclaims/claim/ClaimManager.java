package me.justahuman.xaeropluginclaims.claim;

import com.mojang.authlib.yggdrasil.ProfileResult;
import me.justahuman.xaeropluginclaims.XaeroPluginClaims;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ClaimManager {
    private static final Map<UUID, String> OWNER_CACHE = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Claim>> CLAIM_BY_ID = new HashMap<>();
    private static final Map<RegistryKey<World>, Map<Long, Map<ChunkPos, Claim>>> CLAIM_BY_CHUNK = new HashMap<>();
    private static final Map<RegistryKey<World>, Set<Long>> DELETED_CLAIMS = new HashMap<>();
    private static String currentWorldId = null;
    private static Consumer<Claim> onClaimAdded = claim -> {};
    private static Consumer<Claim> onClaimRemoved = claim -> {};
    private static Consumer<ClientPlayNetworkHandler> onWorldChanged = world -> {
        if (world == null) {
            if (currentWorldId != null) {
                ClaimSerialization.serializeClaims(currentWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
            }
            currentWorldId = null;
            CLAIM_BY_ID.clear();
            CLAIM_BY_CHUNK.clear();
            DELETED_CLAIMS.clear();
            return;
        }

        String oldWorldId = currentWorldId;
        currentWorldId = XaeroPluginClaims.getWorldId(world);
        if (!currentWorldId.equals(oldWorldId)) {
            if (oldWorldId != null) {
                ClaimSerialization.serializeClaims(oldWorldId, CLAIM_BY_ID, DELETED_CLAIMS);
            }
            CLAIM_BY_ID.clear();
            CLAIM_BY_CHUNK.clear();
            DELETED_CLAIMS.clear();
            ClaimSerialization.deserializeClaims(currentWorldId);
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
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
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
        Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
        if (worldClaims != null) {
            Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ()));
            return claimsInRegion != null ? claimsInRegion.get(chunk) : null;
        }
        return null;
    }

    public static void addClaim(Claim claim) {
        deleteClaim(claim.worldKey(), claim.id());
        CLAIM_BY_ID.computeIfAbsent(claim.worldKey(), k -> new HashMap<>()).put(claim.id(), claim);
        DELETED_CLAIMS.computeIfAbsent(claim.worldKey(), k -> new HashSet<>()).remove(claim.id());
        for (ChunkPos chunk : claim.chunks()) {
            long region = XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ());
            CLAIM_BY_CHUNK.computeIfAbsent(claim.worldKey(), k -> new HashMap<>())
                    .computeIfAbsent(region, k -> new HashMap<>())
                    .put(chunk, claim);
        }
        onClaimAdded.accept(claim);
    }

    public static void deleteClaim(RegistryKey<World> worldKey, long id) {
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
            long region = XaeroPluginClaims.pack(chunk.getRegionX(), chunk.getRegionZ());
            Map<Long, Map<ChunkPos, Claim>> worldClaims = CLAIM_BY_CHUNK.get(worldKey);
            if (worldClaims != null) {
                Map<ChunkPos, Claim> claimsInRegion = worldClaims.get(region);
                if (claimsInRegion != null) {
                    claimsInRegion.remove(chunk);
                }
            }
        }
        onClaimRemoved.accept(claim);
    }

    public static void acceptWorldChanged(ClientPlayNetworkHandler world) {
        onWorldChanged.accept(world);
    }

    public static void onClaimAdded(Consumer<Claim> consumer) {
        onClaimAdded = onClaimAdded.andThen(consumer);
    }

    public static void onClaimRemoved(Consumer<Claim> consumer) {
        onClaimRemoved = onClaimRemoved.andThen(consumer);
    }

    public static void onWorldChanged(Consumer<ClientPlayNetworkHandler> consumer) {
        onWorldChanged = onWorldChanged.andThen(consumer);
    }
}
