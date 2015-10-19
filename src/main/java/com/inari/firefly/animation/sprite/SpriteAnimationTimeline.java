package com.inari.firefly.animation.sprite;

import java.util.ArrayList;
import java.util.List;

import com.inari.commons.StringUtils;
import com.inari.commons.config.StringConfigurable;

public final class SpriteAnimationTimeline implements StringConfigurable {
    
    private final List<SpriteIdTimePair> timlineData = new ArrayList<SpriteIdTimePair>();
    private int index = 0;

    public final void add( int spriteId, long time ) {
        timlineData.add( new SpriteIdTimePair( spriteId, time ) );
    }
    
    public final void setFrameTime( int timeInMillis ) {
        for ( SpriteIdTimePair pair : timlineData ) {
            pair.time = timeInMillis;
        }
    }
    
    public final int getSpriteId() {
        SpriteIdTimePair spriteIdTimePair = timlineData.get( index );
        return spriteIdTimePair.spriteId;
    }
    
    public final long getTime() {
        return timlineData.get( index ).time;
    }
    
    public final boolean hasNext() {
        return index < timlineData.size() - 1;
    }
    
    public final void next() {
        index++;
    }
    
    public final int size() {
        return timlineData.size();
    }
    
    public final void reset() {
        index = 0;
    }
    
    public final void clear() {
        timlineData.clear();
    }

    @Override
    public final void fromConfigString( String stringValue ) {
        timlineData.clear();
        String[] values = StringUtils.splitToArray( stringValue, StringUtils.LIST_VALUE_SEPARATOR_STRING );
        for ( int i = 0; i < values.length; i++ ) {
            SpriteIdTimePair value = new SpriteIdTimePair( 0, 0 );
            value.fromConfigString( values[ i ] );
            timlineData.add( value );
        }
    }

    @Override
    public final String toConfigString() {
        return StringUtils.join( timlineData, StringUtils.LIST_VALUE_SEPARATOR_STRING );
    }

}
