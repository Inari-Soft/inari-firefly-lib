package com.inari.firefly.controller.entity;

import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.animation.IntAnimation;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.entity.EntityAttributeController;
import com.inari.firefly.entity.EntityAttributeMap;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.external.FFTimer;

public final class TileSpriteAnimationController extends EntityAttributeController {
    
    private final AnimationSystem animationSystem;

    protected TileSpriteAnimationController( int id, FFContext context ) {
        super( id, context );
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
    protected final void update( final FFTimer timer, int entityId ) {
        ETile tile = context.getEntityComponent( entityId, ETile.TYPE_KEY );
        tile.setSpriteId( animationSystem.getValue( animationId, entityId, tile.getSpriteId() ) );
    }

}
