package me.justahuman.claimmaps.mixin.xaero.minimap;

import me.justahuman.claimmaps.claim.ClaimManager;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.IXaeroMinimap;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.common.minimap.write.MinimapWriter;
import xaero.common.mods.SupportMods;

import java.util.List;

@Mixin(SupportMods.class)
public class SupportModsMixin {
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void init(IXaeroMinimap modMain, CallbackInfo ci) {
        ClaimManager.onClaimAdded(claim -> updateChunks(claim.chunks()));
        ClaimManager.onClaimRemoved(claim -> updateChunks(claim.chunks()));
        ClaimManager.onWorldChanged(world -> {
            XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
            if (minimapSession == null || minimapSession.getMinimapProcessor() == null) {
                return;
            }
            MinimapWriter write = minimapSession.getMinimapProcessor().getMinimapWriter();
            DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
            if (dimHighlightHandler != null) {
                dimHighlightHandler.requestRefresh();
            }
        });
    }

    @Unique
    public void updateChunks(List<ChunkPos> chunks) {
        XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
        if (minimapSession == null || minimapSession.getMinimapProcessor() == null) {
            return;
        }
        MinimapWriter write = minimapSession.getMinimapProcessor().getMinimapWriter();
        DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
        if (dimHighlightHandler != null) {
            for (ChunkPos chunk : chunks) {
                for (int i = -1; i < 2; ++i) {
                    for (int j = -1; j < 2; ++j) {
                        if (i == 0 && j == 0 || i * i != j * j) {
                            dimHighlightHandler.requestRefresh(chunk.x + i >> 5, chunk.z + j >> 5);
                        }
                    }
                }
            }
        }
    }
}
