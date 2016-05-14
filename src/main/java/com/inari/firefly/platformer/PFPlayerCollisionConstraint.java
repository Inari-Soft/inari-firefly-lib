package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.IntIterator;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.collision.BitMask;
import com.inari.firefly.physics.collision.CollisionConstraint;
import com.inari.firefly.physics.collision.Collisions;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.movement.EMovement;

public final class PFPlayerCollisionConstraint extends CollisionConstraint {
    
    public static final AttributeKey<String> DELEGATE_CONSTRAINT_NAME  = new AttributeKey<String>( "delegateConstraintName", String.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> DELEGATE_CONSTRAINT_ID  = new AttributeKey<Integer>( "delegateConstraintId", Integer.class, PFPlayerCollisionConstraint.class );
    public static final AttributeKey<Integer> GROUND_SCAN_HEIGHT  = new AttributeKey<Integer>( "groundScanHeight", Integer.class, PFPlayerCollisionConstraint.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        DELEGATE_CONSTRAINT_ID,
        GROUND_SCAN_HEIGHT
    };
    
    private int delegateConstraintId = -1;
    
    private final Rectangle groundHScanBounds = new Rectangle( 0, 0, 8, 1 );
    private final Rectangle groundVScanBounds = new Rectangle( 0, 0, 1, 10 );
    private final BitSet groundVScanBits = new BitSet( 10 );

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
        return groundVScanBounds.height;
    }

    public final void setGroundScanHeight( int groundScanHeight ) {
        groundVScanBounds.height = groundScanHeight;
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
        groundVScanBounds.height = attributes.getValue( GROUND_SCAN_HEIGHT, groundVScanBounds.height );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( DELEGATE_CONSTRAINT_ID, delegateConstraintId );
        attributes.put( GROUND_SCAN_HEIGHT, groundVScanBounds.height );
    }

    @Override
    public final Collisions checkCollisions( int entityId ) {
        
        final TileGridSystem tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        final ETransform transform = context.getEntityComponent( entityId, ETransform.TYPE_KEY );
        final ECollision collision = context.getEntityComponent( entityId, ECollision.TYPE_KEY );
//        final EState state = context.getEntityComponent( entityId, EState.TYPE_KEY );
        
        groundVScanBits.clear();

        final int viewId = transform.getViewId();
        final IntBag layerIds = collision.getCollisionLayerIds();
        final Rectangle bounding = collision.getBounding();
        
        final int playerXpos = (int) Math.floor( transform.getXpos() );
        final int playerYpos = (int) Math.floor( transform.getYpos() );
        
        final boolean groundContact = collision.hasContact( PFContacts.GROUND );
        
        groundHScanBounds.x = playerXpos + bounding.x;
        groundHScanBounds.y = playerYpos + bounding.y + bounding.height;
        groundHScanBounds.width = bounding.width;
        groundVScanBounds.x = playerXpos + bounding.x + bounding.width / 2;
        groundVScanBounds.y = playerYpos + bounding.y + bounding.height - groundVScanBounds.height / 2;
        
        if ( layerIds == null || layerIds.size() <= 0 ) {
            return Collisions.EMPTY_COLLISSIONS;
        }
        
        IntIterator layerIterator = layerIds.iterator();

        while ( layerIterator.hasNext() ) {
            TileGrid tileGrid = tileGridSystem.getTileGrid( viewId, layerIterator.next() );
            if ( tileGrid != null ) {
                if ( groundContact ) {
                    TileIterator groundTileScan = tileGridSystem.getTiles( tileGrid.index(), groundHScanBounds );
                    if ( !groundTileScan.hasNext() ) {
                        collision.resetContact( PFContacts.GROUND );
                    } 
                }
                        
                if ( movement.getVelocityY() >= 0 ) {
                    TileIterator groundTileScanIterator = tileGridSystem.getTiles( tileGrid.index(), groundVScanBounds );
                    final int correction = adjustToGround( groundTileScanIterator );
                    if ( correction != 0 && !( correction > 0 && !collision.hasContact( PFContacts.GROUND ) ) ) {
                        transform.setYpos( playerYpos + correction );
                        movement.setVelocityY( 0f );
                        collision.setContact( PFContacts.GROUND );
                    }
                }
            }
        }
        
        if ( delegateConstraintId >= 0 ) {
            return context.getSystemComponent( CollisionConstraint.TYPE_KEY, delegateConstraintId ).checkCollisions( entityId );
        }
        
        return Collisions.EMPTY_COLLISSIONS;
    }
    
    private int adjustToGround( final TileIterator groundTileScan ) {
        if ( !groundTileScan.hasNext() ) {
            return 0;
        }
        
        while ( groundTileScan.hasNext() ) {
            addTileToGroundScanBits( groundTileScan );
        }
        
        if ( groundVScanBits.isEmpty() ) {
            return 0;
        }
        
        int correction = 0;
        for ( int i = 0; i < 10; i++ ) {
            if ( groundVScanBits.get( i ) ) {
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
        
        final int tileWorldXpos = (int) groundTileScan.getWorldXPos();
        final int tileWorldYpos = (int) groundTileScan.getWorldYPos();
        final int xOffset = groundVScanBounds.x - tileWorldXpos;
        final int yOffset = groundVScanBounds.y - tileWorldYpos;
        final Rectangle bounding = tileCollision.getBounding();
        final int bitmaskId = tileCollision.getBitmaskId();
        
        if ( bitmaskId >= 0 ) {
            BitMask bitMask = context.getSystemComponent( BitMask.TYPE_KEY, bitmaskId );
            for ( int i = 0; i < 10; i++ ) {
                if ( GeomUtils.contains( bounding, xOffset, yOffset + i ) ) {
                    groundVScanBits.set( i, bitMask.getBit( xOffset, yOffset + i ) );
                }
            }
        } else {
            for ( int i = 0; i < 10; i++ ) {
                if ( GeomUtils.contains( bounding, xOffset, yOffset + i ) ) {
                    groundVScanBits.set( i );
                }
            }
        }
    }

}
