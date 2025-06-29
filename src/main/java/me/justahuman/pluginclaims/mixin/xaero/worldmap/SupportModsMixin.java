package me.justahuman.pluginclaims.mixin.xaero.worldmap;

import me.justahuman.pluginclaims.claim.ClaimManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.WorldMapSession;
import xaero.map.mods.SupportMods;
import xaero.map.world.MapDimension;

import java.util.List;

@Mixin(SupportMods.class)
public class SupportModsMixin {
    @Inject(method = "load", at = @At("TAIL"), remap = false)
    public void load(CallbackInfo ci) {
        ClaimManager.onClaimAdded(claim -> updateChunks(claim.worldKey(), claim.chunks()));
        ClaimManager.onClaimRemoved(claim -> updateChunks(claim.worldKey(), claim.chunks()));
        ClaimManager.onWorldChanged(world -> {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            if (session == null || session.getMapProcessor() == null || MinecraftClient.getInstance().world == null) {
                return;
            }
            MapDimension mapDim = session.getMapProcessor().getMapWorld().getDimension(MinecraftClient.getInstance().world.getRegistryKey());
            if (mapDim != null) {
                mapDim.getHighlightHandler().clearCachedHashes();
            }
        });
    }

    @Unique
    private void updateChunks(RegistryKey<World> worldKey, List<ChunkPos> chunks) {
        WorldMapSession session = WorldMapSession.getCurrentSession();
        if (session == null || session.getMapProcessor() == null) {
            return;
        }
        MapDimension mapDim = session.getMapProcessor().getMapWorld().getDimension(worldKey);
        if (mapDim != null) {
            for (ChunkPos chunk : chunks) {
                for(int i = -1; i < 2; ++i) {
                    for(int j = -1; j < 2; ++j) {
                        if (i == 0 && j == 0 || i * i != j * j) {
                            mapDim.getHighlightHandler().clearCachedHash(chunk.x + i >> 5, chunk.z + j >> 5);
                        }
                    }
                }
            }
        }
    }
}
