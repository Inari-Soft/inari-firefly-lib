package com.inari.firefly.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.graphics.RGBColor;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;
import com.inari.firefly.renderer.BlendMode;

public class EText extends EntityComponent {
    
    public static final int COMPONENT_TYPE = Indexer.getIndexForType( EText.class, EntityComponent.class );
    
    public static final AttributeKey<Integer> FONT_ID = new AttributeKey<Integer>( "fontId", Integer.class, EText.class );
    public static final AttributeKey<String> TEXT = new AttributeKey<String>( "text", String.class, EText.class );
    public static final AttributeKey<RGBColor> TINT_COLOR = new AttributeKey<RGBColor>( "tintColor", RGBColor.class, EText.class );
    public static final AttributeKey<BlendMode> BLEND_MODE = new AttributeKey<BlendMode>( "blendMode", BlendMode.class, EText.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        FONT_ID,
        TEXT,
        TINT_COLOR,
        BLEND_MODE
    };
    
    private int fontId;
    private String text;
    private final RGBColor tintColor;
    private BlendMode blendMode;
    
    public EText() {
        super();
        fontId = -1;
        text = null;
        tintColor = new RGBColor( 1, 1, 1, 1 );
        blendMode = BlendMode.NONE;
    }

    @Override
    public final Class<EText> getComponentType() {
        return EText.class;
    }

    public final int getFontId() {
        return fontId;
    }

    public final void setFontId( int fontId ) {
        this.fontId = fontId;
    }

    public final String getText() {
        return text;
    }

    public final void setText( String text ) {
        this.text = text;
    }

    public final BlendMode getBlendMode() {
        return blendMode;
    }

    public final void setBlendMode( BlendMode blendMode ) {
        this.blendMode = blendMode;
    }

    public final RGBColor getTintColor() {
        return tintColor;
    }
    
    public final void setTintColor( RGBColor tintColor ) {
        this.tintColor.r = tintColor.r;
        this.tintColor.g = tintColor.g;
        this.tintColor.b = tintColor.b;
        this.tintColor.a = tintColor.a;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        fontId = attributes.getValue( FONT_ID, fontId );
        text = attributes.getValue( TEXT, text );
        setTintColor( attributes.getValue( TINT_COLOR, tintColor ) );
        blendMode = attributes.getValue( BLEND_MODE, blendMode );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( FONT_ID, fontId );
        attributes.put( TEXT, text );
        attributes.put( TINT_COLOR, tintColor );
        attributes.put( BLEND_MODE, blendMode );
    }

}
