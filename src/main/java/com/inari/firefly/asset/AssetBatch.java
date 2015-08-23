package com.inari.firefly.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.inari.commons.lang.functional.Clearable;
import com.inari.commons.lang.functional.Disposable;
import com.inari.commons.lang.functional.Loadable;
import com.inari.firefly.system.FFContext;

public class AssetBatch implements Loadable, Disposable, Clearable {

    protected final AssetSystem assetSystem;
    protected final Collection<AssetNameKey> assets;
    
    private boolean loaded = false;
    
    public AssetBatch( FFContext context ) {
        assetSystem = context.getComponent( FFContext.Systems.ASSET_SYSTEM );
        assets = new ArrayList<AssetNameKey>();
    }
    
    public final void add( AssetNameKey... keys ) {
        if ( keys == null ) {
            return;
        }
        
        for ( AssetNameKey key : keys ) {
            add( key );
        }
    }

    public final void add( AssetNameKey key ) {
        if ( ! assetSystem.hasAsset( key ) ) {
            throw new IllegalArgumentException( "No Asset with AssetNameKey: " + key + " registered on AssetProvider" );
        }
        
        assets.add( key );
    }
    
    public final void remove( AssetNameKey... keys ) {
        if ( keys == null ) {
            return;
        }
        assets.removeAll( Arrays.asList( keys ) );
    }
    
    public final boolean isLoaded() {
        return loaded;
    }
    
    @Override
    public final void dispose() {
        if ( !loaded ) {
            return;
        }
        
        for ( AssetNameKey key : assets ) {
            assetSystem.disposeAsset( key );
        }
        
        loaded = false;
    }

    @Override
    public final Disposable load() {
        if ( loaded ) {
            return this;
        }
        
        for ( AssetNameKey key : assets ) {
            assetSystem.loadAsset( key );
        }
        
        loaded = true;
        return this;
    }
    
    @Override
    public final void clear() {
        if ( loaded ) {
            dispose();
        }
        
        for ( AssetNameKey key : assets ) {
            assetSystem.deleteAsset( key );
        }
        
        assets.clear();
    }
}
