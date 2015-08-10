package com.inari.firefly.asset;

import com.inari.firefly.renderer.sprite.TextureAsset;
import com.inari.firefly.system.FFContext;

public class SpriteAssetBatch {
    
    private final AssetSystem assetSystem;
    private final TextureAsset textureAsset;
    
    public SpriteAssetBatch( FFContext context, String textureAssetName ) {
        assetSystem = context.getComponent( FFContext.Systems.ASSET_SYSTEM );
        textureAsset = assetSystem.
    }

}
