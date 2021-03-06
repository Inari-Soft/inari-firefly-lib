package com.inari.firefly.converter;

import java.util.Map;

import com.inari.commons.lang.functional.IntFunction;

public class IntColorConverter implements IntFunction {
    
    private final Map<Integer, Integer> colorMap;

    public IntColorConverter( Map<Integer, Integer> colorMap ) {
        this.colorMap = colorMap;
    }

    @Override
    public final int f( int colorValue ) {
        Integer filteredColor = colorMap.get( colorValue );
        if ( filteredColor != null ) {
            return filteredColor;
        }
        return colorValue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "ColorReplaceMapFitler [colorMap=" );
        builder.append( colorMap );
        builder.append( "]" );
        return builder.toString();
    }

}
