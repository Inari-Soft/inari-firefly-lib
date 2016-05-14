package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Easing;
import com.inari.firefly.FFInitException;
import com.inari.firefly.animation.easing.EasingAnimation;
import com.inari.firefly.animation.easing.EasingData;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.physics.animation.Animation;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.system.external.FFInput;
import com.inari.firefly.system.external.FFInput.ButtonType;
import com.inari.firefly.system.external.FFTimer;

public final class PFMoveController extends EntityController {
    
    public static final AttributeKey<ButtonType> GO_LEFT_BUTTON_TYPE = new AttributeKey<ButtonType>( "goLeftButtonType", ButtonType.class, PFGravityController.class );
    public static final AttributeKey<ButtonType> GO_RIGHT_BUTTON_TYPE = new AttributeKey<ButtonType>( "goRightButtonType", ButtonType.class, PFGravityController.class );
    public static final AttributeKey<Easing.Type> EASING_TYPE = new AttributeKey<Easing.Type>( "easingType", Easing.Type.class, PFGravityController.class );
    public static final AttributeKey<Float> MAX_VELOCITY  = new AttributeKey<Float>( "maxVelocity", Float.class, PFGravityController.class );
    public static final AttributeKey<Long> TIME_TO_MAX  = new AttributeKey<Long>( "timeToMax", Long.class, PFGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        GO_LEFT_BUTTON_TYPE,
        GO_RIGHT_BUTTON_TYPE,
        EASING_TYPE,
        MAX_VELOCITY,
        TIME_TO_MAX
    };
    
    private AnimationSystem animationSystem;
    private EntitySystem entitySystem;
    
    private ButtonType goLeftButtonType;
    private ButtonType goRightButtonType = ButtonType.RIGHT;
    private Easing.Type easingType;
    private float maxVelocity;
    private long timeToMax;

    private int startWalkAnimId;

    protected PFMoveController( int id ) {
        super( id );
        
    }

    @Override
    public final void init() throws FFInitException {
        super.init();
        
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
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

    @Override
    protected final void update( FFTimer timer, int entityId ) {
        final FFInput input = context.getInput();
        final EMovement movement = entitySystem.getComponent( entityId, EMovement.TYPE_KEY );
//        final EState state = entitySystem.getComponent( entityId, EState.TYPE_KEY );
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
        
//        state.resetStateAspect( PFState.GO_RIGHT );
//        state.resetStateAspect( PFState.GO_LEFT );
//        if ( xVelocity > 0 ) {
//            state.setStateAspect( PFState.GO_RIGHT );
//        } else if ( xVelocity < 0 ) {
//            state.setStateAspect( PFState.GO_LEFT );
//        }
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
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( GO_LEFT_BUTTON_TYPE, goLeftButtonType );
        attributes.put( GO_RIGHT_BUTTON_TYPE, goRightButtonType );
        attributes.put( EASING_TYPE, easingType );
        attributes.put( MAX_VELOCITY, maxVelocity );
        attributes.put( TIME_TO_MAX, timeToMax );
    }
}
