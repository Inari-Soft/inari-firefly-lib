package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.FFInitException;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.control.state.EState;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.collision.BitMask;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.system.external.FFTimer;

public final class PFPlayerController extends EntityController {
    
    public static final AttributeKey<String> TILE_GRID_NAME  = new AttributeKey<String>( "tileGridName", String.class, PFGravityController.class );
    public static final AttributeKey<Integer> TILE_GRID_ID  = new AttributeKey<Integer>( "tileGridId", Integer.class, PFGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        TILE_GRID_ID
    };
    
    private TileGridSystem tileGridSystem;
    
    private int tileGridId = 0;
    
    private final Position playerWorldPos = new Position();
    private final Position slopePivot = new Position();
    private final Rectangle groundVScanBounds = new Rectangle( 0, 0, 1, 10 );
    private final BitSet groundVScanBits = new BitSet( 10 );
    private final Rectangle groundHScanBounds = new Rectangle();

    protected PFPlayerController( int id ) {
        super( id );
    }
    
    @Override
    public final void init() throws FFInitException {
        super.init();
        
        tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
    }
    
    public final int getTileGridId() {
        return tileGridId;
    }

    public final void setTileGridId( int tileGridId ) {
        this.tileGridId = tileGridId;
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
        
        tileGridId = attributes.getIdForName( TILE_GRID_NAME, TILE_GRID_ID, TileGrid.TYPE_KEY, tileGridId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TILE_GRID_ID, tileGridId );
    }

    @Override
    protected void update( FFTimer timer, int entityId ) {
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        
        if ( !movement.isMoving() ) {
            return;
        }
            
        final ETransform transform = context.getEntityComponent( entityId, ETransform.TYPE_KEY );
        final ECollision collision = context.getEntityComponent( entityId, ECollision.TYPE_KEY );
        final EState state = context.getEntityComponent( entityId, EState.TYPE_KEY );
        
        float yVelocity = movement.getVelocityY();
        float xVelocity = movement.getVelocityX();
        final Rectangle bounding = collision.getBounding();
        
        if ( slopePivot.x == 0 && slopePivot.y == 0 ) {
            slopePivot.x = bounding.x + bounding.width / 2;
            slopePivot.y = bounding.y + bounding.height - 5;
            groundHScanBounds.width = bounding.width;
            groundHScanBounds.height = 1;
        }
        
        playerWorldPos.x = ( xVelocity > 0 )? 
                (int) Math.floor( transform.getXpos() + movement.getVelocityX() ) : 
                    (int) Math.ceil( transform.getXpos() + movement.getVelocityX() );
        playerWorldPos.y = ( yVelocity > 0 )? 
                (int) Math.floor( transform.getYpos() + movement.getVelocityY() ) : 
                    (int) Math.ceil( transform.getYpos() + movement.getVelocityY() );
        
        if ( state.hasStateFlag( PFState.ON_GROUND ) ) {
            checkGroundContact( state, bounding );
        }
                
        if ( movement.getVelocityY() >= 0 ) {
            adjustToGround( movement, state, transform );
        }
    }
    
    private void checkGroundContact( final EState state, final Rectangle bounding ) {
        groundHScanBounds.x = playerWorldPos.x + bounding.x;
        groundHScanBounds.y = playerWorldPos.y + bounding.y + bounding.height;
        
        TileIterator groundTileScan = tileGridSystem.getTiles( tileGridId, groundHScanBounds );
        if ( !groundTileScan.hasNext() ) {
            state.resetStateFlag( PFState.ON_GROUND );
        } 
    }
    
    private void adjustToGround( final EMovement movement, final EState state, final ETransform transform ) {
        groundVScanBounds.x = playerWorldPos.x + slopePivot.x;
        groundVScanBounds.y = playerWorldPos.y + slopePivot.y;

        groundVScanBits.clear();
        
        TileIterator groundTileScan = tileGridSystem.getTiles( tileGridId, groundVScanBounds );
        if ( !groundTileScan.hasNext() ) {
            return;
        }
        
        while ( groundTileScan.hasNext() ) {
            addTileToGroundScanBits( groundTileScan );
        }
        
        if ( groundVScanBits.isEmpty() ) {
            return;
        }
        
        int correction = 0;
        for ( int i = 0; i < 10; i++ ) {
            if ( groundVScanBits.get( i ) ) {
                correction = -( 5 - i );
                break;
            }
        }
        
        if ( correction == 0 || ( correction > 0 && !state.hasStateFlag( PFState.ON_GROUND ) ) ) {
            return;
        }

        transform.setYpos( playerWorldPos.y + correction );
        movement.setVelocityY( 0f );
        state.setStateFlag( PFState.ON_GROUND );
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
