package com.inari.firefly.animation.sprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.Disposable;
import com.inari.firefly.Loadable;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.asset.AssetNameKey;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.asset.AssetTypeKey;
import com.inari.firefly.control.Controller;
import com.inari.firefly.control.ControllerSystem;
import com.inari.firefly.controller.SpriteIdAnimationController;
import com.inari.firefly.renderer.sprite.SpriteAsset;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFInitException;

public final class SpriteAnimationBuilder {
    
    private final AssetSystem assetSystem;
    private final ControllerSystem controllerSystem;
    private final AnimationSystem animationSystem;
    
    private AssetNameKey textureAssetKey;
    private String namePrefix;
    private String group;
    private AnimationData singleData;
    private DynArray<AnimationData> statedData;
    private Class<? extends StatedSpriteAnimation> statedAnimationType;
    private boolean looping;
    private float updateResolution = -1;
    private long animationStartTime = 0;
    
    private AnimationData currentData;

    
    public SpriteAnimationBuilder( FFContext context ) {
        assetSystem = context.getComponent( AssetSystem.CONTEXT_KEY );
        controllerSystem = context.getComponent( ControllerSystem.CONTEXT_KEY );
        animationSystem = context.getComponent( AnimationSystem.CONTEXT_KEY );
        
        reset();
    }

    public final SpriteAnimationBuilder reset() {
        singleData = new AnimationData( -1 );
        currentData = singleData;
        namePrefix = null;
        group = null;
        statedData = null;
        looping  = false;
        return this;
    }

    public final SpriteAnimationBuilder setTextureAssetKey( AssetNameKey textureAssetKey ) {
        this.textureAssetKey = textureAssetKey;
        return this;
    }
    
    public final SpriteAnimationBuilder setNamePrefix( String namePrefix ) {
        this.namePrefix = namePrefix;
        return this;
    }

    public final SpriteAnimationBuilder setGroup( String group ) {
        this.group = group;
        return this;
    }

    public final SpriteAnimationBuilder setLooping( boolean looping ) {
        this.looping = looping;
        return this;
    }

    public final SpriteAnimationBuilder setUpdateResolution( int updateResolution ) {
        this.updateResolution = updateResolution;
        return this;
    }

    public final SpriteAnimationBuilder setAnimationStartTime( long animationStartTime ) {
        this.animationStartTime = animationStartTime;
        return this;
    }

    public final SpriteAnimationBuilder setStatedAnimationType( Class<? extends StatedSpriteAnimation> statedAnimationType ) {
        this.statedAnimationType = statedAnimationType;
        return this;
    }

    public final SpriteAnimationBuilder setState( int stateId ) {
        if ( statedAnimationType == null ) {
            throw new FFInitException( "statedAnimationType must be set" );
        }
        
        if ( statedData == null ) {
            statedData = new DynArray<AnimationData>();
        }
        
        if ( !statedData.contains( stateId ) ) {
            currentData = new AnimationData( stateId );
            statedData.set( stateId, currentData );
        } else {
            currentData = statedData.get( stateId );
        }
        
        return this;
    }
    
    public final SpriteAnimationBuilder addSpriteToAnimation( long time, Rectangle textureRegion ) {
        if ( textureAssetKey == null ) {
            throw new FFInitException( "The textureAssetKey has to be set first" );
        }
        
        currentData.values.add( new AnimationValue( time, textureRegion ) );
        return this;
    }
    
    public final SpriteAnimationBuilder addSpritesToAnimation( long time, Rectangle textureRegion, int number, boolean horizontal ) {
        if ( textureAssetKey == null ) {
            throw new FFInitException( "The textureAssetKey has to be set first" );
        }

        int vNum = ( horizontal )? 1 : number;
        int hNum = ( horizontal )? number : 1;
        addDataToCurrent( time, textureRegion, vNum, hNum );

        return this;
    }
    
    public final SpriteAnimationHandler build() {
        return new SpriteAnimationHandler();
    }

    private void addDataToCurrent( long time, Rectangle textureRegion, int vNum, int hNum ) {
        Rectangle tmp = new Rectangle( textureRegion );
        for ( int y = 0; ( vNum < 0 )? y > vNum: y < vNum;  y = ( vNum < 0 )? --y : ++y ) {
            for ( int x = 0; ( hNum < 0 )? x > hNum: x < hNum;  x = ( hNum < 0 )? --x : ++x ) {
                Rectangle clip = new Rectangle( tmp );
                clip.x = clip.x + ( x * clip.width );
                clip.y = clip.y + ( y * clip.height );
                
                currentData.values.add( new AnimationValue( time, new Rectangle( clip ) ) );
            }
        }
    }
    
    public final class SpriteAnimationHandler implements Loadable, Disposable {
        
        private final AssetTypeKey textureKey;
        private final String assetGroup;
        private final String assetNamePrefix;
        private final boolean isLooping;
        private final float resolution; 
        private final long startTime;
        private final List<AnimationData> data;
        private final Class<? extends StatedSpriteAnimation> statedType;
        
        private int controllerId;
        private int animationId;
        
        SpriteAnimationHandler() {
            textureKey = assetSystem.getAssetTypeKey( textureAssetKey );
            if ( textureKey == null ) {
                throw new FFInitException( "No AssetTypeKey found" );
            }
            
            assetGroup = ( group == null )? textureAssetKey.group : group;
            assetNamePrefix = ( namePrefix == null )? textureAssetKey.name : namePrefix;
            isLooping = looping;
            resolution = updateResolution;
            startTime = animationStartTime;
            data = new ArrayList<AnimationData>();
            if ( statedData != null ) {
                statedType = statedAnimationType;
                for ( AnimationData animData : statedData ) {
                    data.add( new AnimationData( animData ) );
                }
            } else {
                statedType = null;
                data.add( new AnimationData( singleData ) );
            }

            create();
        }
        
        public final int getControllerId() {
            return controllerId;
        }

        public final int getAnimationId() {
            return animationId;
        }
        
        public final void setAnimationResolution( float resolution ) {
            Controller controller = controllerSystem.getController( controllerId );
            controller.setUpdateResolution( resolution );
        }

        private final void create() {
            if ( data.size() > 1 ) {
                
                StatedSpriteAnimation animation = animationSystem.getAnimationBuilder( statedType )
                    .set( SpriteAnimation.NAME, assetNamePrefix )
                    .set( SpriteAnimation.LOOPING, isLooping )
                    .set( SpriteAnimation.START_TIME, startTime )
                .build();
                animationId = animation.getId();
                
                controllerId = controllerSystem.getControllerBuilder( SpriteIdAnimationController.class )
                    .set( SpriteIdAnimationController.NAME, assetNamePrefix )
                    .set( SpriteIdAnimationController.SPRITE_ID_ANIMATION_ID, animationId )
                    .set( SpriteIdAnimationController.UPDATE_RESOLUTION, resolution )
                .build().getId();
                
                int count = 0;
                for ( AnimationData animData : data ) {
                    SpriteAnimationTimeline animTimeline = new SpriteAnimationTimeline();
                    count = createTimeline( count, animData, animTimeline );
                    animation.setSpriteAnimationTimeline( animData.stateId, animTimeline );
                }
                
            } else {
                AnimationData animData = data.iterator().next();
                
                SpriteAnimationTimeline animTimeline = new SpriteAnimationTimeline();
                createTimeline( 0, animData, animTimeline );
                
                animationId = animationSystem.getAnimationBuilder( SpriteAnimation.class )
                    .set( SpriteAnimation.NAME, assetNamePrefix )
                    .set( SpriteAnimation.SPRITE_ANIMATION_TIMELINE, animTimeline )
                    .set( SpriteAnimation.LOOPING, isLooping )
                    .set( SpriteAnimation.START_TIME, startTime )
                .build().getId();
                
                controllerId = controllerSystem.getControllerBuilder( SpriteIdAnimationController.class )
                    .set( SpriteIdAnimationController.NAME, assetNamePrefix )
                    .set( SpriteIdAnimationController.SPRITE_ID_ANIMATION_ID, animationId )
                    .set( SpriteIdAnimationController.UPDATE_RESOLUTION, resolution )
                .build().getId();
            }
        }

        private int createTimeline( int count, AnimationData animData, SpriteAnimationTimeline animTimeline ) {
            for ( AnimationValue value : animData.values ) {
                SpriteAsset spriteAsset = assetSystem.getAssetBuilder( SpriteAsset.class )
                    .set( SpriteAsset.NAME, assetNamePrefix + "_" + count )
                    .set( SpriteAsset.ASSET_GROUP, assetGroup )
                    .set( SpriteAsset.TEXTURE_REGION, value.textureRegion )
                    .set( SpriteAsset.TEXTURE_ID, textureKey.id )
                .build();
                
                value.assetTypeKey = spriteAsset.getTypeKey();
                animTimeline.add( spriteAsset.getId(), value.time );
                count++;
            }
            
            return count;
        }
        
        public final void setFrameTime( int stateId, int timeInMillis ) {
            ( (StatedSpriteAnimation) animationSystem.getAnimation( animationId ) )
                .getSpriteAnimationTimeline( stateId ).setFrameTime( timeInMillis );
        }
        
        public final void setFrameTime( int timeInMillis ) {
            ( (SpriteAnimation) animationSystem.getAnimation( animationId ) )
                .getSpriteAnimationTimeline().setFrameTime( timeInMillis );
        }
        
        public final Collection<AssetTypeKey> getAllSpriteAssetKeys() {
            ArrayList<AssetTypeKey> result = new ArrayList<AssetTypeKey>();
            for ( AnimationData animData : data ) {
                for ( AnimationValue value : animData.values ) {
                    result.add( value.assetTypeKey );
                }
            }
            return result;
        }
        
        @Override
        public Disposable load( FFContext context ) {
            for ( AssetTypeKey key : getAllSpriteAssetKeys() ) {
                assetSystem.loadAsset( key );
            }
            return this;
        }

        @Override
        public final void dispose( FFContext context ) {
            animationSystem.deleteAnimation( animationId );
            controllerSystem.deleteController( controllerId );
            assetSystem.deleteAssets( assetGroup );
        }

    }

    private final class AnimationData {
        
        final int stateId;
        final List<AnimationValue> values;
        
        private AnimationData( int stateId ) {
            this.stateId = stateId;
            values = new ArrayList<AnimationValue>();
        }
        
        private AnimationData( AnimationData data ) {
            this.stateId = data.stateId;
            values = new ArrayList<AnimationValue>( data.values.size() );
            for ( AnimationValue value : data.values ) {
                values.add( new AnimationValue( value.time, value.textureRegion) );
            }
        }
    }
    
    private final class AnimationValue {
        final long time;
        final Rectangle textureRegion;
        
        AssetTypeKey assetTypeKey;
        
        private AnimationValue( long time, Rectangle textureRegion ) {
            super();
            this.time = time;
            this.textureRegion = textureRegion;
        }
    }

}
