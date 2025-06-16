package me.justahuman.xaeropluginclaims.xaero.worldmap;

import me.justahuman.xaeropluginclaims.claim.Claim;
import me.justahuman.xaeropluginclaims.claim.ClaimHighlighter;
import me.justahuman.xaeropluginclaims.claim.ClaimManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import xaero.map.WorldMap;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;
import java.util.Objects;

public class WorldmapClaimHighlighter extends ChunkHighlighter implements ClaimHighlighter {
    private Text cachedTooltip;
    private Claim cachedTooltipFor;
    private String cachedForCustomName;
    private int cachedForClaimsColor;

    public WorldmapClaimHighlighter() {
        super(true);
    }

    public int calculateRegionHash(RegistryKey<World> worldKey, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims || !ClaimManager.hasClaimRegion(worldKey, regionX, regionZ)) {
            return 0;
        }

        int[] topRegion = { regionX, regionZ - 1 };
        int[] rightRegion = { regionX + 1, regionZ };
        int[] bottomRegion = { regionX, regionZ + 1 };
        int[] leftRegion = { regionX - 1, regionZ };
        long accumulator = WorldMap.settings.claimsBorderOpacity;
        accumulator = accumulator * 37L + (long) WorldMap.settings.claimsFillOpacity;

        for (int i = 0; i < 32; ++i) {
            accumulator = this.accountClaim(accumulator, getRegionClaim(worldKey, topRegion, i, 31));
            accumulator = this.accountClaim(accumulator, getRegionClaim(worldKey, rightRegion, 0, i));
            accumulator = this.accountClaim(accumulator, getRegionClaim(worldKey, bottomRegion, i, 0));
            accumulator = this.accountClaim(accumulator, getRegionClaim(worldKey, leftRegion, 31, i));
            for(int j = 0; j < 32; ++j) {
                accumulator = this.accountClaim(accumulator, getRegionClaim(worldKey, new int[] { regionX, regionZ }, i, j));
            }
        }
        return (int) (accumulator >> 32) * 37 + (int) (accumulator);
    }

    private Claim getRegionClaim(RegistryKey<World> worldKey, int[] region, int chunkX, int chunkZ) {
        return ClaimManager.getClaim(worldKey, region[0] * 8 + chunkX, region[1] * 8 + chunkZ);
    }

    private long accountClaim(long accumulator, Claim claim) {
        if (claim != null) {
            if (claim.owner() != null) {
                accumulator += claim.owner().getLeastSignificantBits();
                accumulator *= 37L;
                accumulator += claim.owner().getMostSignificantBits();
                accumulator *= 37L;
            }
            accumulator += claim.color();
        }
        accumulator *= 37L;
        return accumulator;
    }

    @Override
    public boolean regionHasHighlights(RegistryKey<World> worldKey, int regionX, int regionZ) {
        return hasHighlights(worldKey, regionX, regionX);
    }

    @Override
    public boolean chunkIsHighlit(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return chunkHighlighted(worldKey, chunkX, chunkZ);
    }

    @Override
    public int[] getColors(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return !WorldMap.settings.displayClaims ? null : highlightColor(worldKey, chunkX, chunkZ);
    }

    @Override
    public Text getChunkHighlightSubtleTooltip(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        Claim claim = ClaimManager.getClaim(worldKey, chunkX, chunkZ);
        if (claim == null) {
            return null;
        }

        String ownerName = ClaimManager.getOwnerName(claim.owner());
        String customName = claim.customName();
        int actualClaimsColor = claim.color();
        int claimsColor = actualClaimsColor | -16777216;
        if (!Objects.equals(claim, this.cachedTooltipFor) || this.cachedForClaimsColor != claimsColor || !Objects.equals(customName, this.cachedForCustomName)) {
            this.cachedTooltip = Text.literal("□ ").styled((s -> s.withColor(claimsColor)));
            this.cachedTooltip.getSiblings().add(Text.literal(ownerName + "'s Claim").formatted(Formatting.WHITE));
            if (!customName.isEmpty()) {
                this.cachedTooltip.getSiblings().add(0, Text.literal(I18n.translate(customName) + " - ").formatted(Formatting.WHITE));
            }

            this.cachedTooltipFor = claim;
            this.cachedForCustomName = customName;
            this.cachedForClaimsColor = claimsColor;
        }
        return this.cachedTooltip;
    }

    @Override
    public Text getChunkHighlightBluntTooltip(RegistryKey<World> worldKey, int blockX, int blockZ) { return null; }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Text> list, RegistryKey<World> worldKey, int blockX, int blockZ, int width) {}

    @Override
    public boolean displayClaims() {
        return WorldMap.settings.displayClaims;
    }

    @Override
    public int borderOpacity() {
        return WorldMap.settings.claimsBorderOpacity;
    }

    @Override
    public int fillOpacity() {
        return WorldMap.settings.claimsFillOpacity;
    }

    @Override
    public int[] resultStore() {
        return this.resultStore;
    }
}
