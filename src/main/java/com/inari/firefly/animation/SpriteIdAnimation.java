package com.inari.firefly.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.inari.commons.StringUtils;


public final class SpriteIdAnimation extends IntAnimation {
    
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
    
    public static final class SpriteIdTimePair {
        
        public final int spriteId;
        public final long time;
        
        private SpriteIdTimePair( int spriteId, long time ) {
            super();
            this.spriteId = spriteId;
            this.time = time;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append( spriteId ).append( StringUtils.VALUE_SEPARATOR ).append( time );
            return builder.toString();
        }

    }

}
