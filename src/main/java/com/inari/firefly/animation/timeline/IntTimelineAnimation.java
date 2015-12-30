package com.inari.firefly.animation.timeline;

import com.inari.firefly.animation.IntAnimation;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.external.FFTimer;

public final class IntTimelineAnimation extends IntAnimation {
    
    public static final AttributeKey<IntTimelineData[]> TIMELINE = new AttributeKey<IntTimelineData[]>( 
        "timeline", IntTimelineData[].class, IntTimelineAnimation.class 
    );
    
    private IntTimelineData[] timeline;
    
    private long lastUpdate;
    private int currentIndex;

    protected IntTimelineAnimation( int id ) {
        super( id );
        lastUpdate = -1;
        currentIndex = 0;
    }

    public final IntTimelineData[] getTimeline() {
        return timeline;
    }

    public final void setTimeline( IntTimelineData[] timeline ) {
        this.timeline = timeline;
    }
    
    @Override
    public final void update( FFTimer timer ) {
        super.update( timer );
        if ( !active ) {
            return;
        }
        
        super.update( timer );
        long updateTime = timer.getTime();
        
        if ( lastUpdate < 0 ) {
            lastUpdate = updateTime;
            return;
        }
        
        if ( updateTime - lastUpdate < timeline[ currentIndex ].time ) {
            return;
        }
        
        lastUpdate = updateTime;
        currentIndex++;
        
        if ( currentIndex >= timeline.length ) {
            currentIndex = 0;
            if ( !looping ) {
                active = false;
            } 
        } 
    }
    
    @Override
    public final int getInitValue() {
        if ( timeline == null || timeline.length < 1 ) {
            return -1;
        }
        
        return getValue( -1, -1 );
    }

    @Override
    public final int getValue( int component, int currentValue ) {
        return timeline[ currentIndex ].value;
    }
    
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        timeline = attributes.getValue( TIMELINE, timeline );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( TIMELINE, timeline );
    }

}
