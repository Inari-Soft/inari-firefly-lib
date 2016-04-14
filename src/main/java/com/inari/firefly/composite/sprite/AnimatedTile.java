package com.inari.firefly.composite.sprite;

import com.inari.firefly.control.AnimatedEntityAttribute;
import com.inari.firefly.controller.entity.TileSpriteAnimationController;

public final class AnimatedTile extends AnimatedSprite {

    protected AnimatedTile( int assetIntId ) {
        super( assetIntId );
    }
    
    protected Class<? extends AnimatedEntityAttribute> getControllerType() {
        return TileSpriteAnimationController.class;
    }

}
