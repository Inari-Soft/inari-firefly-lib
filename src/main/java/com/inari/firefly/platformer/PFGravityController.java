package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Easing;
import com.inari.firefly.animation.easing.EasingAnimation;
import com.inari.firefly.animation.easing.EasingData;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.physics.animation.Animation;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.system.external.FFTimer;

public final class PFGravityController extends EntityController {
    
    public static final AttributeKey<Easing.Type> EASING_TYPE = new AttributeKey<Easing.Type>( "easingType", Easing.Type.class, PFGravityController.class );
    public static final AttributeKey<Float> MAX_VELOCITY  = new AttributeKey<Float>( "maxVelocity", Float.class, PFGravityController.class );
    public static final AttributeKey<Long> TIME_TO_MAX  = new AttributeKey<Long>( "timeToMax", Long.class, PFGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        EASING_TYPE,
        MAX_VELOCITY,
        TIME_TO_MAX
    };
    
    private AnimationSystem animationSystem;
    private EntitySystem entitySystem;
    
    private Easing.Type easingType;
    private float maxVelocity;
    private long timeToMax;
    
    private int gravityAnimId;

    protected PFGravityController( int id ) {
        super( id );
    }
    
    @Override
    public final void init() {
        super.init();
        
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        
        gravityAnimId = context.getComponentBuilder( Animation.TYPE_KEY )
            .set( EasingAnimation.NAME, "PlatformerGravityControllerAnimation" )
            .set( EasingAnimation.LOOPING, false )
            .set( EasingAnimation.EASING_DATA, new EasingData( easingType, 0f, maxVelocity, timeToMax ) )
        .build( EasingAnimation.class );
    }

    @Override
    public final void dispose() {
        animationSystem.deleteAnimation( gravityAnimId );
        
        super.dispose();
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
        final EEntity entity = entitySystem.getComponent( entityId, EEntity.TYPE_KEY );
        
        if ( entity.hasAspect( PFState.ON_LADDER ) ) {
            return;
        }
        
        final EMovement movement = entitySystem.getComponent( entityId, EMovement.TYPE_KEY );
        float yVelocity = movement.getVelocityY();
        
        if ( !entity.hasAspect( PFState.ON_GROUND ) ) {
            if ( !animationSystem.isActive( gravityAnimId ) ) {
                animationSystem.activate( gravityAnimId, timer );
            }
            
            yVelocity = animationSystem.getValue( gravityAnimId, entityId, yVelocity );
        } else {
            yVelocity = 0f;
            animationSystem.resetAnimation( gravityAnimId );
        }
        
        movement.setVelocityY( yVelocity );
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
        
        easingType = attributes.getValue( EASING_TYPE, easingType );
        maxVelocity = attributes.getValue( MAX_VELOCITY, maxVelocity );
        timeToMax = attributes.getValue( TIME_TO_MAX, timeToMax );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( EASING_TYPE, easingType );
        attributes.put( MAX_VELOCITY, maxVelocity );
        attributes.put( TIME_TO_MAX, timeToMax );
    }

}
