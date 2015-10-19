package com.inari.firefly.animation.sprite;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.animation.IntAnimation;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public abstract class StatedSpriteAnimation extends IntAnimation {
    
    @SuppressWarnings( "rawtypes" )
    public static final AttributeKey<DynArray> SPRITE_ANIMATION_TIMELINES = new AttributeKey<DynArray>( 
        "spriteAnimationTimelines", DynArray.class, StatedSpriteAnimation.class 
    );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        SPRITE_ANIMATION_TIMELINES,
    };

    
    private final DynArray<SpriteAnimationTimeline> spriteAnimationTimelines;
    private SpriteAnimationTimeline current;
    private int currentState;
    private long lastUpdate;
    

    public StatedSpriteAnimation( int id, FFContext context ) {
        super( id );
        
        spriteAnimationTimelines = new DynArray<SpriteAnimationTimeline>();
        currentState = -1;
        lastUpdate = -1;
        current = null;
    }
    
    public final void setSpriteAnimationTimelines( DynArray<SpriteAnimationTimeline> timelines ) {
        if ( timelines == null ) {
            return;
        }
        spriteAnimationTimelines.clear();
        current = null;
        for ( int i = 0; i < timelines.capacity(); i++ ) {
            if ( !timelines.contains( i ) ) {
                continue;
            }
            spriteAnimationTimelines.set( i, timelines.get( i ) );
        }
    }

    public final SpriteAnimationTimeline getSpriteAnimationTimeline( int id ) {
        return spriteAnimationTimelines.get( id );
    }
    
    public final void setSpriteAnimationTimeline( int id, SpriteAnimationTimeline spriteAnimationTimeline ) {
        spriteAnimationTimelines.set( id, spriteAnimationTimeline );
    }

    @Override
    public void update( FFTimer timer ) {
        super.update( timer );
        if ( !active ) {
            return;
        }
        
        if ( current == null ) {
            return;
        }
        
        super.update( timer );
        long updateTime = timer.getTime();
        
        if ( lastUpdate < 0 ) {
            lastUpdate = updateTime;
            return;
        }
        
        if ( updateTime - lastUpdate < current.getTime() ) {
            return;
        }
        
        lastUpdate = updateTime;
        
        if ( current.hasNext() ) {
            current.next();
        } else if ( looping ){
            current.reset();
        } else {
            active = false;
        }
    }

    @Override
    public final int getValue( int entityId, int currentValue ) {
        int state = getState( entityId );
        if ( state != currentState ) {
            current = spriteAnimationTimelines.get( state );
            current.reset();
            currentState = state;
        }
        
        return current.getSpriteId();
    }
    
    public abstract int getState( int entityId );

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        if ( attributes.contains( SPRITE_ANIMATION_TIMELINES ) ) {
            setSpriteAnimationTimelines( attributes.getValue( SPRITE_ANIMATION_TIMELINES ) );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( SPRITE_ANIMATION_TIMELINES, spriteAnimationTimelines );
    }

}
