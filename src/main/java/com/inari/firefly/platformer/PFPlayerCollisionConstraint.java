package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.IntIterator;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.collision.BitMask;
import com.inari.firefly.physics.collision.CollisionConstraint;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.collision.RayScan;
import com.inari.firefly.physics.movement.EMovement;

public final class PFPlayerCollisionConstraint extends CollisionConstraint {
    
    public static final AttributeKey<String> DELEGATE_CONSTRAINT_NAME  = new AttributeKey<String>( "delegateConstraintName", String.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> DELEGATE_CONSTRAINT_ID  = new AttributeKey<Integer>( "delegateConstraintId", Integer.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> GROUND_SCAN_WIDTH  = new AttributeKey<Integer>( "groundScanWidth", Integer.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> GROUND_SCAN_HEIGHT  = new AttributeKey<Integer>( "groundScanHeight", Integer.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> GROUND_SCAN_EDGES  = new AttributeKey<Integer>( "groundScanEdges", Integer.class, PFPlayerCollisionConstraint.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        DELEGATE_CONSTRAINT_ID,
        GROUND_SCAN_WIDTH,
        GROUND_SCAN_HEIGHT,
        GROUND_SCAN_EDGES
    };
    
    private int delegateConstraintId = -1;
    private int groundScanEdges = 1;
    private final Rectangle groundHScanBounds = new Rectangle( 0, 0, 8, 1 );
    
    private final RayScan groundVScan = new RayScan( 10, false );
//    private final Rectangle groundVScanBounds = new Rectangle( 0, 0, 1, 10 );
//    private final BitSet groundVScanBits = new BitSet( 10 );

    protected PFPlayerCollisionConstraint( int id ) {
        super( id );
    }

    public final int getDelegateConstraintId() {
        return delegateConstraintId;
    }

    public final void setDelegateConstraintId( int delegateConstraintId ) {
        this.delegateConstraintId = delegateConstraintId;
    }

    public final int getGroundScanHeight() {
        return groundVScan.bounds.height;
    }

    public final void setGroundScanHeight( int groundScanHeight ) {
        groundVScan.bounds.height = groundScanHeight;
    }

    public final int getGroundScanWidth() {
        return groundHScanBounds.width;
    }

    public final void setGroundScanWidth( int groundScanWidth ) {
        groundHScanBounds.width = groundScanWidth;
    }

    public final int getGroundScanEdges() {
        return groundScanEdges;
    }

    public final void setGroundScanEdges( int groundScanEdges ) {
        this.groundScanEdges = groundScanEdges;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) ) );
        return attributeKeys;
    }
    
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        delegateConstraintId = attributes.getIdForName( 
            DELEGATE_CONSTRAINT_NAME, 
            DELEGATE_CONSTRAINT_ID, 
            CollisionConstraint.TYPE_KEY, 
            delegateConstraintId 
        );
        groundHScanBounds.width = attributes.getValue( GROUND_SCAN_WIDTH, groundHScanBounds.width );
        groundVScan.bounds.height = attributes.getValue( GROUND_SCAN_HEIGHT, groundVScan.bounds.height );
        groundScanEdges = attributes.getValue( GROUND_SCAN_EDGES, groundScanEdges );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( DELEGATE_CONSTRAINT_ID, delegateConstraintId );
        attributes.put( GROUND_SCAN_WIDTH, groundHScanBounds.width );
        attributes.put( GROUND_SCAN_HEIGHT, groundVScan.bounds.height );
        attributes.put( GROUND_SCAN_EDGES, groundScanEdges );
    }

    @Override
    public final void checkCollisions( int entityId, boolean update ) {
        if ( update ) {
            if ( delegateConstraintId >= 0 ) {
                context.getSystemComponent( CollisionConstraint.TYPE_KEY, delegateConstraintId ).checkCollisions( entityId, false );
            }
            return;
        }
        
        final TileGridSystem tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
        final EEntity entity = context.getEntityComponent( entityId, EEntity.TYPE_KEY );
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        final ETransform transform = context.getEntityComponent( entityId, ETransform.TYPE_KEY );
        final ECollision collision = context.getEntityComponent( entityId, ECollision.TYPE_KEY );

        groundVScan.clear();

        final int viewId = transform.getViewId();
        final IntBag layerIds = collision.getCollisionLayerIds();
        final Rectangle bounding = collision.getBounding();
        
        final int playerXpos = (int) Math.floor( transform.getXpos() );
        final int playerYpos = (int) Math.floor( transform.getYpos() );
        final float playerYVelocity = movement.getVelocityY();
        
        final boolean groundContact = entity.hasAspect( PFState.GROUND );
        entity.resetAspect( PFState.GROUND );
        
        groundHScanBounds.x = playerXpos + bounding.x;
        groundHScanBounds.y = playerYpos + bounding.y + bounding.height;
        groundVScan.bounds.x = playerXpos + bounding.x + bounding.width / 2;
        groundVScan.bounds.y = playerYpos + bounding.y + bounding.height - groundVScan.bounds.height / 2;
        
        if ( layerIds == null || layerIds.size() <= 0 ) {
            return;
        }
        
        IntIterator layerIterator = layerIds.iterator();
        while ( layerIterator.hasNext() ) {
            TileGrid tileGrid = tileGridSystem.getTileGrid( viewId, layerIterator.next() );
            if ( tileGrid != null && playerYVelocity >= 0 ) {
                if ( groundContact ) {
                    TileIterator groundTileScan = tileGridSystem.getTiles( tileGrid.index(), groundHScanBounds );
                    if ( checkOnGround( groundTileScan ) ) {
                        entity.setAspect( PFState.GROUND );
                    } 
                }
                
                TileIterator groundTileScanIterator = tileGridSystem.getTiles( tileGrid.index(), groundVScan.bounds );
                final int correction = adjustToGround( groundTileScanIterator );
                if ( correction != 0 && !( correction > 0 && !groundContact ) ) {
                    transform.setYpos( playerYpos + correction );
                    movement.setVelocityY( 0f );
                    entity.setAspect( PFState.GROUND );
                    break;
                }
            }
        }
        
        if ( delegateConstraintId >= 0 ) {
            context.getSystemComponent( CollisionConstraint.TYPE_KEY, delegateConstraintId ).checkCollisions( entityId, false );
        }
    }

    private boolean checkOnGround( TileIterator groundTileScanIterator ) {
        int xIntersection = 0;
        while ( groundTileScanIterator.hasNext() ) {
            final ECollision tileCollision = context.getEntityComponent( groundTileScanIterator.next(), ECollision.TYPE_KEY );
            if ( tileCollision == null || !tileCollision.isSolid() ) {
                continue;
            }
            final int groundx = (int) groundTileScanIterator.getWorldXPos();
            final int intersection;
            if ( groundx <= groundHScanBounds.x ) {
                intersection = groundx - groundHScanBounds.x + groundHScanBounds.width;
            } else {
                intersection = groundHScanBounds.x - groundx + groundHScanBounds.width;
            }
            xIntersection += intersection;
        }
        
        return ( xIntersection > groundScanEdges );
    }
    
    private int adjustToGround( final TileIterator groundTileScan ) {
        if ( !groundTileScan.hasNext() ) {
            return 0;
        }
        
        while ( groundTileScan.hasNext() ) {
            addTileToGroundScanBits( groundTileScan );
        }
        
        if ( groundVScan.isEmpty() ) {
            return 0;
        }
        
        int correction = 0;
        for ( int i = 0; i < 10; i++ ) {
            if ( groundVScan.scanBits.get( i ) ) {
                correction = -( 5 - i );
                break;
            }
        }
        
        return correction;
    }
    
    
    private void addTileToGroundScanBits( final TileIterator groundTileScan ) {
        final int tileId = groundTileScan.next();
        final ECollision tileCollision = context.getEntityComponent( tileId, ECollision.TYPE_KEY );
        if ( tileCollision == null || !tileCollision.isSolid() ) {
            return;
        }

        final int bitmaskId = tileCollision.getBitmaskId();
        if ( bitmaskId >= 0 ) { 
            groundVScan.setBitMask( context.getSystemComponent( BitMask.TYPE_KEY, bitmaskId ) );
        }
        
        groundVScan.addScan( 
            (int) groundTileScan.getWorldXPos(), 
            (int) groundTileScan.getWorldYPos() 
        );
    }

    


}
