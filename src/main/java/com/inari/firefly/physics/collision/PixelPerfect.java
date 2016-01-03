package com.inari.firefly.physics.collision;

import java.util.BitSet;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;

public final class PixelPerfect {
    
    final Rectangle region;
    final Rectangle intersectionRegion;
    final Rectangle tempRegion;
    
    final BitSet pixels;
    final BitSet intersectionPixels;

    public PixelPerfect( int width, int height ) {
        region = new Rectangle( 0, 0, width, height );
        intersectionRegion = new Rectangle( 0, 0, 0, 0 );
        tempRegion = new Rectangle( 0, 0, 0, 0 );
        pixels = new BitSet( width * height );
        intersectionPixels = new BitSet( width * height );
    }
    
    public PixelPerfect( int width, int height, String pixelsString ) {
        this( width, height );
        
        for ( int i = 0; i < pixelsString.length(); i++ ) {
            pixels.set( i, pixelsString.charAt( i ) != '0' );
        }
    }

    public final int getWidth() {
        return region.width;
    }

    public final int getHeight() {
        return region.height;
    }
    
    public final void clear() {
        pixels.clear();
        intersectionPixels.clear();
        intersectionRegion.x = 0;
        intersectionRegion.y = 0;
        intersectionRegion.width = 0;
        intersectionRegion.height = 0;
        tempRegion.x = 0;
        tempRegion.y = 0;
        tempRegion.width = 0;
        tempRegion.height = 0;
    }
    
    public final void set( int x, int y ) {
        checkPosition( x, y );
        pixels.set( y * region.width + x );
    }
    
    public final void set( Position... points ) {
        for ( Position p : points ) {
            set( p.x, p.y );
        }
    }
    
    public final void set( Rectangle region ) {
        GeomUtils.intersection( region, this.region, intersectionRegion );
        setFormIntersectionRegion( pixels );
    }
    
    public final boolean intersects( int x, int y ) {
        if ( x < 0 || y < 0 || x >= region.width || y >= region.height ) {
            return false;
        }
        
        return pixels.get( y * region.width + x );
    }
    
    public final boolean intersects( Position... points ) {
        for ( Position p : points ) {
            if ( intersects( p.x, p.y ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public final boolean intersects( int xOffset, int yOffset, Position... points ) {
        for ( Position p : points ) {
            if ( intersects( p.x + xOffset, p.y + yOffset ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public final boolean intersects( Rectangle region ) {
        return intersects( region, false );
    }
    
    public final boolean intersects( Rectangle region, boolean cornerCheck ) {
        GeomUtils.intersection( region, this.region, intersectionRegion );
        
        if ( cornerCheck ) {
            return intersects( intersectionRegion.x, intersectionRegion.y ) ||
                   intersects( intersectionRegion.x + intersectionRegion.width, intersectionRegion.y ) ||
                   intersects( intersectionRegion.x, intersectionRegion.y + intersectionRegion.height ) ||
                   intersects( intersectionRegion.x + intersectionRegion.width, intersectionRegion.y + intersectionRegion.height );
        } else {
            intersectionPixels.clear();
            setFormIntersectionRegion( intersectionPixels );
            return pixels.intersects( intersectionPixels );
        }
    }

    public final boolean intersects( int xOffset, int yOffset, PixelPerfect other ) {
        tempRegion.x = other.region.x + xOffset;
        tempRegion.y = other.region.y + yOffset;
        tempRegion.width = other.region.width;
        tempRegion.height = other.region.height;
        GeomUtils.intersection( region, tempRegion, intersectionRegion );
        intersectionPixels.clear();
        
        int otherX = ( xOffset >= 0 )? 0 : -xOffset;
        int otherY = ( yOffset >= 0 )? 0 : -yOffset;
        for ( int y = intersectionRegion.y; y < intersectionRegion.y + intersectionRegion.height; y++ ) {
            for ( int x = intersectionRegion.x; x < intersectionRegion.x + intersectionRegion.width; x++ ) {
                int index = y * region.width + x;
                int otherIndex = otherY * other.region.width + otherX;
                intersectionPixels.set( index, other.pixels.get( otherIndex ) );
                otherX++;
            }
            otherX = 0;
            otherY++;
        }
        
        return pixels.intersects( intersectionPixels );
    }
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "PixelPerfect [region=" ).append( region );
        builder.append( "\npixels=" ).append( pixelsToString( pixels ) );
        return builder.toString();
    }
    
    private final void checkPosition( int x, int y ) {
        if ( x < 0 || y < 0 || x >= region.width || y >= region.height ) {
            throw new IllegalArgumentException( "position out of bounds: x=" + x + " y=" + y + " width=" + region.width + " height=" + region.height );
        }
    }
    
    private void setFormIntersectionRegion( BitSet bitset ) {
        for ( int y = intersectionRegion.y; y < intersectionRegion.y + intersectionRegion.height; y++ ) {
            int formIndex =  y * region.width + intersectionRegion.x;
            int toX = intersectionRegion.x + intersectionRegion.width;
            bitset.set( formIndex, y * region.width + toX );
        }
    }

    String pixelsToString( BitSet pixels ) {
        StringBuilder builder = new StringBuilder();
        for ( int y = 0; y < region.height; y++ ) {
            builder.append( "\n" );
            for ( int x = 0; x < region.width; x++ ) {
                builder.append( ( pixels.get( y * region.width + x ) )? 1 : 0 ); 
            }
        }
        return builder.toString();
    }

}
