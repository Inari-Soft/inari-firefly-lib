package com.inari.firefly.controller.view;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.geom.Position;
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
    private final Rectangle virtualViewBounds = new Rectangle();
    
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
        
        final Position following = pivot.getPivot();
        if ( following == null ) {
            return;
        }
        
        final Position worldPosition = view.getWorldPosition();
        final Rectangle viewBounds = view.getBounds();
        final int viewHorizontal = viewBounds.width / 4;
        final int viewHorizontalHalf = viewHorizontal / 2;
        final int viewVertical = viewBounds.height / 4;
        final int viewVerticalHalf = viewVertical / 2;
        
        final int xMax = snapToBounds.width - viewHorizontal;
        final int yMax = snapToBounds.height - viewVertical;

        boolean orientationChanged = false;
        
        virtualViewBounds.x = following.x + 4 - viewHorizontalHalf;
        virtualViewBounds.y = following.y + 4 - viewVerticalHalf;
        
        if ( virtualViewBounds.x < 0 ) {
            virtualViewBounds.x = 0;
        }
        if ( virtualViewBounds.y < 0 ) {
            virtualViewBounds.y = 0;
        }
        if ( virtualViewBounds.x > xMax ) {
            virtualViewBounds.x = xMax;
        }
        if ( virtualViewBounds.y > yMax ) {
            virtualViewBounds.y = yMax;
        }
        
        if ( worldPosition.x != virtualViewBounds.x || worldPosition.y != virtualViewBounds.y ) {
            orientationChanged = true;
            worldPosition.x = virtualViewBounds.x;
            worldPosition.y = virtualViewBounds.y;
        }
        
        if ( orientationChanged ) {
            viewChangeEvent.setView( view );
            context.notify( viewChangeEvent );
        }
    }

}
