package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.aspect.Aspects;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.system.external.FFInput;
import com.inari.firefly.system.external.FFInput.ButtonType;

public final class PFSimpleJumpController extends EntityController {
    
    public static final AttributeKey<ButtonType> JUMP_BUTTON_TYPE = new AttributeKey<ButtonType>( "jumpButtonType", ButtonType.class, PFSimpleJumpController.class );
    public static final AttributeKey<Float> MAX_VELOCITY  = new AttributeKey<Float>( "maxVelocity", Float.class, PFSimpleJumpController.class );
    public static final AttributeKey<Long> TIME_TO_MAX  = new AttributeKey<Long>( "timeToMax", Long.class, PFSimpleJumpController.class );
    public static final AttributeKey<DynArray<Aspect>> NO_JUMP_ASPECTS = AttributeKey.createDynArray( "noJumpAspects", PFSimpleJumpController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        JUMP_BUTTON_TYPE,
        MAX_VELOCITY,
        TIME_TO_MAX,
        NO_JUMP_ASPECTS
    };

    private ButtonType jumpButtonType;
    private float maxVelocity;
    private long timeToMax;
    private Aspects noJumpAspects;
    
    private float time = 0;
    private boolean jumpPressed = false;

    protected PFSimpleJumpController( int id ) {
        super( id );
        jumpButtonType = null;
        maxVelocity = -1;
        timeToMax = -1;
        noJumpAspects = null;
    }

    public final ButtonType getJumpButtonType() {
        return jumpButtonType;
    }

    public final void setJumpButtonType( ButtonType jumpButtonType ) {
        this.jumpButtonType = jumpButtonType;
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
    
    public final void setNoJumpAspect( Aspect aspect ) {
        if ( noJumpAspects == null ) {
            noJumpAspects = EEntity.ENTITY_ASPECT_GROUP.createAspects();
        }
        noJumpAspects.set( aspect );
    }
    
    public final void resetNoJumpAspect( Aspect aspect ) {
        if ( noJumpAspects == null ) {
            noJumpAspects = EEntity.ENTITY_ASPECT_GROUP.createAspects();
        }
        noJumpAspects.reset( aspect );
    }

    @Override
    protected final void update( int entityId ) {
        final EEntity entity = context.getEntityComponent( entityId, EEntity.TYPE_KEY );
        
        if ( noJumpAspects != null && !noJumpAspects.exclude( entity.getAspects() ) ) {
            return;
        }
        
        final FFInput input = context.getInput();
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        final boolean typed = input.typed( jumpButtonType );
        
        if ( typed && entity.hasAspect( PFState.ON_GROUND ) ) {
            movement.setVelocityY( -maxVelocity );
            entity.setAspect( PFState.JUMP );
            time = 0;
            jumpPressed = true;
            return;
        } 
            
        if ( !entity.hasAspect( PFState.JUMP ) ) {
            return;
        }
            
        if ( entity.hasAspect( PFState.ON_GROUND ) ) {
            entity.resetAspect( PFState.JUMP );
            entity.resetAspect( PFState.DOUBLE_JUMP );
            jumpPressed = false;
            return;
        }
        
        jumpPressed = jumpPressed && input.isPressed( jumpButtonType );
        
        if ( typed && !entity.hasAspect( PFState.DOUBLE_JUMP ) ) {
            time = 0;
            entity.setAspect( PFState.DOUBLE_JUMP );
            jumpPressed = true;
            return;
        }
        
        if ( time < timeToMax && jumpPressed ) {
            movement.setVelocityY( -maxVelocity );
            time += context.getTimeElapsed();
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
        
        jumpButtonType = attributes.getValue( JUMP_BUTTON_TYPE, jumpButtonType );
        maxVelocity = attributes.getValue( MAX_VELOCITY, maxVelocity );
        timeToMax = attributes.getValue( TIME_TO_MAX, timeToMax );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( JUMP_BUTTON_TYPE, jumpButtonType );
        attributes.put( MAX_VELOCITY, maxVelocity );
        attributes.put( TIME_TO_MAX, timeToMax );
    }

}
