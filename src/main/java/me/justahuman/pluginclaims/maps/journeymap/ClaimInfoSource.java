package me.justahuman.pluginclaims.maps.journeymap;

import journeymap.client.ui.theme.ThemeLabelSource;
import me.justahuman.pluginclaims.PluginClaims;
import me.justahuman.pluginclaims.claim.Claim;
import me.justahuman.pluginclaims.claim.ClaimManager;
import net.minecraft.client.MinecraftClient;

public class ClaimInfoSource extends ThemeLabelSource.InfoSlot {
    public static final String KEY = "pluginclaims.current_claim";

    public ClaimInfoSource() {
        super(PluginClaims.MOD_ID, KEY, 100, 1, () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                return "";
            }

            Claim claim = ClaimManager.getClaim(client.world.getRegistryKey(), client.player.getChunkPos());
            return claim != null ? claim.display() : "";
        });
    }
}
