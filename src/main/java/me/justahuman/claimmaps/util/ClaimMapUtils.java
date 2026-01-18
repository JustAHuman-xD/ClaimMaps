package me.justahuman.claimmaps.util;

import me.justahuman.claimmaps.ClaimMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

public class ClaimMapUtils {
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
        return worldKey.getValue().getNamespace() + "$" + worldKey.getValue().getPath().replace('/', '%');
    }

    public static long pack(int x, int z) {
        return x & 4294967295L | (z & 4294967295L) << 32;
    }

    public static int[] unpack(long packed) {
        return new int[] { (int) packed, (int) (packed >> 32) };
    }

    public static Identifier id(String path) {
        return Identifier.of(ClaimMaps.MOD_ID, path);
    }
}
