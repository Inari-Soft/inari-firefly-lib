package com.inari.firefly.text;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.lang.list.IntMap;
import com.inari.firefly.component.NamedIndexedComponent;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;

public class Font extends NamedIndexedComponent {
    
    public static final AttributeKey<IntMap> CHAR_SPRITE_MAP = new AttributeKey<IntMap>( "workflowId", IntMap.class, Font.class );
    public static final AttributeKey<Integer> CHAR_WIDTH = new AttributeKey<Integer>( "charWidth", Integer.class, Font.class );
    public static final AttributeKey<Integer> CHAR_HEIGHT = new AttributeKey<Integer>( "charHeight", Integer.class, Font.class );
    public static final AttributeKey<Integer> CHAR_SPACE = new AttributeKey<Integer>( "charSpace", Integer.class, Font.class );
    public static final AttributeKey<Integer> LINE_SPACE = new AttributeKey<Integer>( "lineSpace", Integer.class, Font.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        CHAR_SPRITE_MAP
    };

    private IntMap charSpriteMap;
    private int charWidth;
    private int charHeight;
    private int charSpace;
    private int lineSpace;
    
    protected Font( int id ) {
        super( id );
        charSpriteMap = new IntMap( -1, 256 );
    }
    
    public final void setCharSpriteMapping( char character, int spriteId ) {
        charSpriteMap.set( Character.getNumericValue( character ), spriteId );
    }
    
    public final int getSpriteId( char character ) {
        return charSpriteMap.getFast( Character.getNumericValue( character ) );
    }

    public final int getCharWidth() {
        return charWidth;
    }

    public final void setCharWidth( int charWidth ) {
        this.charWidth = charWidth;
    }

    public final int getCharHeight() {
        return charHeight;
    }

    public final void setCharHeight( int charHeight ) {
        this.charHeight = charHeight;
    }

    public final int getCharSpace() {
        return charSpace;
    }

    public final void setCharSpace( int charSpace ) {
        this.charSpace = charSpace;
    }

    public final int getLineSpace() {
        return lineSpace;
    }

    public final void setLineSpace( int lineSpace ) {
        this.lineSpace = lineSpace;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        charSpriteMap = attributes.getValue( CHAR_SPRITE_MAP, charSpriteMap );
        charWidth = attributes.getValue( CHAR_WIDTH, charWidth );
        charHeight = attributes.getValue( CHAR_HEIGHT, charHeight );
        charSpace = attributes.getValue( CHAR_SPACE, charSpace );
        lineSpace = attributes.getValue( LINE_SPACE, lineSpace );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );

        attributes.put( CHAR_SPRITE_MAP, charSpriteMap );
        attributes.put( CHAR_WIDTH, charWidth );
        attributes.put( CHAR_HEIGHT, charHeight );
        attributes.put( CHAR_SPACE, charSpace );
        attributes.put( LINE_SPACE, lineSpace );
    }

}
