package me.justahuman.claimmaps.maps.journeymap;

import journeymap.api.v2.client.display.IOverlayListener;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.fullscreen.ModPopupMenu;
import journeymap.api.v2.client.util.UIState;
import me.justahuman.claimmaps.claim.Claim;
import me.justahuman.claimmaps.claim.ClaimManager;
import net.minecraft.util.math.BlockPos;

import java.awt.geom.Point2D;

public record OverlayListener(Claim claim, PolygonOverlay overlay) implements IOverlayListener {
    @Override
    public void onActivate(UIState uiState) {}

    @Override
    public void onDeactivate(UIState uiState) {}

    @Override
    public void onMouseMove(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
        Claim other = ClaimManager.getClaim(mapState.dimension, blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        overlay.getTextProperties().setMinZoom(other != null && claim.id() == other.id() ? 250 : Integer.MAX_VALUE);
    }

    @Override
    public void onMouseOut(UIState uiState, Point2D.Double aDouble, BlockPos blockPos) {}

    @Override
    public boolean onMouseClick(UIState uiState, Point2D.Double aDouble, BlockPos blockPos, int i, boolean b) { return false; }

    @Override
    public void onOverlayMenuPopup(UIState uiState, Point2D.Double aDouble, BlockPos blockPos, ModPopupMenu modPopupMenu) {}
}
