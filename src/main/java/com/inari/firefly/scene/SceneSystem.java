package com.inari.firefly.scene;

import java.util.Iterator;

import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.component.Component;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFInitException;
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
        ComponentSystem
    implements
        UpdateEventListener,
        RenderEventListener,
        SceneEventListener {
    
    private static final SystemComponentKey[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        Scene.TYPE_KEY
    };
    
    public final static TypedKey<SceneSystem> CONTEXT_KEY = TypedKey.create( "SceneSystem", SceneSystem.class );

    private FFContext context;

    private final DynArray<Scene> scenes;
    
    public SceneSystem() {
        scenes = new DynArray<Scene>();
    }
    
    @Override
    public final void init( FFContext context ) throws FFInitException {
        this.context = context;

        context.registerListener( UpdateEvent.class, this );
        context.registerListener( RenderEvent.class, this );
        context.registerListener( SceneEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        context.disposeListener( UpdateEvent.class, this );
        context.disposeListener( RenderEvent.class, this );
        context.disposeListener( SceneEvent.class, this );
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
                    scene.render( event.getLayerId() );
                } else if ( scene.getLayerId() < 0 ) {
                    scene.render( event.getLayerId() );
                }
            }
        }
    }

    @Override
    public final SystemComponentKey[] supportedComponentTypes() {
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
        public final SystemComponentKey systemComponentKey() {
            return Scene.TYPE_KEY;
        }
        
        @Override
        public final int doBuild( int componentId, Class<?> sceneType ) {
            attributes.put( Component.INSTANCE_TYPE_NAME, sceneType.getName() );
            Scene result = getInstance( context, componentId );
            result.fromAttributes( attributes );
            scenes.set( result.index(), result );
            return result.getId();
        }
    }
    
    private final class SceneBuilderHelper extends SystemBuilderAdapter<Scene> {
        public SceneBuilderHelper( ComponentSystem system ) {
            super( system, new SceneBuilder() );
        }
        @Override
        public final SystemComponentKey componentTypeKey() {
            return Scene.TYPE_KEY;
        }
        @Override
        public final Scene get( int id, Class<? extends Scene> subtype ) {
            return scenes.get( id );
        }
        @Override
        public final void delete( int id, Class<? extends Scene> subtype ) {
            deleteScene( id );
        }
        @Override
        public final Iterator<Scene> getAll() {
            return scenes.iterator();
        }
    }

}
