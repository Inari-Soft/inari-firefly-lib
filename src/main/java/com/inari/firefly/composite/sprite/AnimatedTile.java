package com.inari.firefly.composite.sprite;

import com.inari.firefly.controller.entity.TileSpriteAnimationController;
import com.inari.firefly.entity.EntityAttributeController;
import com.inari.firefly.system.FFContext;

public final class AnimatedTile extends AnimatedSprite {

    protected AnimatedTile( int assetIntId, FFContext context ) {
        super( assetIntId, context );
    }
    
    protected Class<? extends EntityAttributeController> getControllerType() {
        return TileSpriteAnimationController.class;
    }

}
