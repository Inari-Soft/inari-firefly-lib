package com.inari.firefly.animation.sprite;

import java.util.Set;

import com.inari.firefly.animation.IntAnimation;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.external.FFTimer;

public class SpriteAnimation extends IntAnimation {
    
    public static final AttributeKey<SpriteAnimationTimeline> SPRITE_ANIMATION_TIMELINE = new AttributeKey<SpriteAnimationTimeline>( 
        "spriteAnimationTimeline", SpriteAnimationTimeline.class, SpriteAnimation.class 
    );
    
    private final SpriteAnimationTimeline spriteAnimationTimeline;
    private long lastUpdate;

    public SpriteAnimation( int id ) {
        super( id );
        spriteAnimationTimeline = new SpriteAnimationTimeline();
        lastUpdate = -1;
    }

    public final SpriteAnimationTimeline getSpriteAnimationTimeline() {
        return spriteAnimationTimeline;
    }
    
    public final void setSpriteAnimationTimeline( SpriteAnimationTimeline spriteAnimationTimeline ) {
        this.spriteAnimationTimeline.clear();
        while( spriteAnimationTimeline.hasNext() ) {
            spriteAnimationTimeline.next();
            this.spriteAnimationTimeline.add( spriteAnimationTimeline.getSpriteId(), spriteAnimationTimeline.getTime() );
        }
    }

    @Override
    public void update( FFTimer timer ) {
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
        
        if ( updateTime - lastUpdate < spriteAnimationTimeline.getTime() ) {
            return;
        }
        
        lastUpdate = updateTime;
        
        if ( spriteAnimationTimeline.hasNext() ) {
            spriteAnimationTimeline.next();
        } else if ( looping ){
            spriteAnimationTimeline.reset();
        } else {
            active = false;
        }
    }
    
    @Override
    public final int getInitValue() {
        return getValue( -1, -1 );
    }

    @Override
    public int getValue( int component, int currentValue ) {
        return spriteAnimationTimeline.getSpriteId();
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.add( SPRITE_ANIMATION_TIMELINE );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        if ( attributes.contains( SPRITE_ANIMATION_TIMELINE ) ) {
            setSpriteAnimationTimeline( attributes.getValue( SPRITE_ANIMATION_TIMELINE ) );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( SPRITE_ANIMATION_TIMELINE, spriteAnimationTimeline );
    }

}
