package me.justahuman.pluginclaims.maps.journeymap;

import journeymap.client.api.display.IOverlayListener;
import journeymap.client.api.display.ModPopupMenu;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.util.UIState;
import me.justahuman.pluginclaims.claim.Claim;
import me.justahuman.pluginclaims.claim.ClaimManager;
import net.minecraft.util.math.BlockPos;

import java.awt.geom.Point2D;

public record OverlayListener(Claim claim, PolygonOverlay overlay) implements IOverlayListener {
    @Override
    public void onActivate(UIState mapState) {}

    @Override
    public void onDeactivate(UIState mapState) {}

    @Override
    public void onMouseMove(UIState uiState, Point2D.Double aDouble, BlockPos blockPos) {
        Claim other = ClaimManager.getClaim(uiState.dimension, blockPos.getX() >> 4, blockPos.getZ() >> 4);
        overlay.getTextProperties().setMinZoom(other != null && claim.id() == other.id() ? 250 : Integer.MAX_VALUE);
    }

    @Override
    public void onMouseOut(UIState uiState, Point2D.Double aDouble, BlockPos blockPos) {}

    @Override
    public boolean onMouseClick(UIState uiState, Point2D.Double aDouble, BlockPos blockPos, int i, boolean b) {
        return false;
    }

    @Override
    public void onOverlayMenuPopup(UIState uiState, Point2D.Double aDouble, BlockPos blockPos, ModPopupMenu modPopupMenu) {}
}
