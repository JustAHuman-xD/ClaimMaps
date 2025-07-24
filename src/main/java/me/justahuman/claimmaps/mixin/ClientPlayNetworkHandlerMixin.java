package me.justahuman.claimmaps.mixin;

import me.justahuman.claimmaps.claim.ClaimManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    public void onJoin(CallbackInfo ci) {
        ClaimManager.acceptWorldChanged(cast());
    }

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    public void onRespawn(CallbackInfo ci) {
        ClaimManager.acceptWorldChanged(cast());
    }

    @Inject(method = "clearWorld", at = @At("TAIL"))
    public void onWorldClear(CallbackInfo ci) {
        ClaimManager.acceptWorldChanged(null);
    }

    @Unique
    private ClientPlayNetworkHandler cast() {
        return (ClientPlayNetworkHandler) (Object) this;
    }
}
