package com.inari.firefly.physics.collision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.inari.commons.StringUtils;
import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.component.attr.ComponentAttributeMap;

public class BitMaskTest {
    
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
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        BitMask pp1 = new BitMask( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_1 ) );
        BitMask pp2 = new BitMask( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_2 ) );
        BitMask pp3 = new BitMask( 3 );
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
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        BitMask pp = new BitMask( 1 );
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
        
        pp.setBit( 3, 4 );
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
        pp.setBits( new Position( 0,0 ), new Position( 1,0 ), new Position( 2,0 ) );
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
        
        pp.clearBits();
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
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        BitMask pp1 = new BitMask( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_1 ) );
        BitMask pp2 = new BitMask( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_2 ) );
        BitMask pp3 = new BitMask( 3 );
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
        assertFalse( pp1.intersects( 1, 1 ) );
        assertFalse( pp1.intersects( 5, 6 ) );
        assertFalse( pp1.intersects( 2, 9 ) );
        assertFalse( pp1.intersects( 0, 5 ) );
        assertFalse( pp1.intersects( -1, 1 ) );
        assertFalse( pp1.intersects( 1, 50 ) );
        
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
        assertFalse( pp2.intersects( 1, 1 ) );
        assertTrue( pp2.intersects( 5, 6 ) );
        assertTrue( pp2.intersects( 2, 9 ) );
        assertTrue( pp2.intersects( 0, 5 ) );
        assertFalse( pp2.intersects( -1, 1 ) );
        assertFalse( pp2.intersects( 1, 50 ) );
        
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
        assertFalse( pp3.intersects( 1, 1 ) );
        assertTrue( pp3.intersects( 5, 6 ) );
        assertTrue( pp3.intersects( 2, 9 ) );
        assertFalse( pp3.intersects( 0, 5 ) );
        assertFalse( pp3.intersects( -1, 1 ) );
        assertFalse( pp3.intersects( 1, 50 ) );
    }
    
    @Test
    public void testIntersectionOfRectangle() {
        
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        BitMask pp1 = new BitMask( 1 );
        pp1.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_1 ) );
        BitMask pp2 = new BitMask( 2 );
        pp2.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_2 ) );
        BitMask pp3 = new BitMask( 3 );
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
        assertFalse( pp1.intersects( new Rectangle( 0, 0, 1, 1 ) ) );
        assertFalse( pp1.intersects( new Rectangle( 5, 5, 1, 1 ) ) );
        assertFalse( pp1.intersects( new Rectangle( 0, 0, 10, 10 ) ) );
        assertFalse( pp1.intersects( new Rectangle( 2, 3, 4, 5 ) ) );
        assertFalse( pp1.intersects( new Rectangle( -3, 0, 50, 4 ) ) );
            
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
        assertFalse( pp2.intersects( new Rectangle( 0, 0, 1, 1 ) ) );
        assertTrue( pp2.intersects( new Rectangle( 5, 5, 1, 1 ) ) );
        assertTrue( pp2.intersects( new Rectangle( 0, 0, 10, 10 ) ) );
        assertTrue( pp2.intersects( new Rectangle( 2, 3, 4, 5 ) ) );
        assertFalse( pp2.intersects( new Rectangle( -3, 0, 50, 4 ) ) );
        
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
        assertFalse( pp3.intersects( new Rectangle( 0, 0, 1, 1 ) ) );
        assertTrue( pp3.intersects( new Rectangle( 5, 5, 1, 1 ) ) );
        assertTrue( pp3.intersects( new Rectangle( 0, 0, 10, 10 ) ) );
        assertTrue( pp3.intersects( new Rectangle( 2, 3, 4, 5 ) ) );
        assertTrue( pp3.intersects( new Rectangle( -3, 0, 50, 4 ) ) );
        
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
        
        assertFalse( pp1.intersects( otherRegion, true ) );
        assertTrue( pp1.intersects( otherRegion ) );
    }
    
    @Test
    public void testIntersectionWithOther() {
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_1 ) );
        BitMask pp1 = new BitMask( 2 );
        pp1.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_2 ) );
        BitMask pp2 = new BitMask( 3 );
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
        
        assertTrue( pp2.intersects( 0, 0, pp1 ) );
        assertTrue( pp2.intersects( -1, -1, pp1 ) );
        assertTrue( pp2.intersects( -2, -2, pp1 ) );
        assertTrue( pp2.intersects( -3, -3, pp1 ) );
        assertTrue( pp2.intersects( -4, -4, pp1 ) );
        assertFalse( pp2.intersects( -5, -5, pp1 ) );
        assertFalse( pp2.intersects( -6, -6, pp1 ) );
    }
    
    @Test
    public void testIntersectionPerformance() {
        AttributeMap attrs = new ComponentAttributeMap();
        attrs.put( BitMask.WIDTH, 10 );
        attrs.put( BitMask.HEIGHT, 10 );
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_1 ) );
        BitMask pp1 = new BitMask( 2 );
        pp1.fromAttributes( attrs );
        
        attrs.put( BitMask.BITS, StringUtils.bitsetFromString( PP_REGION_2 ) );
        BitMask pp2 = new BitMask( 3 );
        pp2.fromAttributes( attrs );
        
        // 10000 * 10 = 100000 checks seems to are no problem for performance
        for ( int f = 0; f < 10000; f++ ) {
            for ( int i = 0; i < 10; i++ ) {
                pp2.intersects( -1, -1, pp1 );
            }
        }
    }

}
