package com.inari.firefly.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inari.commons.lang.IntIterator;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.state.StateSystem;
import com.inari.firefly.state.event.StateChangeEvent;
import com.inari.firefly.state.event.StateChangeListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.FFInitException;
import com.inari.firefly.system.FFTimer;

@Deprecated // create a SpriteAnimation using a SpriteBatch instead
public final class StatedIntTimelineAnimation extends IntAnimation implements StateChangeListener, FFContextInitiable {
    
    public static final AttributeKey<String> TIMELINE_DATA = new AttributeKey<String>( "statedTimelineDataMap", String.class, StatedIntTimelineAnimation.class );
    public static final AttributeKey<Integer> WORKFLOW_ID = new AttributeKey<Integer>( "workflowId", Integer.class, StatedIntTimelineAnimation.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        TIMELINE_DATA,
        WORKFLOW_ID
    };
    
    private int workflowId;
    private final Map<Integer, List<IntTimePair>> statedTimelineDataMap;
    
    private List<IntTimePair> currentTimelineData;
    private IntTimePair currentData;
    private Iterator<IntTimePair> iterator;
    private long lastUpdate;
    
    @Deprecated // create a SpriteAnimation using a SpriteBatch instead
    protected StatedIntTimelineAnimation( int id ) {
        super( id );
        workflowId = -1;
        lastUpdate = -1;
        statedTimelineDataMap = new HashMap<Integer, List<IntTimePair>>();
    }
    
    @Override
    public final void init( FFContext context ) {
        StateSystem stateSystem = context.getComponent( StateSystem.CONTEXT_KEY );
        if ( !stateSystem.hasWorkflow( workflowId ) ) {
            throw new FFInitException( "The Workflow with id: " + workflowId + " does not exists within stateSystem" );
        }
        
        int currentStateId = stateSystem.getCurrentStateId( workflowId );
        reset( currentStateId );
    }

    @Override
    public void dispose( FFContext context ) {
        statedTimelineDataMap.clear();
        currentTimelineData = null;
        currentData = null;
        iterator = null;
        lastUpdate = -1;
    }

    @Override
    public final int getWorkflowId() {
        return workflowId;
    }

    public final void setWorkflowId( int workflowId ) {
        this.workflowId = workflowId;
    }

    public final StatedIntTimelineAnimation addTimelineData( int stateId, int value, long time ) {
        if ( active ) {
            throw new IllegalStateException( "An active Animation is not mutable" );
        }
        
        List<IntTimePair> timelineData = getTimelineData( stateId, true );
        timelineData.add( new IntTimePair( value, time ) );
        
        return this;
    }
    
    public final StatedIntTimelineAnimation createTimelineData( int stateId, IntIterator values, long time ) {
        if ( active ) {
            throw new IllegalStateException( "An active Animation is not mutable" );
        }
        
        List<IntTimePair> timelineData = getTimelineData( stateId, true );
        while ( values.hasNext() ) {
            timelineData.add( new IntTimePair( values.next(), time ) );
        }
        
        return this;
    } 

    
    @Override
    public final void onStateChange( StateChangeEvent event ) {
        reset( event.stateChange.getToStateId() );
    }

    @Override
    public final void update( FFTimer timer ) {
        super.update( timer );
        long updateTime = timer.getTime();
        if ( !active ) {
            return;
        }
        
        if ( updateTime - lastUpdate < currentData.time ) {
            return;
        }
        
        lastUpdate = updateTime;
        
        if ( iterator.hasNext() ) {
            currentData = iterator.next();
        } else {
            if ( looping ) {
                iterator = currentTimelineData.iterator();
                currentData = iterator.next();
            } else {
                active = false;
                finished = true;
                return;
            }
        }
    }
    
    @Override
    public final int getValue( int component, int currentValue ) {
        return currentData.value;
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
        
        if ( attributes.contains( TIMELINE_DATA ) ) {
            statedTimelineDataMap.clear();
            statedTimelineDataMap.putAll( stringToTimelineData( attributes.getValue( TIMELINE_DATA ) ) );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TIMELINE_DATA, timelineDataToString() );
    }
    
    private String timelineDataToString() {
        // TODO Auto-generated method stub
        return null;
    }

    private Map<Integer, List<IntTimePair>> stringToTimelineData( String value ) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void reset( int currentStateId ) {
        currentTimelineData = getTimelineData( currentStateId, false );
        iterator = currentTimelineData.iterator();
        currentData = iterator.next();
    }
    
    private List<IntTimePair> getTimelineData( Integer stateId, boolean createNew ) {
        List<IntTimePair> result;
        if ( !statedTimelineDataMap.containsKey( stateId ) && createNew ) {
            result = new ArrayList<IntTimePair>();
            statedTimelineDataMap.put( stateId, result );
        } else {
            result = statedTimelineDataMap.get( stateId );
        }
        
        return result;
    }

}
