package com.inari.firefly.physics.collision;

import java.util.BitSet;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;

public class PixelPerfectUtils {
    
    private final Rectangle intersectionRegion;
    private final Rectangle tempRegion;
    private final BitSet intersectionPixels;
    
    public PixelPerfectUtils() {
        intersectionRegion = new Rectangle( 0, 0, 0, 0 );
        tempRegion = new Rectangle( 0, 0, 0, 0 );
        intersectionPixels = new BitSet();
    }
    
//    public void clear() {
//        intersectionPixels.clear();
//        intersectionRegion.x = 0;
//        intersectionRegion.y = 0;
//        intersectionRegion.width = 0;
//        intersectionRegion.height = 0;
//        tempRegion.x = 0;
//        tempRegion.y = 0;
//        tempRegion.width = 0;
//        tempRegion.height = 0;
//    }
    
    public final boolean intersects( PixelPerfect pixelPerfect, int x, int y ) {
        if ( x < 0 || y < 0 || x >= pixelPerfect.region.width || y >= pixelPerfect.region.height ) {
            return false;
        }
        
        return pixelPerfect.pixelMap.get( y * pixelPerfect.region.width + x );
    }
    
    public final boolean intersects( PixelPerfect pixelPerfect, Position... points ) {
        for ( Position p : points ) {
            if ( intersects( pixelPerfect, p.x, p.y ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public final boolean intersects( PixelPerfect pixelPerfect, int xOffset, int yOffset, Position... points ) {
        for ( Position p : points ) {
            if ( intersects( pixelPerfect, p.x + xOffset, p.y + yOffset ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public final boolean intersects( PixelPerfect pixelPerfect, Rectangle region ) {
        return intersects( pixelPerfect, region, false );
    }
    
    public final boolean intersects( PixelPerfect pixelPerfect, Rectangle region, boolean cornerCheck ) {
        GeomUtils.intersection( region, pixelPerfect.region, intersectionRegion );
        
        if ( cornerCheck ) {
            return intersects( pixelPerfect, intersectionRegion.x, intersectionRegion.y ) ||
                   intersects( pixelPerfect, intersectionRegion.x + intersectionRegion.width, intersectionRegion.y ) ||
                   intersects( pixelPerfect, intersectionRegion.x, intersectionRegion.y + intersectionRegion.height ) ||
                   intersects( pixelPerfect, intersectionRegion.x + intersectionRegion.width, intersectionRegion.y + intersectionRegion.height );
        } else {
            intersectionPixels.clear();
            setRegion( pixelPerfect.region, intersectionPixels, intersectionRegion );
            return pixelPerfect.pixelMap.intersects( intersectionPixels );
        }
    }

    public final boolean intersects( PixelPerfect pixelPerfect, int xOffset, int yOffset, PixelPerfect other ) {
        tempRegion.x = other.region.x + xOffset;
        tempRegion.y = other.region.y + yOffset;
        tempRegion.width = other.region.width;
        tempRegion.height = other.region.height;
        GeomUtils.intersection( pixelPerfect.region, tempRegion, intersectionRegion );
        intersectionPixels.clear();
        
        int otherX = ( xOffset >= 0 )? 0 : -xOffset;
        int otherY = ( yOffset >= 0 )? 0 : -yOffset;
        for ( int y = intersectionRegion.y; y < intersectionRegion.y + intersectionRegion.height; y++ ) {
            for ( int x = intersectionRegion.x; x < intersectionRegion.x + intersectionRegion.width; x++ ) {
                int index = y * pixelPerfect.region.width + x;
                int otherIndex = otherY * other.region.width + otherX;
                intersectionPixels.set( index, other.pixelMap.get( otherIndex ) );
                otherX++;
            }
            otherX = 0;
            otherY++;
        }
        
        return pixelPerfect.pixelMap.intersects( intersectionPixels );
    }
    
    public static final BitSet pixelMapFromString( String pixelsString ) {
        BitSet pixels = new BitSet( pixelsString.length() );
        for ( int i = 0; i < pixelsString.length(); i++ ) {
            pixels.set( i, pixelsString.charAt( i ) != '0' );
        }
        return pixels;
    }

    public static final String pixelMapToString( BitSet pixels, int width, int height ) {
        StringBuilder builder = new StringBuilder();
        for ( int y = 0; y < height; y++ ) {
            builder.append( "\n" );
            for ( int x = 0; x < width; x++ ) {
                builder.append( ( pixels.get( y * width + x ) )? 1 : 0 ); 
            }
        }
        return builder.toString();
    }
    
    static final void setRegion( Rectangle targetRegion, BitSet bitset, Rectangle subRegion ) {
        for ( int y = subRegion.y; y < subRegion.y + subRegion.height; y++ ) {
            int formIndex =  y * targetRegion.width + subRegion.x;
            int toX = subRegion.x + subRegion.width;
            bitset.set( formIndex, y * targetRegion.width + toX );
        }
    }

}
