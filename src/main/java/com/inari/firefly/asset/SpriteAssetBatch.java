package com.inari.firefly.asset;

import java.util.ArrayList;
import java.util.Collection;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.ImmutablePair;
import com.inari.firefly.asset.AssetSystem.AssetBuilder;
import com.inari.firefly.renderer.TextureAsset;
import com.inari.firefly.renderer.sprite.SpriteAsset;
import com.inari.firefly.system.FFContext;

public class SpriteAssetBatch extends AssetBatch {
    
    private final TextureAsset textureAsset;

    public SpriteAssetBatch( 
        FFContext context, 
        AssetNameKey textureAssetKey, 
        Rectangle startClip, 
        int hNum, int vNum
    ) {
        super( context );
        
        AssetTypeKey typeKey = assetSystem.getAssetTypeKey( textureAssetKey );
        if ( typeKey == null || typeKey.type != TextureAsset.class ) {
            throw new IllegalArgumentException( "No texture Asset: " + textureAssetKey + " is registered within AssetSystem" );
        }
        
        this.textureAsset = assetSystem.getAsset( textureAssetKey, TextureAsset.class );
        createSprites( startClip, hNum, vNum, textureAssetKey.group, textureAssetKey.name );
    }
    
    public final Collection<ImmutablePair<AssetNameKey, AssetTypeKey>> getAssetKeys() {
        Collection<ImmutablePair<AssetNameKey, AssetTypeKey>> result = new ArrayList<ImmutablePair<AssetNameKey, AssetTypeKey>>( assets.size() );
        for ( AssetNameKey assetNameKey : assets ) {
            AssetTypeKey assetTypeKey = assetSystem.getAssetTypeKey( assetNameKey );
            result.add( new ImmutablePair<AssetNameKey, AssetTypeKey>( assetNameKey, assetTypeKey ) );
        }
        return result;
    }
    
    
    private  void createSprites( 
        Rectangle startClip, 
        int hNum, int vNum, 
        String group, String namePrefix 
    ) {
        checkBounds( startClip, hNum, vNum );
        AssetBuilder<SpriteAsset> spriteAssetBuilder = assetSystem.getAssetBuilder( SpriteAsset.class );
        spriteAssetBuilder.set( SpriteAsset.TEXTURE_ID, textureAsset.typeKey.id );
        
        for ( int y = 0; ( vNum < 0 )? y > vNum: y < vNum;  y = ( vNum < 0 )? --y : ++y ) {
            for ( int x = 0; ( hNum < 0 )? x > hNum: x < hNum;  x = ( hNum < 0 )? --x : ++x ) {
                Rectangle clip = new Rectangle( startClip );
                clip.x = clip.x + ( x * clip.width );
                clip.y = clip.y + ( y * clip.height );
                
                AssetNameKey nameKey = new AssetNameKey( group, namePrefix + "_" + x + "_" + y );
                spriteAssetBuilder
                    .set( SpriteAsset.TEXTURE_REGION, clip )
                    .set( SpriteAsset.ASSET_GROUP, nameKey.group )
                    .set( SpriteAsset.NAME, nameKey.name );
                
                add( nameKey );
            }
        }
     }

    private void checkBounds( Rectangle startClip, int hNum, int vNum ) {
        Rectangle textureArea = new Rectangle( 0, 0, textureAsset.getWidth(), textureAsset.getHeight() );
        Rectangle endClip = new Rectangle( startClip );
        endClip.x = endClip.x + ( ( vNum - 1 ) * startClip.width );
        endClip.y = endClip.y + ( ( hNum - 1 ) * startClip.height );
        
        if ( !GeomUtils.contains( textureArea, startClip ) || !GeomUtils.contains( textureArea, endClip ) ) {
            throw new IllegalArgumentException( "The texture area: " + textureArea + " do not conatins start/endClip: " + startClip + "/" + endClip );
        }
    }

}
