package com.inari.firefly.controller.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.renderer.sprite.ESprite;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class SpriteTintColorAnimationController extends EntityController {
    
    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        ESprite.TINT_COLOR
    };
    
    public static final AttributeKey<Integer> TINT_COLOR_ANIMATION_ID = new AttributeKey<Integer>( "tintColorAnimationId", Integer.class, SpriteTintColorAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        TINT_COLOR_ANIMATION_ID
    };
    
    private int tintColorAnimationId;
    
    SpriteTintColorAnimationController( int id, FFContext context ) {
        super( id, context );
        
        tintColorAnimationId = -1;
    }

    public final int getTintColorAnimationId() {
        return tintColorAnimationId;
    }

    public final void setTintColorAnimationId( int tintColorAnimationId ) {
        this.tintColorAnimationId = tintColorAnimationId;
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
        
        tintColorAnimationId = attributes.getValue( TINT_COLOR_ANIMATION_ID, tintColorAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TINT_COLOR_ANIMATION_ID, tintColorAnimationId );
    } 

    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ESprite sprite = entitySystem.getComponent( entityId, ESprite.TYPE_KEY );

        if ( tintColorAnimationId >= 0 && animationSystem.exists( tintColorAnimationId ) ) {
            sprite.setTintColor( animationSystem.getValue( tintColorAnimationId, entityId, sprite.getTintColor() ) );
        } else {
            tintColorAnimationId = -1;
        }
    }
}
