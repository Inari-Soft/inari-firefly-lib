package com.inari.firefly.asset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inari.firefly.Disposable;
import com.inari.firefly.FFInitException;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.animation.AnimationSystem.AnimationBuilder;
import com.inari.firefly.animation.WorkflowAnimationResolver;
import com.inari.firefly.animation.timeline.IntTimelineAnimation;
import com.inari.firefly.animation.timeline.IntTimelineData;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.control.ControllerSystem;
import com.inari.firefly.controller.entity.SpriteIdAnimationController;
import com.inari.firefly.entity.EntityAttributeController;
import com.inari.firefly.state.StateSystem;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.external.FFGraphics;

public class AnimatedSpriteAsset extends Asset {
    
    public static final AttributeKey<Integer> TEXTURE_ASSET_ID = new AttributeKey<Integer>( "textureAssetId", Integer.class, AnimatedSpriteAsset.class );
    public static final AttributeKey<Float> UPDATE_RESOLUTION  = new AttributeKey<Float>( "updateResolution", Float.class, AnimatedSpriteAsset.class );
    public static final AttributeKey<Boolean> LOOPING  = new AttributeKey<Boolean>( "looping", Boolean.class, AnimatedSpriteAsset.class );
    public static final AttributeKey<Integer> WORKFLOW_ID  = new AttributeKey<Integer>( "workflowId", Integer.class, AnimatedSpriteAsset.class );
    public static final AttributeKey<AnimatedSpriteData[]> ANIMATED_SPRITE_DATA  = new AttributeKey<AnimatedSpriteData[]>( "animatedSpriteData", AnimatedSpriteData[].class, AnimatedSpriteAsset.class );
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
    
    private FFContext context;
    
    private int textureAssetId;
    private float updateResolution;
    public boolean looping;
    private int workflowId;
    private AnimatedSpriteData[] animatedSpriteData;
    
    private int controllerId;

    protected AnimatedSpriteAsset( int assetIntId, FFContext context ) {
        super( assetIntId );
        this.context = context;
        
        textureAssetId = -1;
        updateResolution = -1;
        looping = true;
        workflowId = -1;
        animatedSpriteData = null;
        controllerId = -1;
        controllerId = -1;
    }

    public final int getAnimationControllerId() {
        return controllerId;
    }
    
    @Override
    public final int getInstanceId( int index ) {
        return getAnimationControllerId();
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( ATTRIBUTE_KEYS );
        return super.attributeKeys( attributeKeys );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        checkNotAlreadyLoaded();
        super.fromAttributes( attributes );
        
        textureAssetId = attributes.getValue( TEXTURE_ASSET_ID, textureAssetId );
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
    
    @Override
    public final Disposable load( FFContext context ) {
        if ( loaded ) {
            return this;
        }
        
        checkConsistency();
        int textureId = context.getSystem( AssetSystem.SYSTEM_KEY ).getAssetInstaceId( textureAssetId );
        Map<String, List<IntTimelineData>> spriteMapping = createSpriteMapping( textureId );
        
        AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        ControllerSystem controllerSystem = context.getSystem( ControllerSystem.SYSTEM_KEY );
        AnimationBuilder animationBuilder = animationSystem.getAnimationBuilder();
        
        String[][] stateAnimationNameMapping = new String[ spriteMapping.size() ][ 2 ];
        int index = 0;
        for ( String stateName : spriteMapping.keySet() ) {
            List<IntTimelineData> timeLineDataList = spriteMapping.get( stateName );
            String animationName = getName() + ANIMATION_NAME_PREFIX + stateName;
            animationBuilder
                .set( IntTimelineAnimation.NAME, animationName )
                .set( IntTimelineAnimation.LOOPING, looping )
                .set( IntTimelineAnimation.TIMELINE, timeLineDataList.toArray( new IntTimelineData[ timeLineDataList.size() ] ) )
            .activate( IntTimelineAnimation.class );
            
            stateAnimationNameMapping[ index ][ 0 ] = stateName;
            stateAnimationNameMapping[ index ][ 1 ] = animationName;
            index++;
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
            .set( EntityAttributeController.NAME, getName() + ANIMATION_CONTROLLER_NAME )
            .set( EntityAttributeController.ANIMATION_ID, animationSystem.getAnimationId( stateAnimationNameMapping[ 0 ][ 1 ] ) )
            .set( EntityAttributeController.ANIMATION_RESOLVER_ID, animationResolverId )
            .set( EntityAttributeController.UPDATE_RESOLUTION, updateResolution )
        .build( getControllerType() );
        
        return this;
    }
    
    protected Class<? extends EntityAttributeController> getControllerType() {
        return SpriteIdAnimationController.class;
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

            String[][] stateAnimationMapping = resolver.getStateAnimationNameMapping();
            animationSystem.deleteAnimationResolver( resolver.getId() );
            
            for ( int i = 0; i < stateAnimationMapping.length; i++ ) {
                deleteAnimation( stateAnimationMapping[ i ][ 1 ] );
            }
        } else {
            deleteAnimation( getName() + ANIMATION_NAME_PREFIX );
        }
        
        controllerId = -1;
    }

    private void deleteAnimation( String animationName ) {
        FFGraphics graphics = context.getGraphics();
        AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        
        IntTimelineAnimation animation = animationSystem.getAnimationAs( animationName, IntTimelineAnimation.class );
        IntTimelineData[] timeline = animation.getTimeline();
        animationSystem.deleteAnimation( animation.getId() );
        
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
    
    private Map<String, List<IntTimelineData>> createSpriteMapping( int textureId ) {
        Map<String, List<IntTimelineData>> mapping = new HashMap<String, List<IntTimelineData>>();
        
        FFGraphics graphics = context.getGraphics();
        for ( int i = 0; i < animatedSpriteData.length; i++ ) {
            String stateName = animatedSpriteData[ i ].stateName;
            if ( stateName == null ) {
                stateName = "";
            }
            List<IntTimelineData> timelineData = mapping.get( stateName );
            if ( timelineData == null ) {
                timelineData = new ArrayList<IntTimelineData>();
                mapping.put( stateName, timelineData );
            }
            
            int spriteId = graphics.createSprite( textureId, animatedSpriteData[ i ].textureRegion );
            timelineData.add( new IntTimelineData( spriteId, animatedSpriteData[ i ].frameTime )  );
        }
        
        if ( mapping.size() <= 0 ) {
            throw new FFInitException( "No AnimatedSpriteData mapping" );
        }
        if ( workflowId < 0 && mapping.size() > 1 ) {
            throw new FFInitException( "AnimatedSpriteData mapping missmatch. Not workflow based but states defined within AnimatedSpriteData" );
        }

        return mapping;
    }

}
