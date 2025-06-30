package me.justahuman.pluginclaims.maps.journeymap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapPolygonWithHoles;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.PolygonHelper;
import journeymap.client.ui.theme.ThemeLabelSource;
import me.justahuman.pluginclaims.PluginClaims;
import me.justahuman.pluginclaims.claim.ClaimManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneyCompat implements IClientPlugin {
    private static final Map<Long, List<PolygonOverlay>> CLAIM_OVERLAYS = new HashMap<>();

    @Override
    public void initialize(IClientAPI api) {
        ThemeLabelSource.values.put(ClaimInfoSource.KEY, new ClaimInfoSource());
        ClaimManager.onClaimAdded(claim -> {
            List<PolygonOverlay> old = CLAIM_OVERLAYS.remove(claim.id());
            if (old != null) {
                old.forEach(api::remove);
            }

            ShapeProperties properties = new ShapeProperties()
                    .setFillColor(claim.color())
                    .setStrokeColor(claim.color());

            List<PolygonOverlay> overlays = new ArrayList<>();
            int i = 0;
            for (MapPolygonWithHoles polygon : PolygonHelper.createChunksPolygon(claim.chunks(), 10)) {
                try {
                    PolygonOverlay overlay = new PolygonOverlay(
                            getModId(),
                            claim.id() + "-" + i++,
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
    public void onEvent(ClientEvent event) {}

    @Override
    public String getModId() {
        return PluginClaims.MOD_ID;
    }
}
