package me.justahuman.claimmaps.claim;

import com.mojang.authlib.yggdrasil.ProfileResult;
import me.justahuman.claimmaps.ClaimMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ClaimManager {
    private static final Map<UUID, String> OWNER_CACHE = new HashMap<>();
    private static final List<ClaimSource> CLAIM_SOURCES = new ArrayList<>();

    private static String currentWorldId = null;
    private static Consumer<Claim> onClaimAdded = claim -> {};
    private static Consumer<Claim> onClaimRemoved = claim -> {};
    private static BiConsumer<String, ClientPlayNetworkHandler> onWorldChanged = (oldWorldId, world) -> {
        currentWorldId = world == null ? null : ClaimMaps.getWorldId(world);
    };

    public static void registerClaimSource(ClaimSource source) {
        CLAIM_SOURCES.add(source);
    }

    public static boolean hasClaimWorld(RegistryKey<World> worldKey) {
        for (ClaimSource source : CLAIM_SOURCES) {
            if (source.hasClaimWorld(worldKey)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasClaimRegion(RegistryKey<World> worldKey, int regionX, int regionZ) {
        for (ClaimSource source : CLAIM_SOURCES) {
            if (source.hasClaimRegion(worldKey, regionX, regionZ)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        return getClaim(worldKey, chunk) != null;
    }

    public static Claim getClaim(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return getClaim(worldKey, new ChunkPos(chunkX, chunkZ));
    }

    public static Claim getClaim(RegistryKey<World> worldKey, ChunkPos chunk) {
        for (ClaimSource source : CLAIM_SOURCES) {
            Claim claim = source.getClaim(worldKey, chunk);
            if (claim != null) {
                return claim;
            }
        }
        return null;
    }

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

    public static void acceptWorldChanged(ClientPlayNetworkHandler world) {
        onWorldChanged.accept(currentWorldId, world);
    }

    public static void onClaimAdded(Claim claim) {
        onClaimAdded.accept(claim);
    }

    public static void onClaimRemoved(Claim claim) {
        onClaimRemoved.accept(claim);
    }

    public static void onClaimAdded(Consumer<Claim> consumer) {
        onClaimAdded = onClaimAdded.andThen(consumer);
    }

    public static void onClaimRemoved(Consumer<Claim> consumer) {
        onClaimRemoved = onClaimRemoved.andThen(consumer);
    }

    public static void onWorldChanged(BiConsumer<String, ClientPlayNetworkHandler> consumer) {
        onWorldChanged = onWorldChanged.andThen(consumer);
    }
}
