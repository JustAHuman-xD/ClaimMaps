package me.justahuman.pluginclaims.maps.journeymap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.event.InfoSlotDisplayEvent;
import journeymap.api.v2.client.model.MapPolygonWithHoles;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.model.TextProperties;
import journeymap.api.v2.client.util.PolygonHelper;
import journeymap.api.v2.common.event.MinimapEventRegistry;
import journeymap.client.ui.theme.ThemeLabelSource;
import me.justahuman.pluginclaims.PluginClaims;
import me.justahuman.pluginclaims.claim.ClaimManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JourneyMapPlugin(apiVersion = "2.0.0-SNAPSHOT")
public class JourneyCompat implements IClientPlugin {
    private static final Map<Long, List<PolygonOverlay>> CLAIM_OVERLAYS = new HashMap<>();

    @Override
    public void initialize(IClientAPI api) {
        ThemeLabelSource.values.put(ClaimInfoSource.KEY, new ClaimInfoSource());
        MinimapEventRegistry.INFO_SLOT_DISPLAY_EVENT.subscribe(getModId(), event ->
                event.addBefore(ClaimInfoSource.KEY, InfoSlotDisplayEvent.Position.Bottom));
        ClaimManager.onClaimAdded(claim -> {
            List<PolygonOverlay> old = CLAIM_OVERLAYS.remove(claim.id());
            if (old != null) {
                old.forEach(api::remove);
            }

            ShapeProperties properties = new ShapeProperties()
                    .setFillColor(claim.color())
                    .setStrokeColor(claim.color());

            List<PolygonOverlay> overlays = new ArrayList<>();
            for (MapPolygonWithHoles polygon : PolygonHelper.createChunksPolygon(claim.chunks(), 10)) {
                try {
                    PolygonOverlay overlay = new PolygonOverlay(
                            getModId(),
                            claim.worldKey(),
                            properties,
                            polygon
                    );
                    overlay.setOverlayGroupName("Plugin Claims")
                            .setTitle(claim.display())
                            .setOverlayListener(new OverlayListener(claim, overlay))
                            .setTextProperties(new TextProperties()
                                    .setBackgroundColor(claim.color())
                                    .setMinZoom(250)
                                    .setFontShadow(true));
                    api.show(overlay);
                    overlays.add(overlay);
                } catch (Exception e) {
                    PluginClaims.LOGGER.error("Failed to create overlay for claim {} in world {}", claim.id(), claim.worldKey(), e);
                }
            }
            CLAIM_OVERLAYS.put(claim.id(), overlays);
        });
        ClaimManager.onClaimRemoved(claim -> {
            List<PolygonOverlay> overlays = CLAIM_OVERLAYS.remove(claim.id());
            if (overlays != null) {
                overlays.forEach(api::remove);
            }
        });

    }

    @Override
    public String getModId() {
        return PluginClaims.MOD_ID;
    }
}
