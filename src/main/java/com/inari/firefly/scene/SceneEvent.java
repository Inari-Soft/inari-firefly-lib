package com.inari.firefly.scene;

import com.inari.commons.event.Event;

public class SceneEvent extends Event<SceneEventListener> {
    
    public static final EventTypeKey TYPE_KEY = createTypeKey( SceneEvent.class );

    public enum EventType {
        RUN,
        PAUSE,
        RESUME,
        STOP,
        DELETE
    }
    
    public final String sceneName;
    public final int sceneId;
    public final EventType type;

    public SceneEvent( int sceneId, EventType type ) {
        super( TYPE_KEY );
        sceneName = null;
        this.sceneId = sceneId;
        this.type = type;
    }
    
    public SceneEvent( String sceneName, EventType type ) {
        super( TYPE_KEY );
        this.sceneName = sceneName;
        this.sceneId = -1;
        this.type = type;
    }

    @Override
    public void notify( SceneEventListener listener ) {
        listener.notifySceneEvent( this );
    }

}
