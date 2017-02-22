package com.inari.firefly.animation;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.control.state.StateSystem;
import com.inari.firefly.control.state.WorkflowEvent;
import com.inari.firefly.control.state.WorkflowEventListener;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.animation.AnimationSystemEvent;
import com.inari.firefly.physics.animation.AnimationSystemEvent.Type;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.NameMapping;

public final class WorkflowAnimationController implements WorkflowEventListener {
    
    private FFContext context;
    
    private int workflowId;
    private DynArray<NameMapping> stateAnimationNameMapping;
    
    private int animationId;

    public WorkflowAnimationController( int workflowId, DynArray<NameMapping> stateAnimationNameMapping ) {
        super();
        this.workflowId = workflowId;
        this.stateAnimationNameMapping = stateAnimationNameMapping;
    }

    public final void init( FFContext context ) {
        this.context = context;
        
        context.registerListener( WorkflowEvent.TYPE_KEY, this );
    }
    
    public final void dispose( FFContext context ) {
        context.disposeListener( WorkflowEvent.TYPE_KEY, this );
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
        
        context.notify( new AnimationSystemEvent( Type.STOP_ANIMATION, oldAnimationId ) );
        context.notify( new AnimationSystemEvent( Type.START_ANIMATION, animationId ) );
    }

    public final int getAnimationId() {
        if ( animationId < 0 ) {
            String currentState = context.getSystem( StateSystem.SYSTEM_KEY ).getCurrentState( workflowId );
            animationId = getMappedAnimationId( currentState );
            context.notify( new AnimationSystemEvent( Type.START_ANIMATION, animationId ) );
        }
        return animationId;
    }
}
