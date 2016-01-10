package com.inari.firefly.asset;

import com.inari.firefly.controller.entity.TileSpriteAnimationController;
import com.inari.firefly.entity.EntityAttributeController;
import com.inari.firefly.system.FFContext;

public final class AnimatedTileAsset extends AnimatedSpriteAsset {

    protected AnimatedTileAsset( int assetIntId, FFContext context ) {
        super( assetIntId, context );
    }
    
    protected Class<? extends EntityAttributeController> getControllerType() {
        return TileSpriteAnimationController.class;
    }

}
