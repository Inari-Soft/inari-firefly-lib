package com.inari.firefly.physics.collision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.component.attr.ComponentAttributeMap;

public class PixelPerfectTest {
    
    @Before
    public void init() {
        Indexer.clear();
    }
    
    public static final String PP_REGION_1 = 
            "0000000000" +
            "0000000000" +
            "0000000000" +
            "0000000000" +
            "0000000000" +
            "1111111111" +
            "1111111111" +
            "1111111111" +
            "1111111111" +
            "1111111111";
    
    public static final String PP_REGION_2 = 
            "0000000001" +
            "0000000011" +
            "0000000111" +
            "0000001111" +
            "0000011111" +
            "0000111111" +
            "0001111111" +
            "0011111111" +
            "0111111111" +
            "1111111111";
    
    @Test
    public void testCreation() {
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        PixelPerfect pp1 = new PixelPerfect( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_1 ) );
        PixelPerfect pp2 = new PixelPerfect( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_2 ) );
        PixelPerfect pp3 = new PixelPerfect( 3 );
        pp3.fromAttributes( attrs );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp1.toString() 
        );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111", 
            pp2.toString() 
        );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000001\n" + 
            "0000000011\n" + 
            "0000000111\n" + 
            "0000001111\n" + 
            "0000011111\n" + 
            "0000111111\n" + 
            "0001111111\n" + 
            "0011111111\n" + 
            "0111111111\n" + 
            "1111111111", 
            pp3.toString() 
        );
    }
    
    @Test
    public void testSetPixel() {
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        PixelPerfect pp = new PixelPerfect( 1 );
        pp.fromAttributes( attrs );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp.toString() 
        );
        
        pp.setPixel( 3, 4 );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0001000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp.toString() 
        );
        pp.setPixel( new Position( 0,0 ), new Position( 1,0 ), new Position( 2,0 ) );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "1110000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0001000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp.toString() 
        );
        pp.setPixelRegion( new Rectangle( 5, 5, 4, 4 ) );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "1110000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0001000000\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000000000", 
            pp.toString() 
        );
        pp.setPixelRegion( new Rectangle( 5, 0, 40, 2 ) );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "1110011111\n" + 
            "0000011111\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0001000000\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000011110\n" + 
            "0000000000", 
            pp.toString() 
        );
        
        pp.clear();
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp.toString() 
        );
    }
    
    @Test
    public void testIntersectionOfPixel() {
        PixelPerfectUtils utils = new PixelPerfectUtils();
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        PixelPerfect pp1 = new PixelPerfect( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_1 ) );
        PixelPerfect pp2 = new PixelPerfect( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_2 ) );
        PixelPerfect pp3 = new PixelPerfect( 3 );
        pp3.fromAttributes( attrs );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp1.toString() 
        );
        assertFalse( utils.intersects( pp1, 1, 1 ) );
        assertFalse( utils.intersects( pp1, 5, 6 ) );
        assertFalse( utils.intersects( pp1, 2, 9 ) );
        assertFalse( utils.intersects( pp1, 0, 5 ) );
        assertFalse( utils.intersects( pp1, -1, 1 ) );
        assertFalse( utils.intersects( pp1, 1, 50 ) );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111", 
            pp2.toString() 
        );
        assertFalse( utils.intersects( pp2, 1, 1 ) );
        assertTrue( utils.intersects( pp2, 5, 6 ) );
        assertTrue( utils.intersects( pp2, 2, 9 ) );
        assertTrue( utils.intersects( pp2, 0, 5 ) );
        assertFalse( utils.intersects( pp2, -1, 1 ) );
        assertFalse( utils.intersects( pp2, 1, 50 ) );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000001\n" + 
            "0000000011\n" + 
            "0000000111\n" + 
            "0000001111\n" + 
            "0000011111\n" + 
            "0000111111\n" + 
            "0001111111\n" + 
            "0011111111\n" + 
            "0111111111\n" + 
            "1111111111", 
            pp3.toString() 
        );
        assertFalse( utils.intersects( pp3, 1, 1 ) );
        assertTrue( utils.intersects( pp3, 5, 6 ) );
        assertTrue( utils.intersects( pp3, 2, 9 ) );
        assertFalse( utils.intersects( pp3, 0, 5 ) );
        assertFalse( utils.intersects( pp3, -1, 1 ) );
        assertFalse( utils.intersects( pp3, 1, 50 ) );
    }
    
    @Test
    public void testIntersectionOfRectangle() {
        PixelPerfectUtils utils = new PixelPerfectUtils();
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        PixelPerfect pp1 = new PixelPerfect( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_1 ) );
        PixelPerfect pp2 = new PixelPerfect( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_2 ) );
        PixelPerfect pp3 = new PixelPerfect( 3 );
        pp3.fromAttributes( attrs );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp1.toString() 
        );
        assertFalse( utils.intersects( pp1, new Rectangle( 0, 0, 1, 1 ) ) );
        assertFalse( utils.intersects( pp1, new Rectangle( 5, 5, 1, 1 ) ) );
        assertFalse( utils.intersects( pp1, new Rectangle( 0, 0, 10, 10 ) ) );
        assertFalse( utils.intersects( pp1, new Rectangle( 2, 3, 4, 5 ) ) );
        assertFalse( utils.intersects( pp1, new Rectangle( -3, 0, 50, 4 ) ) );
            
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111", 
            pp2.toString() 
        );
        assertFalse( utils.intersects( pp2, new Rectangle( 0, 0, 1, 1 ) ) );
        assertTrue( utils.intersects( pp2, new Rectangle( 5, 5, 1, 1 ) ) );
        assertTrue( utils.intersects( pp2, new Rectangle( 0, 0, 10, 10 ) ) );
        assertTrue( utils.intersects( pp2, new Rectangle( 2, 3, 4, 5 ) ) );
        assertFalse( utils.intersects( pp2, new Rectangle( -3, 0, 50, 4 ) ) );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000001\n" + 
            "0000000011\n" + 
            "0000000111\n" + 
            "0000001111\n" + 
            "0000011111\n" + 
            "0000111111\n" + 
            "0001111111\n" + 
            "0011111111\n" + 
            "0111111111\n" + 
            "1111111111", 
            pp3.toString() 
        );
        assertFalse( utils.intersects( pp3, new Rectangle( 0, 0, 1, 1 ) ) );
        assertTrue( utils.intersects( pp3, new Rectangle( 5, 5, 1, 1 ) ) );
        assertTrue( utils.intersects( pp3, new Rectangle( 0, 0, 10, 10 ) ) );
        assertTrue( utils.intersects( pp3, new Rectangle( 2, 3, 4, 5 ) ) );
        assertTrue( utils.intersects( pp3, new Rectangle( -3, 0, 50, 4 ) ) );
        
        // difference between with or without cornerCheck
        pp1.setPixelRegion( new Rectangle( 5, 5, 5, 2 ) );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000011111\n" + 
            "0000011111\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000", 
            pp1.toString() 
        );
        
        Rectangle otherRegion = new Rectangle( -10, -10, 17, 17 );
        
        assertFalse( utils.intersects( pp1, otherRegion, true ) );
        assertTrue( utils.intersects( pp1, otherRegion ) );
    }
    
    @Test
    public void testIntersectionWithOther() {
        PixelPerfectUtils utils = new PixelPerfectUtils();
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_1 ) );
        PixelPerfect pp1 = new PixelPerfect( 2 );
        pp1.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_2 ) );
        PixelPerfect pp2 = new PixelPerfect( 3 );
        pp2.fromAttributes( attrs );
        
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "0000000000\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111\n" + 
            "1111111111", 
            pp1.toString() 
        );
        assertEquals( 
            "PixelPerfect [region=[x=0,y=0,width=10,height=10]\n" + 
            "pixelMap=\n" + 
            "0000000001\n" + 
            "0000000011\n" + 
            "0000000111\n" + 
            "0000001111\n" + 
            "0000011111\n" + 
            "0000111111\n" + 
            "0001111111\n" + 
            "0011111111\n" + 
            "0111111111\n" + 
            "1111111111", 
            pp2.toString() 
        );
        
        assertTrue( utils.intersects( pp2, 0, 0, pp1 ) );
        assertTrue( utils.intersects( pp2,-1, -1, pp1 ) );
        assertTrue( utils.intersects( pp2,-2, -2, pp1 ) );
        assertTrue( utils.intersects( pp2,-3, -3, pp1 ) );
        assertTrue( utils.intersects( pp2,-4, -4, pp1 ) );
        assertFalse( utils.intersects( pp2,-5, -5, pp1 ) );
        assertFalse( utils.intersects( pp2,-6, -6, pp1 ) );
    }
    
    @Test
    public void testIntersectionPerformance() {
        PixelPerfectUtils utils = new PixelPerfectUtils();
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( PixelPerfect.REGION_WIDTH, 10 );
        attrs.put( PixelPerfect.REGION_HEIGHT, 10 );
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_1 ) );
        PixelPerfect pp1 = new PixelPerfect( 2 );
        pp1.fromAttributes( attrs );
        
        attrs.put( PixelPerfect.PIXEL_MAP, PixelPerfectUtils.pixelMapFromString( PP_REGION_2 ) );
        PixelPerfect pp2 = new PixelPerfect( 3 );
        pp2.fromAttributes( attrs );
        
        // 10000 * 10 = 100000 checks seems to are no problem for performance
        for ( int f = 0; f < 10000; f++ ) {
            for ( int i = 0; i < 10; i++ ) {
                utils.intersects( pp2, -1, -1, pp1 );
            }
        }
    }

}
