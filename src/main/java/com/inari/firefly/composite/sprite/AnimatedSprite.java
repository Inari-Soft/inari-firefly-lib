package com.inari.firefly.composite.sprite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.FFInitException;
import com.inari.firefly.animation.WorkflowAnimationResolver;
import com.inari.firefly.animation.timeline.IntTimelineAnimation;
import com.inari.firefly.animation.timeline.IntTimelineData;
import com.inari.firefly.asset.Asset;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.control.AnimatedEntityAttribute;
import com.inari.firefly.control.ControllerSystem;
import com.inari.firefly.control.state.StateSystem;
import com.inari.firefly.controller.entity.SpriteIdAnimationController;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.animation.AnimationSystem.AnimationBuilder;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.NameMapping;
import com.inari.firefly.system.external.FFGraphics;
import com.inari.firefly.system.external.SpriteData;
import com.inari.firefly.system.utils.Disposable;

public class AnimatedSprite extends Asset {
    
    public static final AttributeKey<String> TEXTURE_ASSET_NAME = new AttributeKey<String>( "textureAssetName", String.class, AnimatedSprite.class );
    public static final AttributeKey<Integer> TEXTURE_ASSET_ID = new AttributeKey<Integer>( "textureAssetId", Integer.class, AnimatedSprite.class );
    public static final AttributeKey<Float> UPDATE_RESOLUTION  = new AttributeKey<Float>( "updateResolution", Float.class, AnimatedSprite.class );
    public static final AttributeKey<Boolean> LOOPING  = new AttributeKey<Boolean>( "looping", Boolean.class, AnimatedSprite.class );
    public static final AttributeKey<Integer> WORKFLOW_ID  = new AttributeKey<Integer>( "workflowId", Integer.class, AnimatedSprite.class );
    public static final AttributeKey<DynArray<AnimatedSpriteData>> ANIMATED_SPRITE_DATA  = AttributeKey.createDynArray( "animatedSpriteData", AnimatedSprite.class );
    private static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = new HashSet<AttributeKey<?>>( Arrays.<AttributeKey<?>>asList( new AttributeKey[] { 
        TEXTURE_ASSET_ID,
        UPDATE_RESOLUTION,
        LOOPING,
        WORKFLOW_ID,
        ANIMATED_SPRITE_DATA
    } ) );
    
    private static final String ANIMATION_NAME_PREFIX = "_ANIMATION_";
    private static final String ANIMATION_RESOLVER_NAME = ANIMATION_NAME_PREFIX + "RESOLVER";
    private static final String ANIMATION_CONTROLLER_NAME = ANIMATION_NAME_PREFIX + "CONTROLLER";
    
    private int textureAssetId;
    private float updateResolution;
    public boolean looping;
    private int workflowId;
    private DynArray<AnimatedSpriteData> animatedSpriteData;
    
    private int controllerId;
    private final IntBag dependsOn;
    private final InternalSpriteData spriteData = new InternalSpriteData();

    protected AnimatedSprite( int assetIntId ) {
        super( assetIntId );
        
        textureAssetId = -1;
        updateResolution = -1;
        looping = true;
        workflowId = -1;
        animatedSpriteData = null;
        controllerId = -1;
        controllerId = -1;
        dependsOn = new IntBag( 1 );
    }
    
    @Override
    public int getInstanceId( int index ) {
        return controllerId;
    }

    public final int getAnimationControllerId() {
        return controllerId;
    }
    
    protected IntBag dependsOn() {
        return dependsOn;
    }
    
    @Override
    public final Disposable load( FFContext context ) {
        if ( loaded ) {
            return this;
        }
        
        checkConsistency();
        int textureId = context.getSystem( AssetSystem.SYSTEM_KEY ).getAssetInstanceId( textureAssetId );
        Map<String, DynArray<IntTimelineData>> spriteMapping = createSpriteMapping( textureId );
        
        AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        ControllerSystem controllerSystem = context.getSystem( ControllerSystem.SYSTEM_KEY );
        AnimationBuilder animationBuilder = animationSystem.getAnimationBuilder();
        
        DynArray<NameMapping> stateAnimationNameMapping = new DynArray<NameMapping>( spriteMapping.size(), 2 );
        for ( String stateName : spriteMapping.keySet() ) {
            DynArray<IntTimelineData> timeLineDataList = spriteMapping.get( stateName );
            String animationName = getName() + ANIMATION_NAME_PREFIX + stateName;
            animationBuilder
                .set( IntTimelineAnimation.NAME, animationName )
                .set( IntTimelineAnimation.LOOPING, looping )
                .set( IntTimelineAnimation.TIMELINE, timeLineDataList )
            .activate( IntTimelineAnimation.class );
            
            stateAnimationNameMapping.add( new NameMapping( stateName, animationName ) );
        }
        
        int animationResolverId = -1;
        if ( workflowId >= 0 ) {
            animationResolverId = animationSystem.getAnimationResolverBuilder()
                .set( WorkflowAnimationResolver.NAME, getName() + ANIMATION_RESOLVER_NAME )
                .set( WorkflowAnimationResolver.WORKFLOW_ID, workflowId )
                .set( WorkflowAnimationResolver.STATE_ANIMATION_NAME_MAPPING, stateAnimationNameMapping )
            .build( WorkflowAnimationResolver.class );
        }
        
        controllerId = controllerSystem.getControllerBuilder()
            .set( AnimatedEntityAttribute.NAME, getName() + ANIMATION_CONTROLLER_NAME )
            .set( AnimatedEntityAttribute.ANIMATION_ID, animationSystem.getAnimationId( stateAnimationNameMapping.iterator().next().name2 ) )
            .set( AnimatedEntityAttribute.ANIMATION_RESOLVER_ID, animationResolverId )
            .set( AnimatedEntityAttribute.UPDATE_RESOLUTION, updateResolution )
        .build( getControllerType() );
        
        loaded = true;
        
        return this;
    }
    
    @Override
    public final void dispose( FFContext context ) {
        if ( !loaded ) {
            return;
        }
        
        AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        ControllerSystem controllerSystem = context.getSystem( ControllerSystem.SYSTEM_KEY );
        
        controllerSystem.deleteController( controllerId );
        if ( workflowId >= 0 ) {
            WorkflowAnimationResolver resolver = animationSystem.getAnimationResolverAs( 
                animationSystem.getAnimationResolverId( getName() + ANIMATION_RESOLVER_NAME ), 
                WorkflowAnimationResolver.class
            );

            DynArray<NameMapping> stateAnimationMapping = resolver.getStateAnimationNameMapping();
            animationSystem.deleteAnimationResolver( resolver.index() );
            
            for ( NameMapping nameMapping : stateAnimationMapping ) {
                deleteAnimation( nameMapping.name2 );
            }
        } else {
            deleteAnimation( getName() + ANIMATION_NAME_PREFIX );
        }
        
        controllerId = -1;
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( ATTRIBUTE_KEYS );
        attributeKeys.addAll( super.attributeKeys() );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        if ( loaded ) {
            throw new IllegalStateException( "The AnimatedSprite is already loaded" );
        }
        
        super.fromAttributes( attributes );
        
        textureAssetId = attributes.getIdForName( TEXTURE_ASSET_NAME, TEXTURE_ASSET_ID, Asset.TYPE_KEY, textureAssetId );
        dependsOn.add( textureAssetId );
        updateResolution = attributes.getValue( UPDATE_RESOLUTION, updateResolution );
        looping = attributes.getValue( LOOPING, looping );
        workflowId = attributes.getValue( WORKFLOW_ID, workflowId );
        animatedSpriteData = attributes.getValue( ANIMATED_SPRITE_DATA, animatedSpriteData );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TEXTURE_ASSET_ID, textureAssetId );
        attributes.put( UPDATE_RESOLUTION, updateResolution );
        attributes.put( LOOPING, looping );
        attributes.put( WORKFLOW_ID, workflowId );
        attributes.put( ANIMATED_SPRITE_DATA, animatedSpriteData );
    }

    private void deleteAnimation( String animationName ) {
        FFGraphics graphics = context.getGraphics();
        AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        
        IntTimelineAnimation animation = animationSystem.getAnimationAs( animationName, IntTimelineAnimation.class );
        IntTimelineData[] timeline = animation.getTimeline();
        animationSystem.deleteAnimation( animation.index() );
        
        for ( IntTimelineData timlineData : timeline ) {
            graphics.disposeSprite( timlineData.getValue() );
        }
    }

    private void checkConsistency() {
        if ( !context.getSystem( AssetSystem.SYSTEM_KEY ).isLoaded( textureAssetId ) ) {
            throw new FFInitException( "The TextureAsset with id: " + textureAssetId + " is not loaded yet" );
        }
        if ( workflowId >= 0 && !context.getSystem( StateSystem.SYSTEM_KEY ).hasWorkflow( workflowId ) ) {
            throw new FFInitException( "The Workflow with id: " + workflowId + " does not exist" );
        }
    }
    
    private Map<String, DynArray<IntTimelineData>> createSpriteMapping( int textureId ) {
        spriteData.textureId = textureId;
        Map<String, DynArray<IntTimelineData>> mapping = new HashMap<String, DynArray<IntTimelineData>>();
        
        FFGraphics graphics = context.getGraphics();
        for ( int i = 0; i < animatedSpriteData.capacity(); i++ ) {
            if ( !animatedSpriteData.contains( i ) ) {
                continue;
            }
            
            AnimatedSpriteData asd = animatedSpriteData.get( i );
            String stateName = asd.stateName;
            if ( stateName == null ) {
                stateName = "";
            }
            DynArray<IntTimelineData> timelineData = mapping.get( stateName );
            if ( timelineData == null ) {
                timelineData = new DynArray<IntTimelineData>();
                mapping.put( stateName, timelineData );
            }
            
            spriteData.region = asd.textureRegion;
            int spriteId = graphics.createSprite( spriteData );
            timelineData.add( new IntTimelineData( spriteId, asd.frameTime )  );
        }
        
        if ( mapping.size() <= 0 ) {
            throw new FFInitException( "No AnimatedSpriteData mapping" );
        }
        if ( workflowId < 0 && mapping.size() > 1 ) {
            throw new FFInitException( "AnimatedSpriteData mapping missmatch. Not workflow based but states defined within AnimatedSpriteData" );
        }

        return mapping;
    }
    
    protected Class<? extends AnimatedEntityAttribute> getControllerType() {
        return SpriteIdAnimationController.class;
    }
    
    private final class InternalSpriteData implements SpriteData {
        
        int textureId;
        Rectangle region;

        @Override
        public final int getTextureId() {
            return textureId;
        }

        @Override
        public final Rectangle getTextureRegion() {
            return region;
        }

        @Override
        public final <A> A getDynamicAttribute( AttributeKey<A> key ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public final boolean isHorizontalFlip() {
            return false;
        }

        @Override
        public final boolean isVerticalFlip() {
            return false;
        }
    }

}
