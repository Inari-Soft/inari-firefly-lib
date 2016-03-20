package com.inari.firefly.composite.sprite;

import com.inari.firefly.controller.entity.TileSpriteAnimationController;
import com.inari.firefly.entity.EntityAttributeAnimationController;

public final class AnimatedTile extends AnimatedSprite {

    protected AnimatedTile( int assetIntId ) {
        super( assetIntId );
    }
    
    protected Class<? extends EntityAttributeAnimationController> getControllerType() {
        return TileSpriteAnimationController.class;
    }

}
