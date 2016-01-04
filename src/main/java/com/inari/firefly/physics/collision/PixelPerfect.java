package com.inari.firefly.physics.collision;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.indexed.IIndexedTypeKey;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.component.SystemComponent;

public final class PixelPerfect extends SystemComponent {
    
    public static final SystemComponentKey<PixelPerfect> TYPE_KEY = SystemComponentKey.create( PixelPerfect.class );

    public static final AttributeKey<Integer> REGION_WIDTH = new AttributeKey<Integer>( "regionWidth", Integer.class, PixelPerfect.class );
    public static final AttributeKey<Integer> REGION_HEIGHT = new AttributeKey<Integer>( "regionHeight", Integer.class, PixelPerfect.class );
    public static final AttributeKey<BitSet> PIXEL_MAP = new AttributeKey<BitSet>( "pixelMap", BitSet.class, PixelPerfect.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        REGION_WIDTH,
        REGION_HEIGHT,
        PIXEL_MAP
    };
    
    final Rectangle region;
    BitSet pixelMap;

    protected PixelPerfect( int id ) {
        super( id );
        region = new Rectangle();
        pixelMap = new BitSet();
    }
    
    @Override
    public final IIndexedTypeKey indexedTypeKey() {
        return TYPE_KEY;
    }
    
    public final void setWidth( int width ) {
        region.width = width;
    }

    public final int getWidth() {
        return region.width;
    }
    
    public final void setHeight( int height ) {
        region.height = height;
    }

    public final int getHeight() {
        return region.height;
    }
    
    public final BitSet getPixelMap() {
        return pixelMap;
    }
    
    public final void clear() {
        pixelMap.clear();
        
    }
    
    public final void setPixel( int x, int y ) {
        checkPosition( x, y );
        pixelMap.set( y * region.width + x );
    }
    
    public final void setPixel( Position... points ) {
        for ( Position p : points ) {
            setPixel( p.x, p.y );
        }
    }
    
    public final void setPixelRegion( Rectangle region ) {
        Rectangle intersectionRegion = new Rectangle();
        GeomUtils.intersection( region, this.region, intersectionRegion );
        PixelPerfectUtils.setRegion( this.region, pixelMap, intersectionRegion );
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) ) );
        return attributeKeys;
    }
    
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        region.width = attributes.getValue( REGION_WIDTH, region.width );
        region.height = attributes.getValue( REGION_HEIGHT, region.height );
        pixelMap = attributes.getValue( PIXEL_MAP, pixelMap );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( REGION_WIDTH, region.width );
        attributes.put( REGION_HEIGHT, region.height );
        attributes.put( PIXEL_MAP, pixelMap );
    } 
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "PixelPerfect [region=" ).append( region );
        builder.append( "\npixelMap=" ).append( PixelPerfectUtils.pixelMapToString( pixelMap, region.width, region.height ) );
        return builder.toString();
    }
    
    private final void checkPosition( int x, int y ) {
        if ( x < 0 || y < 0 || x >= region.width || y >= region.height ) {
            throw new IllegalArgumentException( "position out of bounds: x=" + x + " y=" + y + " width=" + region.width + " height=" + region.height );
        }
    }

//    public final boolean intersects( int x, int y ) {
//        if ( x < 0 || y < 0 || x >= region.width || y >= region.height ) {
//            return false;
//        }
//        
//        return pixels.get( y * region.width + x );
//    }
//    
//    public final boolean intersects( Position... points ) {
//        for ( Position p : points ) {
//            if ( intersects( p.x, p.y ) ) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//    
//    public final boolean intersects( int xOffset, int yOffset, Position... points ) {
//        for ( Position p : points ) {
//            if ( intersects( p.x + xOffset, p.y + yOffset ) ) {
//                return true;
//            }
//        }
//        
//        return false;
//    }
//    
//    public final boolean intersects( Rectangle region ) {
//        return intersects( region, false );
//    }
//    
//    public final boolean intersects( Rectangle region, boolean cornerCheck ) {
//        GeomUtils.intersection( region, this.region, intersectionRegion );
//        
//        if ( cornerCheck ) {
//            return intersects( intersectionRegion.x, intersectionRegion.y ) ||
//                   intersects( intersectionRegion.x + intersectionRegion.width, intersectionRegion.y ) ||
//                   intersects( intersectionRegion.x, intersectionRegion.y + intersectionRegion.height ) ||
//                   intersects( intersectionRegion.x + intersectionRegion.width, intersectionRegion.y + intersectionRegion.height );
//        } else {
//            intersectionPixels.clear();
//            setFormIntersectionRegion( intersectionPixels );
//            return pixels.intersects( intersectionPixels );
//        }
//    }
//
//    public final boolean intersects( int xOffset, int yOffset, PixelPerfect other ) {
//        tempRegion.x = other.region.x + xOffset;
//        tempRegion.y = other.region.y + yOffset;
//        tempRegion.width = other.region.width;
//        tempRegion.height = other.region.height;
//        GeomUtils.intersection( region, tempRegion, intersectionRegion );
//        intersectionPixels.clear();
//        
//        int otherX = ( xOffset >= 0 )? 0 : -xOffset;
//        int otherY = ( yOffset >= 0 )? 0 : -yOffset;
//        for ( int y = intersectionRegion.y; y < intersectionRegion.y + intersectionRegion.height; y++ ) {
//            for ( int x = intersectionRegion.x; x < intersectionRegion.x + intersectionRegion.width; x++ ) {
//                int index = y * region.width + x;
//                int otherIndex = otherY * other.region.width + otherX;
//                intersectionPixels.set( index, other.pixels.get( otherIndex ) );
//                otherX++;
//            }
//            otherX = 0;
//            otherY++;
//        }
//        
//        return pixels.intersects( intersectionPixels );
//    }
//    
//    

//    
//    private void setFormIntersectionRegion( BitSet bitset ) {
//        for ( int y = intersectionRegion.y; y < intersectionRegion.y + intersectionRegion.height; y++ ) {
//            int formIndex =  y * region.width + intersectionRegion.x;
//            int toX = intersectionRegion.x + intersectionRegion.width;
//            bitset.set( formIndex, y * region.width + toX );
//        }
//    }
//
//    String pixelsToString( BitSet pixels ) {
//        StringBuilder builder = new StringBuilder();
//        for ( int y = 0; y < region.height; y++ ) {
//            builder.append( "\n" );
//            for ( int x = 0; x < region.width; x++ ) {
//                builder.append( ( pixels.get( y * region.width + x ) )? 1 : 0 ); 
//            }
//        }
//        return builder.toString();
//    }

}
