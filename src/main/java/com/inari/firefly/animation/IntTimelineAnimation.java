package com.inari.firefly.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.inari.commons.StringUtils;
import com.inari.commons.lang.IntIterator;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFTimer;


public final class IntTimelineAnimation extends IntAnimation {
    
    public static final AttributeKey<String> TIMELINE_DATA = new AttributeKey<String>( "timelineData", String.class, IntTimelineAnimation.class );
    
    private final List<IntTimePair> timelineData;
    
    private IntTimePair currentData;
    private Iterator<IntTimePair> iterator;
    private long lastUpdate;
    

    protected IntTimelineAnimation( int id ) {
        super( id );
        timelineData = new ArrayList<IntTimePair>();
    }

    public final List<IntTimePair> getTimelineData() {
        return timelineData;
    }
    
    public final IntTimelineAnimation addTimelineData( int value, long time ) {
        if ( active ) {
            throw new IllegalStateException( "An active Animation is not mutable" );
        }
        
        timelineData.add( new IntTimePair( value, time ) );
        
        return this;
    }
    
    public final IntTimelineAnimation createTimelineData( IntIterator values, long time ) {
        if ( active ) {
            throw new IllegalStateException( "An active Animation is not mutable" );
        }
        
        while ( values.hasNext() ) {
            timelineData.add( new IntTimePair( values.next(), time ) );
        }
        
        return this;
    }

    @Override
    public void update( FFTimer timer ) {
        if ( !active ) {
            return;
        }
        
        super.update( timer );
        long updateTime = timer.getTime();
        
        if ( iterator == null ) {
            iterator = timelineData.iterator();
            currentData = iterator.next();
        }
        
        if ( updateTime - lastUpdate < currentData.time ) {
            return;
        }
        
        lastUpdate = updateTime;
        
        if ( iterator.hasNext() ) {
            currentData = iterator.next();
        } else {
            if ( looping ) {
                iterator = timelineData.iterator();
                currentData = iterator.next();
            } else {
                active = false;
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
        attributeKeys.add( TIMELINE_DATA );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        if ( attributes.contains( TIMELINE_DATA ) ) {
            timelineData.clear();
            timelineData.addAll( stringToTimelineData( attributes.getValue( TIMELINE_DATA ) ) );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TIMELINE_DATA, StringUtils.join( timelineData, StringUtils.LIST_VALUE_SEPARATOR_STRING ) );
    }
    

    private List<IntTimePair> stringToTimelineData( String value ) {
        List<IntTimePair> result = new ArrayList<IntTimePair>();
        String[] values = StringUtils.splitToArray( value, StringUtils.LIST_VALUE_SEPARATOR_STRING );
        for ( int i = 0; i < values.length; i++ ) {
            result.add( new IntTimePair( values[ i ] ) );
        }
        return result;
    }
  
}
