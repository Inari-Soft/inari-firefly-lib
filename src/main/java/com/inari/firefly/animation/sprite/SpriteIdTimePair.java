package com.inari.firefly.animation.sprite;

import com.inari.commons.StringUtils;
import com.inari.commons.config.StringConfigurable;

public final class SpriteIdTimePair implements StringConfigurable {
    
    int spriteId;
    long time;
    
    public SpriteIdTimePair( int spriteId, long time ) {
        this.spriteId = spriteId;
        this.time = time;
    }

    public final int getSpriteId() {
        return spriteId;
    }

    public final long getTime() {
        return time;
    }

    @Override
    public void fromConfigString( String stringValue ) {
        String[] stringValues = StringUtils.splitToArray( stringValue, StringUtils.VALUE_SEPARATOR_STRING );
        this.spriteId = Integer.parseInt( stringValues[ 0 ] );
        this.time = Long.parseLong( stringValues[ 1 ] );
    }

    @Override
    public String toConfigString() {
        StringBuilder builder = new StringBuilder();
        builder.append( spriteId ).append( StringUtils.VALUE_SEPARATOR ).append( time );
        return builder.toString();
    }
    
}