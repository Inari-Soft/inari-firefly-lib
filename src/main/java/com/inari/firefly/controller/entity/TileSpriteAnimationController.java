package com.inari.firefly.controller.entity;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.control.AnimatedEntityAttribute;
import com.inari.firefly.entity.EntityAttributeMap;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.animation.IntAnimation;

public final class TileSpriteAnimationController extends AnimatedEntityAttribute {
    
    private AnimationSystem animationSystem;

    protected TileSpriteAnimationController( int id ) {
        super( id );
    }
    
    @Override
    public final void init() {
        super.init();
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
    }
    
    @Override
    public final AttributeKey<Integer> getControlledAttribute() {
        return ETile.SPRITE_ID;
    }

    @Override
    public final void initEntity( EntityAttributeMap attributes ) {
        animationId = animationSystem.getAnimationId( animationResolverId, animationId );
        IntAnimation animation = animationSystem.getAnimationAs( animationId, IntAnimation.class );
        int value = animation.getInitValue();
        attributes.put( getControlledAttribute(), value );
    }

    @Override
    protected final void update( int entityId ) {
        updateAnimationId( animationSystem );
        ETile tile = context.getEntityComponent( entityId, ETile.TYPE_KEY );
        tile.setSpriteId( animationSystem.getValue( animationId, entityId, tile.getSpriteId() ) );
    }

}
