package com.inari.firefly.text;

import java.util.HashSet;
import java.util.Set;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.indexed.IndexedTypeSet;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.asset.AssetNameKey;
import com.inari.firefly.asset.AssetSystem;
import com.inari.firefly.component.ComponentBuilderHelper;
import com.inari.firefly.component.ComponentSystem;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.component.build.BaseComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilderFactory;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityComponent;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.entity.event.EntityActivationEvent;
import com.inari.firefly.entity.event.EntityActivationListener;
import com.inari.firefly.renderer.TextureAsset;
import com.inari.firefly.renderer.sprite.SpriteAsset;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.FFInitException;

public class TextSystem 
    implements 
        FFContextInitiable, 
        EntityActivationListener, 
        ComponentSystem, 
        ComponentBuilderFactory {
    
    private final int COMPONENT_ID_ETRANFORM = Indexer.getIndexForType( ETransform.class, EntityComponent.class );
    private final int COMPONENT_ID_ETEXT = Indexer.getIndexForType( EText.class, EntityComponent.class );
    
    public static final TypedKey<TextSystem> CONTEXT_KEY = TypedKey.create( "TextSystem", TextSystem.class );
    
    private EntitySystem entitySystem;
    private AssetSystem assetSystem;

    private final DynArray<Font> fonts;
    private final DynArray<DynArray<DynArray<IndexedTypeSet>>> textPerViewAndLayer;
    
    TextSystem() {
        fonts = new DynArray<Font>();
        textPerViewAndLayer = new DynArray<DynArray<DynArray<IndexedTypeSet>>>();
    }
    
    @Override
    public final void init( FFContext context ) throws FFInitException {
        entitySystem = context.getComponent( EntitySystem.CONTEXT_KEY );
        assetSystem = context.getComponent( AssetSystem.CONTEXT_KEY );

        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.register( EntityActivationEvent.class, this );
    }

    @Override
    public final void dispose( FFContext context ) {
        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.unregister( EntityActivationEvent.class, this );
        
        textPerViewAndLayer.clear();
        clear();
    }
    
    public final boolean hasTexts( int viewId ) {
        return textPerViewAndLayer.contains( viewId );
    }
    
    public final Font getFont( int fontId ) {
        return fonts.get( fontId );
    }
    
    public final int getFontId( String name ) {
        for ( Font font : fonts ) {
            if ( name.equals( font.getName() ) ) {
                return font.getId();
            }
        }
        
        return -1;
    }
    
    public final void loadFont( int fontId ) {
        Font font = fonts.get( fontId );
        String name = font.getName();
        assetSystem.loadAsset( new AssetNameKey( name, name ) );
        assetSystem.loadAssets( name );
    }
    
    public final void disposeFont( int fontId ) {
        Font font = fonts.get( fontId );
        String name = font.getName();
        assetSystem.disposeAssets( name );
        assetSystem.disposeAsset( new AssetNameKey( name, name ) );
    }
    
    public final void deleteFont( int fontId ) {
        Font font = fonts.remove( fontId );
        if ( font != null ) {
            font.dispose();
        }
    }
    
    public final void clear() {
        for ( Font font : fonts ) {
            font.dispose();
        }
        fonts.clear();
    }

    @Override
    public final boolean match( AspectBitSet aspect ) {
        return aspect.contains( COMPONENT_ID_ETEXT );
    }

    @Override
    public void onEntityActivationEvent( EntityActivationEvent event ) {
        IndexedTypeSet components = entitySystem.getComponents( event.entityId );
        ETransform transform = components.get( COMPONENT_ID_ETRANFORM );
        int viewId = transform.getViewId();
        int layerId = transform.getLayerId();
        switch ( event.eventType ) {
            case ENTITY_ACTIVATED: {
                DynArray<IndexedTypeSet> renderablesOfView = getTexts( viewId, layerId, true );
                renderablesOfView.add( components );
                break;
            }
            case ENTITY_DEACTIVATED: {
                DynArray<IndexedTypeSet> renderablesOfView = getTexts( viewId, layerId, false );
                renderablesOfView.remove( components );
            }
        }
    }
    
    public final DynArray<IndexedTypeSet> getTexts( int viewId, int layerId ) {
        return getTexts( viewId, layerId, false );
    }
 
    private final DynArray<IndexedTypeSet> getTexts( int viewId, int layerId, boolean createNew ) {
        DynArray<DynArray<IndexedTypeSet>> textPerLayer = null;
        if ( textPerViewAndLayer.contains( viewId ) ) { 
            textPerLayer = textPerViewAndLayer.get( viewId );
        } else if ( createNew ) {
            textPerLayer = new DynArray<DynArray<IndexedTypeSet>>();
            textPerViewAndLayer.set( viewId, textPerLayer );
        }
        
        if ( textPerLayer == null ) {
            return null;
        }
        
        DynArray<IndexedTypeSet> textOfLayer = null;
        if ( textPerLayer.contains( layerId ) ) { 
            textOfLayer = textPerLayer.get( layerId );
        } else if ( createNew ) {
            textOfLayer = new DynArray<IndexedTypeSet>();
            textPerLayer.set( layerId, textOfLayer );
        }
        
        return textOfLayer;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public final <C> ComponentBuilder<C> getComponentBuilder( Class<C> type ) {
        if ( type == Font.class ) {
            return (ComponentBuilder<C>) getFontBuilder();
        }
        
        throw new IllegalArgumentException( "Unsupported IComponent type for TextSystem. Type: " + type );
    }
    
    public final FontBuilder getFontBuilder() {
        return new FontBuilder( this, false );
    }
    
    public final FontBuilder getFontBuilderWithAutoLoad() {
        return new FontBuilder( this, true );
    }

    private static final Set<Class<?>> SUPPORTED_COMPONENT_TYPES = new HashSet<Class<?>>();
    @Override
    public final Set<Class<?>> supportedComponentTypes() {
        if ( SUPPORTED_COMPONENT_TYPES.isEmpty() ) {
            SUPPORTED_COMPONENT_TYPES.add( Font.class );
        }
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final void fromAttributes( Attributes attributes ) {
        fromAttributes( attributes, BuildType.CLEAR_OLD );
    }

    @Override
    public final void fromAttributes( Attributes attributes, BuildType buildType ) {
        if ( buildType == BuildType.CLEAR_OLD ) {
            clear();
        }
        
        new ComponentBuilderHelper<Font>() {
            @Override
            public Font get( int id ) {
                return fonts.get( id );
            }
            @Override
            public void delete( int id ) {
                deleteFont( id );
            }
        }.buildComponents( Font.class, buildType, getFontBuilder(), attributes );
    }

    @Override
    public final void toAttributes( Attributes attributes ) {
        ComponentBuilderHelper.toAttributes( attributes, Font.class, fonts );
    }

    public final class FontBuilder extends BaseComponentBuilder<Font> {
        
        private final boolean autoLoad;

        protected FontBuilder( TextSystem textSystem, boolean autoLoad ) {
            super( textSystem );
            this.autoLoad = autoLoad;
        }

        @Override
        public final Font build( int componentId ) {
            Font font = new Font( componentId );
            font.fromAttributes( attributes );
            
            checkName( font );
            

            Rectangle textureRegion = new Rectangle( 0, 0, font.getCharWidth(), font.getCharHeight() );
            char[][] charTextureMap = font.getCharTextureMap();
            int charWidth = font.getCharWidth();
            int charHeight = font.getCharHeight();
            String fontName = font.getName();
            
            TextureAsset fontTexture = assetSystem.getAssetBuilder( TextureAsset.class )
                .set( TextureAsset.NAME, fontName )
                .set( TextureAsset.ASSET_GROUP, fontName )
                .set( TextureAsset.RESOURCE_NAME, font.getFontTextureResourceName() )
                .set( TextureAsset.TEXTURE_WIDTH, charTextureMap[ 0 ].length * font.getCharWidth() )
                .set( TextureAsset.TEXTURE_HEIGHT, charTextureMap.length * font.getCharHeight() )
            .build();
            
            for ( int y = 0; y < charTextureMap.length; y++ ) {
                for ( int x = 0; x < charTextureMap[ y ].length; x++ ) {
                    textureRegion.x = x * charWidth;
                    textureRegion.y = y * charHeight;
                    
                    SpriteAsset charSpriteAsset = assetSystem.getAssetBuilder( SpriteAsset.class )
                        .set( SpriteAsset.TEXTURE_ID, fontTexture.getId() )
                        .set( SpriteAsset.TEXTURE_REGION, textureRegion )
                        .set( SpriteAsset.ASSET_GROUP, fontName )
                        .set( SpriteAsset.NAME, fontName + "_" + x + "_"+ y )
                    .build();
                    
                    font.setCharSpriteMapping( charTextureMap[ y ][ x ], charSpriteAsset.getId() );
                }
            }
 
            fonts.set( font.index(), font );
            
            if ( autoLoad ) {
                loadFont( font.index() );
            }
            
            return font;
        }
    }

}
