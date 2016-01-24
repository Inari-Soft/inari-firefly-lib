package com.inari.firefly.scene;

import java.util.Iterator;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
import com.inari.firefly.component.Component;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.RenderEvent;
import com.inari.firefly.system.RenderEventListener;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.component.SystemComponentBuilder;

public class SceneSystem 
    extends 
        ComponentSystem<SceneSystem>
    implements
        UpdateEventListener,
        RenderEventListener {
    
    public final static FFSystemTypeKey<SceneSystem> SYSTEM_KEY = FFSystemTypeKey.create( SceneSystem.class );
    
    private static final SystemComponentKey<?>[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        Scene.TYPE_KEY
    };

    private final DynArray<Scene> scenes;
    
    public SceneSystem() {
        super( SYSTEM_KEY );
        scenes = new DynArray<Scene>();
    }
    
    @Override
    public final void init( FFContext context ) throws FFInitException {
        super.init( context );

        context.registerListener( UpdateEvent.class, this );
        context.registerListener( RenderEvent.class, this );
        context.registerListener( SceneSystemEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        context.disposeListener( UpdateEvent.class, this );
        context.disposeListener( RenderEvent.class, this );
        context.disposeListener( SceneSystemEvent.class, this );
    }
    
    final void notifySceneEvent( SceneSystemEvent event ) {
        Scene scene = ( event.sceneName != null )? scenes.get( getSceneId( event.sceneName ) ) : scenes.get( event.sceneId );
        switch ( event.type ) {
            case RUN : {
                scene.run( context );
                break;
            }
            case PAUSE : {
                scene.pause();
                break;
            }
            case RESUME : {
                scene.resume();
                break;
            }
            case STOP : {
                scene.stop( context );
                if ( scene.isRunOnce() ) {
                    scene.dispose( context );
                    deleteScene( event.sceneId );
                }
                break;
            }
            case DELETE : {
                scene.dispose( context );
                deleteScene( scene.getId() );
                break;
            }
        }
    }
    
    public final Scene getScene( int sceneId ) {
        if ( !scenes.contains( sceneId ) ) {
            return null;
        }
        
        return scenes.get( sceneId );
    }
    
    public final <S extends Scene> S getSceneAs( int sceneId, Class<S> subType ) {
        Scene scene = getScene( sceneId ); 
        if ( scene == null ) {
            return null;
        }
        
        return subType.cast( scene );
    }
    
    public final int getSceneId( String sceneName ) {
        for ( Scene scene : scenes ) {
            if ( sceneName.equals( scene.getName() ) ) {
                return scene.getId();
            }
        }
        
        return -1;
    }

    public final void deleteScene( int sceneId ) {
        if ( !scenes.contains( sceneId ) ) {
            return;
        }
        Scene scene = scenes.remove( sceneId );
        if ( scene.isActive() ) {
            scene.stop( context );
        }
        
        disposeSystemComponent( scene );
    }
    
    public final void clear() {
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
                    scene.render();
                } else if ( scene.getLayerId() < 0 ) {
                    scene.render();
                }
            }
        }
    }

    @Override
    public final SystemComponentKey<?>[] supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final SystemBuilderAdapter<?>[] getSupportedBuilderAdapter() {
        return new SystemBuilderAdapter<?>[] {
            new SceneBuilderHelper( this )
        };
    }

    public final class SceneBuilder extends SystemComponentBuilder {

        @Override
        public final SystemComponentKey<Scene> systemComponentKey() {
            return Scene.TYPE_KEY;
        }
        
        @Override
        public final int doBuild( int componentId, Class<?> sceneType, boolean activate ) {
            attributes.put( Component.INSTANCE_TYPE_NAME, sceneType.getName() );
            Scene result = getInstance( context, componentId );
            result.fromAttributes( attributes );
            scenes.set( result.index(), result );
            return result.getId();
        }
    }
    
    private final class SceneBuilderHelper extends SystemBuilderAdapter<Scene> {
        public SceneBuilderHelper( SceneSystem system ) {
            super( system, new SceneBuilder() );
        }
        @Override
        public final SystemComponentKey<Scene> componentTypeKey() {
            return Scene.TYPE_KEY;
        }
        @Override
        public final Scene getComponent( int id ) {
            return scenes.get( id );
        }
        @Override
        public final void deleteComponent( int id ) {
            deleteScene( id );
        }
        @Override
        public final Iterator<Scene> getAll() {
            return scenes.iterator();
        }
        @Override
        public final void deleteComponent( String name ) {
           deleteScene( getSceneId( name ) );
            
        }
        @Override
        public final Scene getComponent( String name ) {
            return getScene( getSceneId( name ) );
        }
    }

}
