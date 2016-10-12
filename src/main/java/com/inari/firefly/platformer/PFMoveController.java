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
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.physics.animation.Animation;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.collision.Contact;
import com.inari.firefly.physics.collision.ContactConstraint;
import com.inari.firefly.physics.collision.ContactScan;
import com.inari.firefly.physics.collision.ECollision;
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
    public static final AttributeKey<ButtonType> CLIMB_UP_BUTTON_TYPE = new AttributeKey<ButtonType>( "climbUpButtonType", ButtonType.class, PFGravityController.class );
    public static final AttributeKey<ButtonType> CLIMB_DOWN_BUTTON_TYPE = new AttributeKey<ButtonType>( "climbDownButtonType", ButtonType.class, PFGravityController.class );
    public static final AttributeKey<Float> CLIMB_VELOCITY = new AttributeKey<Float>( "climbVelocity", Float.class, PFGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        GO_LEFT_BUTTON_TYPE,
        GO_RIGHT_BUTTON_TYPE,
        EASING_TYPE,
        MAX_VELOCITY,
        TIME_TO_MAX,
        CLIMB_UP_BUTTON_TYPE,
        CLIMB_DOWN_BUTTON_TYPE,
        CLIMB_VELOCITY
    };
    
    private AnimationSystem animationSystem;
    private EntitySystem entitySystem;
    
    private ButtonType goLeftButtonType;
    private ButtonType goRightButtonType;
    private Easing.Type easingType;
    private float maxVelocity;
    private long timeToMax;
    private ButtonType climbUpButtonType;
    private ButtonType climbDownButtonType;
    private float climbVelocity;

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
    
    public final ButtonType getClimbUpButtonType() {
        return climbUpButtonType;
    }

    public final void setClimbUpButtonType( ButtonType climbUpButtonType ) {
        this.climbUpButtonType = climbUpButtonType;
    }

    public final ButtonType getClimbDownButtonType() {
        return climbDownButtonType;
    }

    public final void setClimbDownButtonType( ButtonType climbDownButtonType ) {
        this.climbDownButtonType = climbDownButtonType;
    }

    public final float getClimbVelocity() {
        return climbVelocity;
    }

    public final void setClimbVelocity( float climbVelocity ) {
        this.climbVelocity = climbVelocity;
    }

    @Override
    protected final void update( FFTimer timer, int entityId ) {
        final FFInput input = context.getInput();
        final ETransform transform = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
        final EMovement movement = entitySystem.getComponent( entityId, EMovement.TYPE_KEY );
        final EEntity entity = entitySystem.getComponent( entityId, EEntity.TYPE_KEY );
        final ECollision collision = entitySystem.getComponent( entityId, ECollision.TYPE_KEY );
        final ContactScan contactScan = collision.getContactScan();
        
        float xVelocity = movement.getVelocityX();
        float yVelocity = movement.getVelocityY();

        // walking right/left
        if ( input.isPressed( goRightButtonType ) && xVelocity >= 0f ) {
            entity.resetAspect( PFState.WALK_LEFT );
            entity.setAspect( PFState.WALK_RIGHT );
            if ( xVelocity == 0f && !animationSystem.isActive( startWalkAnimId ) ) {
                animationSystem.activate( startWalkAnimId, timer );
            }
            
           if ( animationSystem.isActive( startWalkAnimId ) ) {
                xVelocity = animationSystem.getValue( startWalkAnimId, entityId, xVelocity );
           } else if ( xVelocity < maxVelocity ) {
               xVelocity = maxVelocity;
           }
        } else if ( input.isPressed( goLeftButtonType ) && xVelocity <= 0f ) {
            entity.setAspect( PFState.WALK_LEFT );
            entity.resetAspect( PFState.WALK_RIGHT );
            if ( xVelocity == 0f && !animationSystem.isActive( startWalkAnimId ) ) {
                animationSystem.activate( startWalkAnimId, timer );
            }
            
            if ( animationSystem.isActive( startWalkAnimId ) ) {
                xVelocity = -animationSystem.getValue( startWalkAnimId, entityId, xVelocity );
            } else if ( xVelocity > -maxVelocity ) {
                xVelocity = -maxVelocity;
            }
        } else if ( xVelocity != 0f ) {
            entity.resetAspect( PFState.WALK_LEFT );
            entity.resetAspect( PFState.WALK_RIGHT );
            if ( Math.abs( xVelocity ) > 1f ) {
                xVelocity = ( xVelocity > 0f )? xVelocity - 0.3f : xVelocity + 0.3f;
            } else {
                xVelocity = 0f;
                animationSystem.resetAnimation( startWalkAnimId );
            }
        }

        final ContactConstraint ladderContacts = contactScan.getContactContstraint( PFContact.PLATFORMER_LADDER_CONTACT_SCAN );
        final boolean hasLadderContact = ladderContacts != null && ladderContacts.hasAnyContact();
        if ( input.isPressed( climbUpButtonType ) && hasLadderContact ) {
            final Contact contact = ladderContacts.getFirstContact( PFContact.LADDER );
            if ( contact.intersectionBounds().width > 3 ) {
                adjustToLadder( transform, entity, contact );
                yVelocity = -climbVelocity;
            }
        } else if ( input.isPressed( climbDownButtonType ) && hasLadderContact ) {
            final Contact contact = ladderContacts.getFirstContact( PFContact.LADDER );
            adjustToLadder( transform, entity, contact );
            yVelocity = climbVelocity;
        } else {
            yVelocity = 0;
            entity.resetAspect( PFState.CLIMB_UP );
        }
        
        movement.setVelocity( xVelocity, yVelocity );
    }

    private void adjustToLadder( final ETransform transform, final EEntity entity, final Contact contact ) {
        transform.setXpos( contact.worldBounds().x );
        entity.resetAspects();
        entity.setAspect( PFState.ON_LADDER );
        entity.setAspect( PFState.CLIMB_UP );
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
        climbUpButtonType = attributes.getValue( CLIMB_UP_BUTTON_TYPE, climbUpButtonType );
        climbDownButtonType = attributes.getValue( CLIMB_DOWN_BUTTON_TYPE, climbDownButtonType );
        climbVelocity = attributes.getValue( CLIMB_VELOCITY, climbVelocity );
        easingType = attributes.getValue( EASING_TYPE, easingType );
        maxVelocity = attributes.getValue( MAX_VELOCITY, maxVelocity );
        timeToMax = attributes.getValue( TIME_TO_MAX, timeToMax );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( GO_LEFT_BUTTON_TYPE, goLeftButtonType );
        attributes.put( GO_RIGHT_BUTTON_TYPE, goRightButtonType );
        attributes.put( CLIMB_UP_BUTTON_TYPE, climbUpButtonType );
        attributes.put( CLIMB_DOWN_BUTTON_TYPE, climbDownButtonType );
        attributes.put( CLIMB_VELOCITY, climbVelocity );
        attributes.put( EASING_TYPE, easingType );
        attributes.put( MAX_VELOCITY, maxVelocity );
        attributes.put( TIME_TO_MAX, timeToMax );
    }
}
