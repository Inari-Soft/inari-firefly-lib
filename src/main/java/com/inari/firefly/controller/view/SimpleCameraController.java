package com.inari.firefly.controller.view;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.geom.PositionF;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.graphics.view.View;
import com.inari.firefly.graphics.view.ViewChangeEvent;
import com.inari.firefly.graphics.view.ViewChangeEvent.Type;
import com.inari.firefly.graphics.view.ViewController;
import com.inari.firefly.system.external.FFTimer;

public final class SimpleCameraController extends ViewController {
    
    public static final AttributeKey<CameraPivot> PIVOT = new AttributeKey<CameraPivot>( "pivot", CameraPivot.class, BorderedCameraController.class );
    public static final AttributeKey<Rectangle> SNAP_TO_BOUNDS = new AttributeKey<Rectangle>( "snapToBounds", Rectangle.class, BorderedCameraController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        PIVOT,
        SNAP_TO_BOUNDS
    };
    
    private CameraPivot pivot;
    private Rectangle snapToBounds;
    //private final Rectangle virtualViewBounds = new Rectangle();
    
    private final ViewChangeEvent viewChangeEvent = new ViewChangeEvent( null, Type.ORIENTATION );

    protected SimpleCameraController( int id ) {
        super( id );
    }

    public final CameraPivot getPivot() {
        return pivot;
    }

    public final void setPivot( CameraPivot pivot ) {
        this.pivot = pivot;
    }

    public final Rectangle getSnapToBounds() {
        return snapToBounds;
    }

    public final void setSnapToBounds( Rectangle snapToBounds ) {
        this.snapToBounds = snapToBounds;
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        pivot = attributes.getValue( PIVOT, pivot );
        snapToBounds = attributes.getValue( SNAP_TO_BOUNDS, snapToBounds );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( PIVOT, pivot );
        attributes.put( SNAP_TO_BOUNDS, snapToBounds );
    }

    @Override
    public final void update( FFTimer timer, View view ) {
        if ( pivot == null ) {
            return;
        }
        
        final PositionF following = pivot.getPivot();
        if ( following == null ) {
            return;
        }
        
        final Rectangle viewBounds = view.getBounds();
        final PositionF worldPosition = view.getWorldPosition();
        final float zoom = view.getZoom();
        final float viewHorizontal = viewBounds.width / 4;
        final float viewHorizontalHalf = viewHorizontal / 2;
        final float viewVertical = viewBounds.height / 4;
        final float viewVerticalHalf = viewVertical / 2;
        
        final float xMax = snapToBounds.width - viewHorizontal;
        final float yMax = snapToBounds.height - viewVertical;

        boolean orientationChanged = false;
        
        float xpos = following.x + 4 - viewHorizontalHalf;
        float ypos = following.y + 4 - viewVerticalHalf;
        
        if ( xpos < 0 ) {
            xpos = 0;
        }
        if ( ypos < 0 ) {
            ypos = 0;
        }
        if ( xpos > xMax ) {
            xpos = xMax;
        }
        if ( ypos > yMax ) {
            ypos = yMax;
        }

        // adjust xpos and ypos within the zoom to pixel correct position (otherwise there may be a flickering on texture area rendering while the camera is moving).
        xpos = ( (int) ( xpos / zoom ) ) * zoom;
        ypos = ( (int) ( ypos / zoom ) ) * zoom;
        
        if ( worldPosition.x != xpos || worldPosition.y != ypos ) {
            orientationChanged = true;
            worldPosition.x = xpos;
            worldPosition.y = ypos;
        }
        
        if ( orientationChanged ) {
            viewChangeEvent.setView( view );
            context.notify( viewChangeEvent );
        }
    }

}
