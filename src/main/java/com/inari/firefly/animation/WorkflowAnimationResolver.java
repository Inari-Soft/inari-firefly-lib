package com.inari.firefly.animation;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.animation.AnimationEvent.Type;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.state.StateSystem;
import com.inari.firefly.state.WorkflowEvent;
import com.inari.firefly.state.WorkflowEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.NameMapping;

public final class WorkflowAnimationResolver extends AnimationResolver implements WorkflowEventListener {
    
    public static final AttributeKey<Integer> WORKFLOW_ID = new AttributeKey<Integer>( "workflowId", Integer.class, WorkflowAnimationResolver.class );
    public static final AttributeKey<DynArray<NameMapping>> STATE_ANIMATION_NAME_MAPPING = AttributeKey.createForDynArray( "stateAnimationNameMapping", WorkflowAnimationResolver.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        WORKFLOW_ID,
        STATE_ANIMATION_NAME_MAPPING,
    };
    
    private FFContext context;
    
    private int workflowId;
    private DynArray<NameMapping> stateAnimationNameMapping;
    
    private int animationId;

    protected WorkflowAnimationResolver( int id, FFContext context ) {
        super( id );
        
        this.context = context;
        context.registerListener( WorkflowEvent.class, this );
        animationId = -1;
    }

    public final int getWorkflowId() {
        return workflowId;
    }

    public final void setWorkflowId( int workflowId ) {
        this.workflowId = workflowId;
    }

    public final DynArray<NameMapping> getStateAnimationNameMapping() {
        return stateAnimationNameMapping;
    }

    public final void setStateAnimationNameMapping( DynArray<NameMapping> stateAnimationNameMapping ) {
        this.stateAnimationNameMapping = stateAnimationNameMapping;
    }

    public final int getMappedAnimationId( String stateName ) {
        if ( stateName == null ) {
            return -1;
        }
        
        for ( NameMapping nameMapping : stateAnimationNameMapping ) {
            if ( stateName.equals( nameMapping.name1 ) ) {
                return context.getSystem( AnimationSystem.SYSTEM_KEY ).getAnimationId( nameMapping.name2 );
            }
        }
        
        return -1;
    }
    
    @Override
    public final void onEvent( WorkflowEvent event ) {
        if ( workflowId != event.workflowId || event.type != WorkflowEvent.Type.STATE_CHANGED  ) {
            return;
        }
        
        int oldAnimationId = animationId;
        animationId = getMappedAnimationId( event.targetStateName );
        if ( oldAnimationId == animationId ) {
            return;
        } 
        
        context.notify( new AnimationEvent( Type.STOP_ANIMATION, oldAnimationId ) );
        context.notify( new AnimationEvent( Type.START_ANIMATION, animationId ) );
    }

    @Override
    public final int getAnimationId() {
        if ( animationId < 0 ) {
            String currentState = context.getSystem( StateSystem.SYSTEM_KEY ).getCurrentState( workflowId );
            animationId = getMappedAnimationId( currentState );
            context.notify( new AnimationEvent( Type.START_ANIMATION, animationId ) );
        }
        return animationId;
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        workflowId = attributes.getValue( WORKFLOW_ID, workflowId );
        stateAnimationNameMapping = attributes.getValue( STATE_ANIMATION_NAME_MAPPING, stateAnimationNameMapping );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( WORKFLOW_ID, workflowId );
        attributes.put( STATE_ANIMATION_NAME_MAPPING, stateAnimationNameMapping );
    }

    @Override
    public final void dispose() {
        context.disposeListener( WorkflowEvent.class, this );
        super.dispose();
    }

}