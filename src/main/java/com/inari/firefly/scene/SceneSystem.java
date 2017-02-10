package com.inari.firefly.scene;

import java.util.Iterator;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
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

        context.registerListener( UpdateEvent.TYPE_KEY, this );
        context.registerListener( RenderEvent.TYPE_KEY, this );
        context.registerListener( SceneSystemEvent.TYPE_KEY, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        context.disposeListener( UpdateEvent.TYPE_KEY, this );
        context.disposeListener( RenderEvent.TYPE_KEY, this );
        context.disposeListener( SceneSystemEvent.TYPE_KEY, this );
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
                deleteScene( scene.index() );
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
                return scene.index();
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
            new SceneBuilderHelper()
        };
    }

    public final class SceneBuilder extends SystemComponentBuilder {
        
        public SceneBuilder( Class<? extends Scene> componentType ) {
            super( context, componentType );
        }

        @Override
        public final SystemComponentKey<Scene> systemComponentKey() {
            return Scene.TYPE_KEY;
        }
        
        @Override
        public final int doBuild( int componentId, Class<?> sceneType, boolean activate ) {
            Scene result = createSystemComponent( componentId, sceneType, context );
            scenes.set( result.index(), result );
            return result.index();
        }
    }
    
    private final class SceneBuilderHelper extends SystemBuilderAdapter<Scene> {
        private SceneBuilderHelper() {
            super( SceneSystem.this, Scene.TYPE_KEY );
        }
        @Override
        public final Scene get( int id ) {
            return scenes.get( id );
        }
        @Override
        public final void delete( int id ) {
            deleteScene( id );
        }
        @Override
        public final Iterator<Scene> getAll() {
            return scenes.iterator();
        }
        @Override
        public int getId( String name ) {
            return getSceneId( name );
        }
        @Override
        public void activate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public void deactivate( int id ) {
            throw new UnsupportedOperationException( componentTypeKey() + " is not activable" );
        }
        @Override
        public SystemComponentBuilder createComponentBuilder( Class<? extends Scene> componentType ) {
            if ( componentType == null ) {
                throw new IllegalArgumentException( "componentType is needed for SystemComponentBuilder for component: " + componentTypeKey().name() );
            }
            return new SceneBuilder( componentType );
        }
    }

}
