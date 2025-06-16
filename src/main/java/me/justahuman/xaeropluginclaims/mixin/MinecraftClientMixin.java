package me.justahuman.xaeropluginclaims.mixin;

import me.justahuman.xaeropluginclaims.claim.ClaimManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setWorld", at = @At("TAIL"))
    public void onWorldChange(ClientWorld world, CallbackInfo ci) {
        ClaimManager.acceptWorldChanged(world == null ? null : world.getRegistryKey());
    }
}
