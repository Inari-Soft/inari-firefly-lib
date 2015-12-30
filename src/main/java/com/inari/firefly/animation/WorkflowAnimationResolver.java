package com.inari.firefly.animation;

import java.util.Arrays;
import java.util.Set;

import com.inari.firefly.animation.event.AnimationEvent;
import com.inari.firefly.animation.event.AnimationEvent.Type;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.state.StateSystem;
import com.inari.firefly.state.event.WorkflowEvent;
import com.inari.firefly.state.event.WorkflowEventListener;
import com.inari.firefly.system.FFContext;

public final class WorkflowAnimationResolver extends AnimationResolver implements WorkflowEventListener {
    
    public static final AttributeKey<Integer> WORKFLOW_ID = new AttributeKey<Integer>( "workflowId", Integer.class, WorkflowAnimationResolver.class );
    public static final AttributeKey<int[][]> STATE_ANIMATION_MAPPING = new AttributeKey<int[][]>( "stateAnimationMapping", int[][].class, WorkflowAnimationResolver.class );
    public static final AttributeKey<String[][]> STATE_ANIMATION_NAME_MAPPING = new AttributeKey<String[][]>( "stateAnimationNameMapping", String[][].class, WorkflowAnimationResolver.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        WORKFLOW_ID,
        STATE_ANIMATION_MAPPING,
    };
    
    private FFContext context;
    
    private int workflowId;
    private int[][] stateAnimationMapping;
    
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

    public final int[][] getStateAnimationMapping() {
        return stateAnimationMapping;
    }

    public final void setStateAnimationMapping( int[][] stateAnimationMapping ) {
        this.stateAnimationMapping = stateAnimationMapping;
    }

    public final int getMappedAnimationId( int stateId ) {
        int stateIdIndex = getStateIdIndex( stateId );
        if ( stateIdIndex < 0 ) {
            return -1;
        }
        
        return stateAnimationMapping[ stateIdIndex ][ 1 ];
    }
    
    @Override
    public final void onEvent( WorkflowEvent event ) {
        if ( workflowId != event.workflowId ) {
            return;
        }
        
        int oldAnimationId = animationId;
        animationId = getMappedAnimationId( event.targetStateId );
        if ( oldAnimationId == animationId ) {
            return;
        } 
        
        context.notify( new AnimationEvent( Type.STOP_ANIMATION, oldAnimationId ) );
        context.notify( new AnimationEvent( Type.START_ANIMATION, animationId ) );
    }

    @Override
    public final int getAnimationId() {
        if ( animationId < 0 ) {
            animationId = getMappedAnimationId( context.getSystem( StateSystem.SYSTEM_KEY ).getCurrentStateId( workflowId ) );
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
        if ( attributes.contains( STATE_ANIMATION_MAPPING ) ) {
            stateAnimationMapping = attributes.getValue( STATE_ANIMATION_MAPPING, stateAnimationMapping );
        } else {
            StateSystem stateSystem = context.getSystem( StateSystem.SYSTEM_KEY );
            AnimationSystem animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
            String[][] nameMapping = attributes.getValue( STATE_ANIMATION_NAME_MAPPING );
            stateAnimationMapping = new int[ nameMapping.length ][ 2 ];
            for ( int i = 0; i < nameMapping.length; i++ ) {
                stateAnimationMapping[ i ][ 0 ] = stateSystem.getStateId( nameMapping[ i ][ 0 ] );
                stateAnimationMapping[ i ][ 1 ] = animationSystem.getAnimationId( nameMapping[ i ][ 1 ] );
            }
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( WORKFLOW_ID, workflowId );
        attributes.put( STATE_ANIMATION_MAPPING, stateAnimationMapping );
    }

    @Override
    public final void dispose() {
        context.disposeListener( WorkflowEvent.class, this );
        super.dispose();
    }
    
    private int getStateIdIndex( int stateId ) {
        for ( int i = 0; i < stateAnimationMapping.length; i++ ) {
            if ( stateId == stateAnimationMapping[ i ][ 0 ] ) {
                return i;
            }
        }
        
        return -1;
    }

}
