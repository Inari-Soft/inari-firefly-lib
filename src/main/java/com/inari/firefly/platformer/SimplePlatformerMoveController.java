package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Easing;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.FFInitException;
import com.inari.firefly.animation.Animation;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.animation.easing.EasingAnimation;
import com.inari.firefly.animation.easing.EasingData;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.collision.BitMask;
import com.inari.firefly.physics.collision.CollisionSystem;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.system.external.FFInput;
import com.inari.firefly.system.external.FFInput.ButtonType;
import com.inari.firefly.system.external.FFTimer;

public final class SimplePlatformerMoveController extends EntityController {
    
    public static final AttributeKey<ButtonType> GO_LEFT_BUTTON_TYPE = new AttributeKey<ButtonType>( "goLeftButtonType", ButtonType.class, PlatformerGravityController.class );
    public static final AttributeKey<ButtonType> GO_RIGHT_BUTTON_TYPE = new AttributeKey<ButtonType>( "goRightButtonType", ButtonType.class, PlatformerGravityController.class );
    public static final AttributeKey<Easing.Type> EASING_TYPE = new AttributeKey<Easing.Type>( "easingType", Easing.Type.class, PlatformerGravityController.class );
    public static final AttributeKey<Float> MAX_VELOCITY  = new AttributeKey<Float>( "maxVelocity", Float.class, PlatformerGravityController.class );
    public static final AttributeKey<Long> TIME_TO_MAX  = new AttributeKey<Long>( "timeToMax", Long.class, PlatformerGravityController.class );
    public static final AttributeKey<String> TILE_GRID_NAME  = new AttributeKey<String>( "tileGridName", String.class, PlatformerGravityController.class );
    public static final AttributeKey<Integer> TILE_GRID_ID  = new AttributeKey<Integer>( "tileGridId", Integer.class, PlatformerGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        GO_LEFT_BUTTON_TYPE,
        GO_RIGHT_BUTTON_TYPE,
        EASING_TYPE,
        MAX_VELOCITY,
        TIME_TO_MAX,
        TILE_GRID_ID
    };
    
    private AnimationSystem animationSystem;
    private TileGridSystem tileGridSystem;
    private CollisionSystem collisionSystem;
    private EntitySystem entitySystem;
    
    private ButtonType goLeftButtonType;
    private ButtonType goRightButtonType = ButtonType.RIGHT;
    private Easing.Type easingType;
    private float maxVelocity;
    private long timeToMax;
    private int tileGridId = 0;
    
    private int startWalkAnimId;
    private final Position playerWorldPos = new Position();
    private final Position slopePivot = new Position();
    private final Rectangle groundVScanBounds = new Rectangle( 0, 0, 1, 10 );
    private final BitSet groundVScanBits = new BitSet( 10 );
    private final Rectangle groundHScanBounds = new Rectangle();

    protected SimplePlatformerMoveController( int id ) {
        super( id );
        
    }

    @Override
    public final void init() throws FFInitException {
        super.init();
        
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
        collisionSystem = context.getSystem( CollisionSystem.SYSTEM_KEY );
        entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        
        startWalkAnimId = context.getComponentBuilder( Animation.TYPE_KEY )
            .set( EasingAnimation.NAME, "SimplePlatformerMoveControllerAnimation" )
            .set( EasingAnimation.LOOPING, false )
            .set( EasingAnimation.EASING_DATA, new EasingData( Easing.Type.LINEAR, 0f, maxVelocity, timeToMax ) )
        .build( EasingAnimation.class );
    }

    @Override
    public final void dispose() {
        animationSystem.deleteAnimation( startWalkAnimId );
        
        super.dispose();
    }

    public final ButtonType getGoLeftButtonType() {
        return goLeftButtonType;
    }

    public final void setGoLeftButtonType( ButtonType goLeftButtonType ) {
        this.goLeftButtonType = goLeftButtonType;
    }

    public final ButtonType getGoRightButtonType() {
        return goRightButtonType;
    }

    public final void setGoRightButtonType( ButtonType goRightButtonType ) {
        this.goRightButtonType = goRightButtonType;
    }

    public final Easing.Type getEasingType() {
        return easingType;
    }

    public final void setEasingType( Easing.Type easingType ) {
        this.easingType = easingType;
    }

    public final float getMaxVelocity() {
        return maxVelocity;
    }

    public final void setMaxVelocity( float maxVelocity ) {
        this.maxVelocity = maxVelocity;
    }

    public final long getTimeToMax() {
        return timeToMax;
    }

    public final void setTimeToMax( long timeToMax ) {
        this.timeToMax = timeToMax;
    }

    public final int getTileGridId() {
        return tileGridId;
    }

    public final void setTileGridId( int tileGridId ) {
        this.tileGridId = tileGridId;
    }

    @Override
    protected final void update( FFTimer timer, int entityId ) {
        final FFInput input = context.getInput();
        final EMovement movement = entitySystem.getComponent( entityId, EMovement.TYPE_KEY );
        final ETransform transform = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
        float yVelocity = movement.getVelocityY();
        float xVelocity = movement.getVelocityX();
        
        // walking right/left
        if ( input.isPressed( goRightButtonType ) && xVelocity >= 0f ) {
            if ( xVelocity == 0f && !animationSystem.isActive( startWalkAnimId ) ) {
                animationSystem.activate( startWalkAnimId, timer );
            }
            
           if ( animationSystem.isActive( startWalkAnimId ) ) {
                xVelocity = animationSystem.getValue( startWalkAnimId, entityId, xVelocity );
           } else if ( xVelocity < maxVelocity ) {
               xVelocity = maxVelocity;
           }
        } else if ( input.isPressed( goLeftButtonType ) && xVelocity <= 0f ) {
            if ( xVelocity == 0f && !animationSystem.isActive( startWalkAnimId ) ) {
                animationSystem.activate( startWalkAnimId, timer );
            }
            
            if ( animationSystem.isActive( startWalkAnimId ) ) {
                xVelocity = -animationSystem.getValue( startWalkAnimId, entityId, xVelocity );
            } else if ( xVelocity > -maxVelocity ) {
                xVelocity = -maxVelocity;
            }
        } else if ( xVelocity != 0f ) {
            if ( Math.abs( xVelocity ) > 1f ) {
                xVelocity = ( xVelocity > 0f )? xVelocity - 0.3f : xVelocity + 0.3f;
            } else {
                xVelocity = 0f;
                animationSystem.resetAnimation( startWalkAnimId );
            }
        }
        
        movement.setVelocityX( xVelocity );
        
        if ( movement.isMoving() ) {
            final ECollision collision = entitySystem.getComponent( entityId, ECollision.TYPE_KEY );
            final Rectangle bounding = collision.getBounding();
            
            if ( slopePivot.x == 0 && slopePivot.y == 0 ) {
                slopePivot.x = bounding.x + bounding.width / 2;
                slopePivot.y = bounding.y + bounding.height - 1 - 4;
                groundHScanBounds.width = bounding.width;
                groundHScanBounds.height = 1;
            }
            
            playerWorldPos.x = ( xVelocity > 0 )? 
                    (int) Math.floor( transform.getXpos() + movement.getVelocityX() ) : 
                        (int) Math.ceil( transform.getXpos() + movement.getVelocityX() );
            playerWorldPos.y = ( yVelocity > 0 )? 
                    (int) Math.floor( transform.getYpos() + movement.getVelocityY() ) : 
                        (int) Math.ceil( transform.getYpos() + movement.getVelocityY() );
            
            if ( movement.hasStateFlag( PlatformerState.CONTACT_SOUTH ) ) {
                checkGroundContact( movement, bounding );
            }
                    
            adjustToGround( movement, transform );
        }
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
        
        goLeftButtonType = attributes.getValue( GO_LEFT_BUTTON_TYPE, goLeftButtonType );
        goRightButtonType = attributes.getValue( GO_RIGHT_BUTTON_TYPE, goRightButtonType );
        easingType = attributes.getValue( EASING_TYPE, easingType );
        maxVelocity = attributes.getValue( MAX_VELOCITY, maxVelocity );
        timeToMax = attributes.getValue( TIME_TO_MAX, timeToMax );
        tileGridId = attributes.getIdForName( TILE_GRID_NAME, TILE_GRID_ID, TileGrid.TYPE_KEY, tileGridId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( GO_LEFT_BUTTON_TYPE, goLeftButtonType );
        attributes.put( GO_RIGHT_BUTTON_TYPE, goRightButtonType );
        attributes.put( EASING_TYPE, easingType );
        attributes.put( MAX_VELOCITY, maxVelocity );
        attributes.put( TIME_TO_MAX, timeToMax );
        attributes.put( TILE_GRID_ID, tileGridId );
    }

    private void checkGroundContact( final EMovement movement, final Rectangle bounding ) {
        groundHScanBounds.x = playerWorldPos.x + bounding.x;
        groundHScanBounds.y = playerWorldPos.y + bounding.y + bounding.height;
        
        TileIterator groundTileScan = tileGridSystem.getTiles( tileGridId, groundHScanBounds );
        if ( !groundTileScan.hasNext() ) {
            movement.resetStateFlag( PlatformerState.CONTACT_SOUTH );
        } 
    }
    
    private void adjustToGround( final EMovement movement, final ETransform transform ) {
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
        
        if ( correction == 0 || ( correction > 0 && !movement.hasStateFlag( PlatformerState.CONTACT_SOUTH ) ) ) {
            return;
        }

        transform.setYpos( playerWorldPos.y + correction );
        movement.setVelocityY( 0f );
        movement.setStateFlag( PlatformerState.CONTACT_SOUTH );
    }

    private void addTileToGroundScanBits( final TileIterator groundTileScan ) {
        final int tileId = groundTileScan.next();
        if ( !entitySystem.getAspect( tileId ).contains( ECollision.TYPE_KEY ) ) {
            return;
        }
        
        final int tileWorldXpos = (int) groundTileScan.getWorldXPos();
        final int tileWorldYpos = (int) groundTileScan.getWorldYPos();
        final int xOffset = groundVScanBounds.x - tileWorldXpos;
        final int yOffset = groundVScanBounds.y - tileWorldYpos;
        final ECollision tileCollision = entitySystem.getComponent( tileId, ECollision.TYPE_KEY );
        final Rectangle bounding = tileCollision.getBounding();
        final int bitmaskId = tileCollision.getBitmaskId();
        
        if ( bitmaskId >= 0 ) {
            BitMask bitMask = collisionSystem.getBitMask( bitmaskId );
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
