package com.inari.firefly.animation.sprite;

import java.util.ArrayList;
import java.util.List;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.Disposable;
import com.inari.firefly.FFInitException;
import com.inari.firefly.Loadable;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.control.Controller;
import com.inari.firefly.control.ControllerSystem;
import com.inari.firefly.controller.entity.SpriteIdAnimationController;
import com.inari.firefly.renderer.sprite.SpriteAsset;
import com.inari.firefly.system.FFContext;

public final class SpriteAnimationBuilder {
    
    private final AssetSystem assetSystem;
    private final ControllerSystem controllerSystem;
    private final AnimationSystem animationSystem;
    
    private String textureAssetName;
    private String namePrefix;
    private AnimationData singleData;
    private DynArray<AnimationData> statedData;
    private Class<? extends StatedSpriteAnimation> statedAnimationType;
    private boolean looping;
    private float updateResolution = -1;
    private long animationStartTime = 0;
    
    private AnimationData currentData;

    
    public SpriteAnimationBuilder( FFContext context ) {
        assetSystem = context.getSystem( AssetSystem.SYSTEM_KEY );
        controllerSystem = context.getSystem( ControllerSystem.SYSTEM_KEY );
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        
        reset();
    }

    public final SpriteAnimationBuilder reset() {
        singleData = new AnimationData( -1 );
        currentData = singleData;
        namePrefix = null;
        statedData = null;
        looping  = false;
        return this;
    }

    public final SpriteAnimationBuilder setTextureAssetName( String textureAssetName ) {
        this.textureAssetName = textureAssetName;
        return this;
    }
    
    public final SpriteAnimationBuilder setNamePrefix( String namePrefix ) {
        this.namePrefix = namePrefix;
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
        if ( textureAssetName == null ) {
            throw new FFInitException( "The textureAssetKey has to be set first" );
        }
        
        currentData.values.add( new AnimationValue( time, textureRegion ) );
        return this;
    }
    
    public final SpriteAnimationBuilder addSpritesToAnimation( long time, Rectangle textureRegion, int number, boolean horizontal ) {
        if ( textureAssetName == null ) {
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
        
        private final int textureId;
        private final String assetNamePrefix;
        private final boolean isLooping;
        private final float resolution; 
        private final long startTime;
        private final List<AnimationData> data;
        private final Class<? extends StatedSpriteAnimation> statedType;
        
        private int controllerId;
        private int animationId;
        
        SpriteAnimationHandler() {
            textureId = assetSystem.getAssetId( textureAssetName );
            if ( textureId < 0 ) {
                throw new FFInitException( "No loaded Texture found for name: " + textureAssetName );
            }
            
            assetNamePrefix = ( namePrefix == null )? textureAssetName : namePrefix;
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
                
                animationId = animationSystem.getAnimationBuilder()
                    .set( SpriteAnimation.NAME, assetNamePrefix )
                    .set( SpriteAnimation.LOOPING, isLooping )
                    .set( SpriteAnimation.START_TIME, startTime )
                .build( statedType );
                StatedSpriteAnimation animation = animationSystem.getAnimationAs( animationId, StatedSpriteAnimation.class );
                
                controllerId = controllerSystem.getControllerBuilder()
                    .set( SpriteIdAnimationController.NAME, assetNamePrefix )
                    .set( SpriteIdAnimationController.SPRITE_ID_ANIMATION_ID, animationId )
                    .set( SpriteIdAnimationController.UPDATE_RESOLUTION, resolution )
                .build( SpriteIdAnimationController.class );
                
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
                
                animationId = animationSystem.getAnimationBuilder()
                    .set( SpriteAnimation.NAME, assetNamePrefix )
                    .set( SpriteAnimation.SPRITE_ANIMATION_TIMELINE, animTimeline )
                    .set( SpriteAnimation.LOOPING, isLooping )
                    .set( SpriteAnimation.START_TIME, startTime )
                .build( SpriteAnimation.class );
                
                controllerId = controllerSystem.getControllerBuilder()
                    .set( SpriteIdAnimationController.NAME, assetNamePrefix )
                    .set( SpriteIdAnimationController.SPRITE_ID_ANIMATION_ID, animationId )
                    .set( SpriteIdAnimationController.UPDATE_RESOLUTION, resolution )
                .build( SpriteIdAnimationController.class );
            }
        }

        private int createTimeline( int count, AnimationData animData, SpriteAnimationTimeline animTimeline ) {
            for ( AnimationValue value : animData.values ) {
                int spriteAssetId = assetSystem.getAssetBuilder()
                    .set( SpriteAsset.NAME, assetNamePrefix + "_" + count )
                    .set( SpriteAsset.TEXTURE_REGION, value.textureRegion )
                    .set( SpriteAsset.TEXTURE_ASSET_ID, textureId )
                .activate( SpriteAsset.class );
                SpriteAsset spriteAsset = assetSystem.getAssetAs( spriteAssetId, SpriteAsset.class );
                
                value.assetId = spriteAsset.getId();
                value.spriteId = spriteAsset.getSpriteId();
                animTimeline.add( spriteAsset.getSpriteId(), value.time );
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
        
        @Override
        public Disposable load( FFContext context ) {
            return this;
        }

        @Override
        public final void dispose( FFContext context ) {
            animationSystem.deleteAnimation( animationId );
            controllerSystem.deleteController( controllerId );
            
            for ( AnimationData animData : data ) {
                for ( AnimationValue value : animData.values ) {
                    assetSystem.disposeAsset( value.assetId );
                }
            }
        }

        public final IntBag getAllSpriteAssetKeys() {
            IntBag result = new IntBag( data.size(), -1 );
            for ( AnimationData animData : data ) {
                for ( AnimationValue value : animData.values ) {
                    result.add( value.assetId );
                }
            }
            return result;
        }

        public Integer getStartSpriteId() {
            return data.iterator().next().values.iterator().next().spriteId;
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
        
        int assetId;
        int spriteId;
        
        private AnimationValue( long time, Rectangle textureRegion ) {
            super();
            this.time = time;
            this.textureRegion = textureRegion;
        }
    }

}
