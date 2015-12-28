package com.inari.firefly.controller.entity;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.entity.EntityAttributeController;
import com.inari.firefly.graphics.sprite.ESprite;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.external.FFTimer;

public final class SpriteTintColorAnimationController extends EntityAttributeController {
    
    SpriteTintColorAnimationController( int id, FFContext context ) {
        super( id, context );
    }
    
    @Override
    public final AttributeKey<?> getControlledAttribute() {
        return ESprite.TINT_COLOR;
    }

    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ESprite sprite = context.getEntityComponent( entityId, ESprite.TYPE_KEY );
        sprite.setTintColor( animationSystem.getValue( animationId, entityId, sprite.getTintColor() ) );
    }

}
