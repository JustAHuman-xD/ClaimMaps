package me.justahuman.claimmaps.mixin.xaero.worldmap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.settings.ModOptions;

@Mixin(value = ModOptions.class, remap = false)
public class ModOptionsMixin {
    @Inject(method = "isDisabledBecausePac", at = @At("HEAD"), cancellable = true)
    public void disabledPac(CallbackInfoReturnable<Boolean> cir) {
        // We use these options for our claims, so we don't want to disable them
        cir.setReturnValue(false);
    }
}
