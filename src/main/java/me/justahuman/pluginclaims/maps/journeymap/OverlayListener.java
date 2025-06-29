package me.justahuman.pluginclaims.maps.journeymap;

import journeymap.api.v2.client.display.IOverlayListener;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.util.UIState;
import me.justahuman.pluginclaims.claim.Claim;
import me.justahuman.pluginclaims.claim.ClaimManager;
import net.minecraft.util.math.BlockPos;

import java.awt.geom.Point2D;

public record OverlayListener(Claim claim, PolygonOverlay overlay) implements IOverlayListener {
    @Override
    public void onMouseMove(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
        Claim other = ClaimManager.getClaim(mapState.dimension, blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        overlay.getTextProperties().setMinZoom(other != null && claim.id() == other.id() ? 250 : Integer.MAX_VALUE);
    }
}
