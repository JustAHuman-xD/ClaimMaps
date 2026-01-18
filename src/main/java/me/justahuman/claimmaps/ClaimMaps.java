package me.justahuman.claimmaps;

import com.mojang.logging.LogUtils;
import me.justahuman.claimmaps.payload.ClaimPayload;
import me.justahuman.claimmaps.payload.DeleteClaimPayload;
import me.justahuman.claimmaps.payload.NoClaimsPayload;
import me.justahuman.claimmaps.payload.Payloads;
import me.justahuman.claimmaps.implementation.sources.PluginClaims;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;

public class ClaimMaps implements ClientModInitializer {
    public static final String MOD_ID = "claimmaps";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(Payloads.CLAIM, ClaimPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.NO_CLAIMS, NoClaimsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.DELETE_CLAIM, DeleteClaimPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(Payloads.CLAIM, (payload, context) -> PluginClaims.INSTANCE.addClaim(payload.claim()));
        ClientPlayNetworking.registerGlobalReceiver(Payloads.NO_CLAIMS, (payload, context) -> PluginClaims.INSTANCE.removeClaims(payload.worldKey(), payload.chunkPos()));
        ClientPlayNetworking.registerGlobalReceiver(Payloads.DELETE_CLAIM, (payload, context) -> PluginClaims.INSTANCE.deleteClaim(payload.worldKey(), payload.id()));
    }
}