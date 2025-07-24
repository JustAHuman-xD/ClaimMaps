package me.justahuman.claimmaps.maps.journeymap;

import journeymap.client.ui.theme.ThemeLabelSource;
import me.justahuman.claimmaps.ClaimMaps;
import me.justahuman.claimmaps.claim.Claim;
import me.justahuman.claimmaps.claim.ClaimManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ClaimInfoSource extends ThemeLabelSource.InfoSlot {
    public static final String KEY = "pluginclaims.current_claim";

    public ClaimInfoSource() {
        super(ClaimMaps.MOD_ID, KEY, 100, 1, () -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) {
                return Text.empty();
            }

            Claim claim = ClaimManager.getClaim(client.world.getRegistryKey(), client.player.getChunkPos());
            return claim != null ? claim.displayText() : Text.empty();
        });
    }
}
