package me.justahuman.pluginclaims.mixin.xaero.worldmap;

import com.llamalad7.mixinextras.sugar.Local;
import me.justahuman.pluginclaims.maps.xaero.worldmap.WorldmapClaimHighlighter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.WorldMapSession;
import xaero.map.highlight.HighlighterRegistry;

@Mixin(WorldMapSession.class)
public class WorldMapSessionMixin {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lxaero/map/highlight/HighlighterRegistry;end()V"), remap = false)
    public void addHighlighter(ClientPlayNetworkHandler connection, long biomeZoomSeed, CallbackInfo ci,
                               @Local HighlighterRegistry highlighterRegistry) {
        highlighterRegistry.register(new WorldmapClaimHighlighter());
    }
}
