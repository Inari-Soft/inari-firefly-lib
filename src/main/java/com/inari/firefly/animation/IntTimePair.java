package com.inari.firefly.animation;

import com.inari.commons.StringUtils;

public class IntTimePair {
    
    public final int value;
    public final long time;
    
    public IntTimePair( int value, long time ) {
        this.value = value;
        this.time = time;
    }
    
    public IntTimePair( String stringValue ) {
        String[] stringValues = StringUtils.splitToArray( stringValue, StringUtils.VALUE_SEPARATOR_STRING );
        this.value = Integer.parseInt( stringValues[ 0 ] );
        this.time = Long.parseLong( stringValues[ 1 ] );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( value ).append( StringUtils.VALUE_SEPARATOR ).append( time );
        return builder.toString();
    }

}
