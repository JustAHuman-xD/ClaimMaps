package me.justahuman.pluginclaims.mixin.journeymap;

import journeymap.client.ui.minimap.DisplayVars;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeLabelSource;
import me.justahuman.pluginclaims.maps.journeymap.ClaimInfoSource;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DisplayVars.class, remap = false)
public abstract class DisplayVarsMixin {
    @Shadow protected abstract void positionLabels(TextRenderer fontRenderer, int centerX, int startY, Theme.LabelSpec labelSpec, ThemeLabelSource.InfoSlot... themeLabelSources);

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljourneymap/client/ui/minimap/DisplayVars;positionLabels(Lnet/minecraft/client/font/TextRenderer;IILjourneymap/client/ui/theme/Theme$LabelSpec;[Ljourneymap/client/ui/theme/ThemeLabelSource$InfoSlot;)V", ordinal = 1))
    public void addClaimInfoSource(DisplayVars instance, TextRenderer tuple, int themeLabelSource, int i, Theme.LabelSpec fontRenderer, ThemeLabelSource.InfoSlot[] centerX) {
        ThemeLabelSource.InfoSlot[] newSlots = new ThemeLabelSource.InfoSlot[centerX.length + 1];
        newSlots[0] = ThemeLabelSource.values.get(ClaimInfoSource.KEY);
        System.arraycopy(centerX, 0, newSlots, 1, centerX.length);
        positionLabels(tuple, themeLabelSource, i, fontRenderer, newSlots);
    }
}
