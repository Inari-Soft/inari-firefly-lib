package com.inari.firefly.controller.view;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.geom.Direction;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.graphics.view.View;
import com.inari.firefly.graphics.view.ViewChangeEvent;
import com.inari.firefly.graphics.view.ViewChangeEvent.Type;
import com.inari.firefly.graphics.view.ViewController;
import com.inari.firefly.system.external.FFTimer;

public final class BorderedCameraController extends ViewController {
    
    public static final AttributeKey<CameraPivot> PIVOT = new AttributeKey<CameraPivot>( "pivot", CameraPivot.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> H_ON_THRESHOLD = new AttributeKey<Integer>( "hOnThreshold", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> V_ON_THRESHOLD = new AttributeKey<Integer>( "vOnThreshold", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> H_OFF_THRESHOLD = new AttributeKey<Integer>( "hOffThreshold", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> V_OFF_THRESHOLD = new AttributeKey<Integer>( "vOffThreshold", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> H_VELOCITY = new AttributeKey<Integer>( "hVelocity", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Integer> V_VELOCITY = new AttributeKey<Integer>( "vVelocity", Integer.class, BorderedCameraController.class );
    public static final AttributeKey<Rectangle> SNAP_TO_BOUNDS = new AttributeKey<Rectangle>( "snapToBounds", Rectangle.class, BorderedCameraController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        PIVOT,
        H_ON_THRESHOLD,
        V_ON_THRESHOLD,
        H_OFF_THRESHOLD,
        V_OFF_THRESHOLD,
        H_VELOCITY,
        V_VELOCITY,
        SNAP_TO_BOUNDS
    };
    
    private CameraPivot pivot;
    private int hOnThreshold;
    private int vOnThreshold;
    private int hOffThreshold;
    private int vOffThreshold;
    private int hVelocity = 0;
    private int vVelocity = 0;
    private Rectangle snapToBounds;
    
    private Direction hMove = Direction.NONE;
    private Direction vMove = Direction.NONE;
    private final Rectangle virtualViewBounds = new Rectangle();
    
    private final ViewChangeEvent viewChangeEvent = new ViewChangeEvent( null, Type.ORIENTATION );

    protected BorderedCameraController( int id ) {
        super( id );
    }

    public final CameraPivot getPivot() {
        return pivot;
    }

    public final void setPivot( CameraPivot pivot ) {
        this.pivot = pivot;
    }

    public final int gethOnThreshold() {
        return hOnThreshold;
    }

    public final void sethOnThreshold( int hOnThreshold ) {
        this.hOnThreshold = hOnThreshold;
    }

    public final int getvOnThreshold() {
        return vOnThreshold;
    }

    public final void setvOnThreshold( int vOnThreshold ) {
        this.vOnThreshold = vOnThreshold;
    }

    public final int gethOffThreshold() {
        return hOffThreshold;
    }

    public final void sethOffThreshold( int hOffThreshold ) {
        this.hOffThreshold = hOffThreshold;
    }

    public final int getvOffThreshold() {
        return vOffThreshold;
    }

    public final void setvOffThreshold( int vOffThreshold ) {
        this.vOffThreshold = vOffThreshold;
    }

    public final int gethVelocity() {
        return hVelocity;
    }

    public final void sethVelocity( int hVelocity ) {
        this.hVelocity = hVelocity;
    }

    public final int getvVelocity() {
        return vVelocity;
    }

    public final void setvVelocity( int vVelocity ) {
        this.vVelocity = vVelocity;
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
        hOnThreshold = attributes.getValue( H_ON_THRESHOLD, hOnThreshold );
        vOnThreshold = attributes.getValue( V_ON_THRESHOLD, vOnThreshold );
        hOffThreshold = attributes.getValue( H_OFF_THRESHOLD, hOffThreshold );
        vOffThreshold = attributes.getValue( V_OFF_THRESHOLD, vOffThreshold );
        hVelocity = attributes.getValue( H_VELOCITY, hVelocity );
        vVelocity = attributes.getValue( V_VELOCITY, vVelocity );
        snapToBounds = attributes.getValue( SNAP_TO_BOUNDS, snapToBounds );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( PIVOT, pivot );
        attributes.put( H_ON_THRESHOLD, hOnThreshold );
        attributes.put( V_ON_THRESHOLD, vOnThreshold );
        attributes.put( H_OFF_THRESHOLD, hOffThreshold );
        attributes.put( V_OFF_THRESHOLD, vOffThreshold );
        attributes.put( H_VELOCITY, hVelocity );
        attributes.put( V_VELOCITY, vVelocity );
        attributes.put( SNAP_TO_BOUNDS, snapToBounds );
    }

    @Override
    public final void update( FFTimer timer, View view ) {
        if ( pivot == null ) {
            return;
        }
        Position following = pivot.getPivot();
        if ( following == null ) {
            return;
        }
        
        // TODO find the right bounds, combine worldposition with view bounds
        Rectangle viewBounds = view.getBounds();
        Position worldPosition = view.getWorldPosition();
        virtualViewBounds.setFrom( worldPosition );
        virtualViewBounds.width = viewBounds.width;
        virtualViewBounds.height = viewBounds.height;
        
        float zoom = view.getZoom();
        if ( zoom != 1 ) {
            virtualViewBounds.width = (int) ( virtualViewBounds.width * zoom );
            virtualViewBounds.height = (int) ( virtualViewBounds.height * zoom );
        }
        
        updateMoveDirection( virtualViewBounds, following );
        boolean orientationChanged = false;
        
        switch ( hMove ) {
            case EAST: {
                if ( snapToBounds != null && virtualViewBounds.x + virtualViewBounds.width >= snapToBounds.x + snapToBounds.width ) {
                    break;
                }
                worldPosition.x += hVelocity;
                orientationChanged = true;
                break;
            }
            case WEST: {
                if ( snapToBounds != null && virtualViewBounds.x <= snapToBounds.x ) {
                    break;
                }
                worldPosition.x -= hVelocity;
                orientationChanged = true;
                break;
            }
            default: {}
        }
        
        switch ( vMove ) {
            case NORTH: {
                if ( snapToBounds != null && virtualViewBounds.y <= snapToBounds.y ) {
                    break;
                }
                worldPosition.y -= vVelocity;
                orientationChanged = true;
                break;
            }
            case SOUTH: {
                if ( snapToBounds != null && virtualViewBounds.y + virtualViewBounds.height >= snapToBounds.y + snapToBounds.height ) {
                    break;
                }
                worldPosition.y += vVelocity;
                orientationChanged = true;
                break;
            }
            default: {}
        }
        
        if ( orientationChanged ) {
            viewChangeEvent.setView( view );
            context.notify( viewChangeEvent );
        }
    }
    
    private final void updateMoveDirection( Rectangle viewBounds, Position following ) {
        if ( following.x < viewBounds.x + hOnThreshold ) {
            hMove = Direction.WEST;
        } else if ( following.x > viewBounds.x + viewBounds.width - hOnThreshold ) {
            hMove = Direction.EAST;
        } else if ( ( following.x > viewBounds.x + hOffThreshold ) && ( following.x < viewBounds.x + viewBounds.width - hOffThreshold ) ) {
            hMove = Direction.NONE;
        }
        
        if ( following.y < viewBounds.y + vOnThreshold ) {
            vMove = Direction.NORTH;
        } else if ( following.y > viewBounds.y + viewBounds.height - vOnThreshold ) {
            vMove = Direction.SOUTH;
        } else if ( ( following.y > viewBounds.y + vOffThreshold ) && ( following.y < viewBounds.y + viewBounds.height - vOffThreshold ) ) {
            vMove = Direction.NONE;
        }
    }

}
