package com.inari.firefly.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.inari.commons.StringUtils;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;


public final class SpriteIdAnimation extends IntAnimation {
    
    public static final AttributeKey<String> TIMELINE_DATA = new AttributeKey<String>( "timelineData", String.class, SpriteIdAnimation.class );
    
    private final List<SpriteIdTimePair> timelineData;
    
    private SpriteIdTimePair currentData;
    private Iterator<SpriteIdTimePair> iterator;
    private long lastUpdate;
    

    protected SpriteIdAnimation( int id ) {
        super( id );
        timelineData = new ArrayList<SpriteIdTimePair>();
    }

    @Override
    public void update( long updateTime ) {
        super.update( updateTime );
        if ( active ) {
            if ( iterator == null ) {
                iterator = timelineData.iterator();
                currentData = iterator.next();
            }
            
            if ( updateTime - lastUpdate < currentData.time ) {
                return;
            }
            
            if ( iterator.hasNext() ) {
                currentData = iterator.next();
                lastUpdate = updateTime;
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
    }

    @Override
    public final int getValue( int component, int currentValue ) {
        return currentData.spriteId;
    }
    
    @Override
    public Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.add( TIMELINE_DATA );
        return attributeKeys;
    }

    @Override
    public void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        if ( attributes.contains( TIMELINE_DATA ) ) {
            timelineData.clear();
            timelineData.addAll( stringToTimelineData( attributes.getValue( TIMELINE_DATA ) ) );
        }
    }

    @Override
    public void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TIMELINE_DATA, StringUtils.join( timelineData, StringUtils.LIST_VALUE_SEPARATOR_STRING ) );
    }
    

    private List<SpriteIdTimePair> stringToTimelineData( String value ) {
        List<SpriteIdTimePair> result = new ArrayList<SpriteIdTimePair>();
        String[] values = StringUtils.splitToArray( value, StringUtils.LIST_VALUE_SEPARATOR_STRING );
        for ( int i = 0; i < values.length; i++ ) {
            result.add( new SpriteIdTimePair( values[ i ] ) );
        }
        return result;
    }
    
    public static final class SpriteIdTimePair {
        
        public final int spriteId;
        public final long time;
        
        private SpriteIdTimePair( int spriteId, long time ) {
            this.spriteId = spriteId;
            this.time = time;
        }
        
        private SpriteIdTimePair( String stringValue ) {
            String[] stringValues = StringUtils.splitToArray( stringValue, StringUtils.VALUE_SEPARATOR_STRING );
            this.spriteId = Integer.parseInt( stringValues[ 0 ] );
            this.time = Long.parseLong( stringValues[ 1 ] );
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append( spriteId ).append( StringUtils.VALUE_SEPARATOR ).append( time );
            return builder.toString();
        }
    }

}
