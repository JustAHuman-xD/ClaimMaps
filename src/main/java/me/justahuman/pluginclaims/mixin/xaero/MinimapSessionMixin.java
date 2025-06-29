package me.justahuman.pluginclaims.mixin.xaero.minimap;

import com.llamalad7.mixinextras.sugar.Local;
import me.justahuman.pluginclaims.maps.xaero.minimap.MinimapClaimHighlighter;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.minimap.highlight.HighlighterRegistry;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.module.HudModule;

@Mixin(MinimapSession.class)
public class MinimapSessionMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lxaero/common/minimap/highlight/HighlighterRegistry;end()V"), remap = false)
    public void addHighlighter(HudMod modMain, HudModule<MinimapSession> _module, ClientPlayNetworkHandler connection, CallbackInfo ci,
                                @Local HighlighterRegistry highlighterRegistry) {
        highlighterRegistry.register(new MinimapClaimHighlighter(modMain));
    }
}
