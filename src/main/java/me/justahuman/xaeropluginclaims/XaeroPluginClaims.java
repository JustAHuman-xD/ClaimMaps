package me.justahuman.xaeropluginclaims;

import com.mojang.logging.LogUtils;
import me.justahuman.xaeropluginclaims.claim.ClaimManager;
import me.justahuman.xaeropluginclaims.payload.ClaimPayload;
import me.justahuman.xaeropluginclaims.payload.DeleteClaimPayload;
import me.justahuman.xaeropluginclaims.payload.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class XaeroPluginClaims implements ClientModInitializer {
    public static final String MOD_ID = "xaeropluginclaims";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Path saveFolder;

    @Override
    public void onInitializeClient() {
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir().normalize();
            Path xaeroFolder = gameDir.resolve("xaero");
            if (!Files.exists(xaeroFolder)) {
                Files.createDirectories(xaeroFolder);
            }
            saveFolder = xaeroFolder.resolve("plugin-claims");
            if (!Files.exists(saveFolder)) {
                Files.createDirectories(saveFolder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create save folder(s) for Xaero Plugin Claims", e);
        }

        PayloadTypeRegistry.playS2C().register(Payloads.CLAIM, ClaimPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.DELETE_CLAIM, DeleteClaimPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(Payloads.CLAIM, (payload, context) -> ClaimManager.addClaim(payload.claim()));
        ClientPlayNetworking.registerGlobalReceiver(Payloads.DELETE_CLAIM, (payload, context) -> ClaimManager.deleteClaim(payload.worldKey(), payload.id()));
    }

    public static String getWorldId(ClientPlayNetworkHandler connection) {
        IntegratedServer integratedServer = MinecraftClient.getInstance().getServer();
        if (integratedServer != null) {
            String worldFolder = integratedServer.getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString();
            String rootId = worldFolder.replaceAll("_", "^us^");
            if (rootId.startsWith("Multiplayer_")) {
                rootId = "^e^" + rootId;
            }
            rootId = rootId.replace("[", "%lb%").replace("]", "%rb%");
            return rootId;
        }

        ServerInfo serverData = connection.getServerInfo();
        if (serverData == null) {
            return "Multiplayer_Unknown";
        }

        String serverIP = serverData.address;
        int portDivider = serverIP.indexOf(":") != serverIP.lastIndexOf(":") ? serverIP.lastIndexOf("]:") + 1 : serverIP.indexOf(":");
        if (portDivider > 0) {
            serverIP = serverIP.substring(0, portDivider);
        }
        while(serverIP.endsWith(".")) {
            serverIP = serverIP.substring(0, serverIP.length() - 1);
        }
        return "Multiplayer_" + serverIP.replace("[", "").replace("]", "").replaceAll(":", ".");
    }

    public static String getDimensionId(RegistryKey<World> worldKey) {
        if (worldKey == null) {
            return null;
        } else if (worldKey == World.OVERWORLD) {
            return "null";
        } else if (worldKey == World.NETHER) {
            return "DIM-1";
        } else if (worldKey == World.END) {
            return "DIM1";
        }
        return worldKey.getValue().getNamespace() + "$" + worldKey.getValue().getPath().replace('/', '%');
    }

    public static long pack(int x, int z) {
        return x & 4294967295L | (z & 4294967295L) << 32;
    }

    public static int[] unpack(long packed) {
        return new int[] { (int) packed, (int) (packed >> 32) };
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
