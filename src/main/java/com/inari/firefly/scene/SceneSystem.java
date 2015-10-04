package com.inari.firefly.scene;

import java.util.HashSet;
import java.util.Set;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.component.Component;
import com.inari.firefly.component.ComponentBuilderHelper;
import com.inari.firefly.component.ComponentSystem;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.component.build.BaseComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilderFactory;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.FFInitException;
import com.inari.firefly.system.RenderEvent;
import com.inari.firefly.system.RenderEventListener;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;

public class SceneSystem 
    implements
        FFContextInitiable,
        ComponentSystem,
        ComponentBuilderFactory,
        UpdateEventListener,
        RenderEventListener,
        SceneEventListener {
    
    public final static TypedKey<SceneSystem> CONTEXT_KEY = TypedKey.create( "SceneSystem", SceneSystem.class );

    private FFContext context;

    private final DynArray<Scene> scenes;
    
    public SceneSystem() {
        scenes = new DynArray<Scene>();
    }
    
    @Override
    public final void init( FFContext context ) throws FFInitException {
        this.context = context;
        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.register( UpdateEvent.class, this );
        eventDispatcher.register( RenderEvent.class, this );
        eventDispatcher.register( SceneEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        IEventDispatcher eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        eventDispatcher.unregister( UpdateEvent.class, this );
        eventDispatcher.unregister( RenderEvent.class, this );
        eventDispatcher.unregister( SceneEvent.class, this );
    }
    
    @Override
    public void notifySceneEvent( SceneEvent event ) {
        switch ( event.type ) {
            case ACTIVATE : {
                scenes.get( event.sceneId ).run( context );
                break;
            }
            case PAUSE : {
                scenes.get( event.sceneId ).pause();
                break;
            }
            case RESUME : {
                scenes.get( event.sceneId ).resume();
                break;
            }
            case STOP : {
                Scene scene = scenes.get( event.sceneId );
                scene.stop( context );
                if ( scene.isRunOnce() ) {
                    scene.dispose( context );
                    deleteScene( event.sceneId );
                }
                break;
            }
            case DELETE : {
                scenes.get( event.sceneId ).dispose( context );
                deleteScene( event.sceneId );
                break;
            }
        }
    }
    
    public final void deleteScene( int sceneId ) {
        if ( !scenes.contains( sceneId ) ) {
            return;
        }
        Scene scene = scenes.remove( sceneId );
        if ( scene.isActive() ) {
            scene.stop( context );
            scene.dispose( context );
        }
        scene.dispose();
    }
    
    private final void clear() {
        for ( int i = 0; i < scenes.capacity(); i++ ) {
            if ( scenes.contains( i ) ) {
                deleteScene( i );
            }
        }
        scenes.clear();
    }
    
    @Override
    public final void update( UpdateEvent event ) {
        for ( Scene scene : scenes ) {
            if ( scene.isActive() ) {
                scene.tick( event.timer );
            }
        }
    }

    @Override
    public final void render( RenderEvent event ) {
        for ( Scene scene : scenes ) {
            if ( scene.isActive() && scene.getViewId() == event.getViewId() ) {
                if ( scene.getLayerId() == event.getLayerId() ) {
                    scene.render( event.getLayerId() );
                } else if ( scene.getLayerId() < 0 ) {
                    scene.render( event.getLayerId() );
                }
            }
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public final <C> ComponentBuilder<C> getComponentBuilder( Class<C> type ) {
        if ( Scene.class.isAssignableFrom( type ) ) {
            return new SceneBuilder( this, type );
        }
        
        throw new IllegalArgumentException( "Unsupported Component type for SceneSystem Builder. Type: " + type );
    }
    
    public final <S extends Scene> SceneBuilder<S> getSceneBuilder( Class<S> sceneType ) {
        return new SceneBuilder<S>( this, sceneType );
    }

    private static final Set<Class<?>> SUPPORTED_COMPONENT_TYPES = new HashSet<Class<?>>();
    @Override
    public final Set<Class<?>> supportedComponentTypes() {
        if ( SUPPORTED_COMPONENT_TYPES.isEmpty() ) {
            SUPPORTED_COMPONENT_TYPES.add( Scene.class );
        }
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final void fromAttributes( Attributes attributes ) {
        fromAttributes( attributes, BuildType.CLEAR_OLD );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void fromAttributes( Attributes attributes, BuildType buildType ) {
        if ( buildType == BuildType.CLEAR_OLD ) {
            clear();
        }
        for ( Class<? extends Scene> sceneType : attributes.getAllSubTypes( Scene.class ) ) {
            new ComponentBuilderHelper<Scene>() {
                @Override
                public Scene get( int id ) {
                    return scenes.get( id );
                }
                @Override
                public void delete( int id ) {
                    deleteScene( id );
                }
            }.buildComponents( Scene.class, buildType, (SceneBuilder<Scene>) getSceneBuilder( sceneType ), attributes );
        }
    }

    @Override
    public final void toAttributes( Attributes attributes ) {
        ComponentBuilderHelper.toAttributes( attributes, Scene.class, scenes );
    }
    
    public final class SceneBuilder<C extends Scene> extends BaseComponentBuilder<C> {
        
        private final Class<C> sceneType;

        protected SceneBuilder( ComponentBuilderFactory componentFactory, Class<C> sceneType ) {
            super( componentFactory );
            this.sceneType = sceneType;
        }

        @Override
        public C build( int componentId ) {
            attributes.put( Component.INSTANCE_TYPE_NAME, sceneType.getName() );
            C result = getInstance( context, componentId );
            result.fromAttributes( attributes );
            scenes.set( result.index(), result );
            return result;
        }
    }

}
