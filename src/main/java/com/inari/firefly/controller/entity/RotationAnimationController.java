package com.inari.firefly.controller.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class RotationAnimationController extends EntityController {
    
    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        ETransform.ROTATION
    };
    
    public static final AttributeKey<Integer> ROTATION_ANIMATION_ID = new AttributeKey<Integer>( "rotationAnimationId", Integer.class, RotationAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        ROTATION_ANIMATION_ID
    };

    private int rotationAnimationId;
    
    RotationAnimationController( int id, FFContext context ) {
        super( id, context );
        rotationAnimationId = -1;
    }

    public final int getRotationAnimationId() {
        return rotationAnimationId;
    }

    public final void setRotationAnimationId( int rotationAnimationId ) {
        this.rotationAnimationId = rotationAnimationId;
    }
    
    @Override
    public final AttributeKey<?>[] getControlledAttribute() {
        return CONTROLLED_ATTRIBUTES;
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
        
        rotationAnimationId = attributes.getValue( ROTATION_ANIMATION_ID, rotationAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( ROTATION_ANIMATION_ID, rotationAnimationId );
    }


    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ETransform transform = entitySystem.getComponent( entityId, ETransform.COMPONENT_TYPE );

        if ( rotationAnimationId >= 0 && animationSystem.exists( rotationAnimationId ) ) {
            transform.setRotation( animationSystem.getValue( rotationAnimationId, entityId, transform.getRotation() ) );
        } else {
            rotationAnimationId = -1;
        }
    }

    
}
