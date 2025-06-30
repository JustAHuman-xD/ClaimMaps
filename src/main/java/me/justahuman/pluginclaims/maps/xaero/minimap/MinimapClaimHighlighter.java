package me.justahuman.pluginclaims.maps.xaero.minimap;

import me.justahuman.pluginclaims.claim.Claim;
import me.justahuman.pluginclaims.claim.ClaimHighlighter;
import me.justahuman.pluginclaims.claim.ClaimManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.highlight.ChunkHighlighter;
import xaero.common.minimap.info.render.compile.InfoDisplayCompiler;
import xaero.common.misc.TextSplitter;
import xaero.common.settings.ModSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MinimapClaimHighlighter extends ChunkHighlighter implements ClaimHighlighter {
    private final ModSettings settings;
    private List<Text> cachedTooltip;
    private Claim cachedTooltipFor;
    private int cachedForWidth;
    private String cachedForCustomName;
    private int cachedForClaimsColor;

    public MinimapClaimHighlighter(IXaeroMinimap minimap) {
        super(true);
        this.settings = minimap.getSettings();
    }

    @Override
    public boolean regionHasHighlights(RegistryKey<World> worldKey, int regionX, int regionZ) {
        return hasHighlights(worldKey, regionX, regionZ);
    }

    @Override
    public boolean chunkIsHighlit(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return chunkHighlighted(worldKey, chunkX, chunkZ);
    }

    @Override
    public int[] getColors(RegistryKey<World> worldKey, int chunkX, int chunkZ) {
        return !this.settings.getDisplayClaims() ? null : highlightColor(worldKey, chunkX, chunkZ);
    }

    @Override
    public void addChunkHighlightTooltips(InfoDisplayCompiler compiler, RegistryKey<World> worldKey, int chunkX, int chunkZ, int width) {
        if (!this.settings.displayCurrentClaim) {
            return;
        }
        Claim claim = ClaimManager.getClaim(worldKey, chunkX, chunkZ);
        if (claim == null) {
            return;
        }

        String customName = claim.customName();
        int actualClaimsColor = claim.color();
        int claimsColor = actualClaimsColor | -16777216;
        if (!Objects.equals(claim, this.cachedTooltipFor) || this.cachedForWidth != width || this.cachedForClaimsColor != claimsColor || !Objects.equals(customName, this.cachedForCustomName)) {
            this.cachedTooltip = new ArrayList<>();
            TextSplitter.splitTextIntoLines(this.cachedTooltip, width, width, claim.displayText(), null);
            this.cachedTooltipFor = claim;
            this.cachedForWidth = width;
            this.cachedForCustomName = customName;
            this.cachedForClaimsColor = claimsColor;
        }

        for (Text text : this.cachedTooltip) {
            compiler.addLine(text);
        }
    }

    @Override
    public boolean displayClaims() {
        return settings.getDisplayClaims();
    }

    @Override
    public int borderOpacity() {
        return settings.getClaimsBorderOpacity();
    }

    @Override
    public int fillOpacity() {
        return settings.getClaimsFillOpacity();
    }

    @Override
    public int[] resultStore() {
        return this.resultStore;
    }
}
