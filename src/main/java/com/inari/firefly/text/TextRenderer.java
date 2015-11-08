package com.inari.firefly.text;

import com.inari.commons.graphics.RGBColor;
import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.indexed.IndexedTypeSet;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityComponent;
import com.inari.firefly.renderer.BaseRenderer;
import com.inari.firefly.renderer.BlendMode;
import com.inari.firefly.renderer.SpriteRenderable;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFInitException;
import com.inari.firefly.system.RenderEvent;
import com.inari.firefly.system.RenderEventListener;

public class TextRenderer extends BaseRenderer implements RenderEventListener {
    
    private final int COMPONENT_ID_ETEXT = Indexer.getIndexForType( EText.class, EntityComponent.class );
    
    public static final TypedKey<TextRenderer> CONTEXT_KEY = TypedKey.create( "TextRenderer", TextRenderer.class );
    
    private TextSystem textSystem;
    
    @Override
    public void init( FFContext context ) throws FFInitException {
        super.init( context );
        textSystem = context.getComponent( TextSystem.CONTEXT_KEY );
        
        context.getComponent( FFContext.EVENT_DISPATCHER ).register( RenderEvent.class, this );
    }
    
    @Override
    public void dispose( FFContext context ) {
        context.getComponent( FFContext.EVENT_DISPATCHER ).unregister( RenderEvent.class, this );
    }

    @Override
    public void render( RenderEvent event ) {
        if ( !textSystem.hasTexts( event.getViewId() ) ) {
            return;
        } 
        
        DynArray<IndexedTypeSet> textsToRender = textSystem.getTexts( event.getViewId(), event.getLayerId() );
        if ( textsToRender == null ) {
            return;
        }
        
        for ( int i = 0; i < textsToRender.capacity(); i++ ) {
            IndexedTypeSet components = textsToRender.get( i );
            if ( components == null ) {
                continue;
            }
            
            EText text = components.get( COMPONENT_ID_ETEXT );
            ETransform transform = components.get( COMPONENT_ID_ETRANSFORM );
            Font font = textSystem.getFont( text.getFontId() );
            
            char[] chars = text.getText();
            RENDERABLE.blendMode = text.getBlendMode();
            RENDERABLE.tintColor = text.getTintColor();
            transformCollector.set( transform );
            int horizontalStep = font.getCharWidth() + font.getCharSpace();
            int verticalStep = font.getCharHeight() + font.getLineSpace();
            
            for ( char character : chars ) {
                if ( character == '\n' ) {
                    transformCollector.xpos = transform.getXpos();
                    transformCollector.ypos += verticalStep;
                    continue;
                }

                RENDERABLE.spriteId = font.getSpriteId( character );
                render( RENDERABLE );
                transformCollector.xpos += horizontalStep;
            }
        }
    }

    final TextRenderable RENDERABLE = new TextRenderable();
    
    final class TextRenderable implements SpriteRenderable {
        
        int spriteId;
        RGBColor tintColor; 
        BlendMode blendMode;

        @Override
        public int getSpriteId() {
            return spriteId;
        }

        @Override
        public RGBColor getTintColor() {
            return tintColor;
        }

        @Override
        public BlendMode getBlendMode() {
            return blendMode;
        }

        @Override
        public int getOrdering() {
            return 0;
        }
    };

}