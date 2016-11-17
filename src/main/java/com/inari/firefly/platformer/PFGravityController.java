package com.inari.firefly.platformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.physics.movement.EMovement;

public final class PFGravityController extends EntityController {
    
    public static final AttributeKey<Float> MAX_VELOCITY  = new AttributeKey<Float>( "maxVelocity", Float.class, PFGravityController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        MAX_VELOCITY,
    };
    
    private float maxVelocity;
   
    protected PFGravityController( int id ) {
        super( id );
    }

    public final float getMaxVelocity() {
        return maxVelocity;
    }

    public final void setMaxVelocity( float maxVelocity ) {
        this.maxVelocity = maxVelocity;
    }

    @Override
    protected final void update( int entityId ) {
        final EEntity entity = context.getEntityComponent( entityId, EEntity.TYPE_KEY );
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        
        if ( !entity.hasAspect( PFState.ON_GROUND ) ) {
            float velocityY = movement.getVelocityY();
            velocityY = velocityY + Math.abs( ( velocityY / maxVelocity - 1f ) * 0.25f );
            movement.setVelocityY( velocityY );
        } else {
            movement.setVelocityY( 0f );
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
        
        maxVelocity = attributes.getValue( MAX_VELOCITY, maxVelocity );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( MAX_VELOCITY, maxVelocity );
    }

}
