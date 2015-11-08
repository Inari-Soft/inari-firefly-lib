package com.inari.firefly.filter;

import java.util.Map;

public class ColorReplaceMapFitler implements IColorFilter {
    
    private final Map<Integer, Integer> colorMap;

    public ColorReplaceMapFitler( Map<Integer, Integer> colorMap ) {
        this.colorMap = colorMap;
    }

    @Override
    public final int filter( int colorValue ) {
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