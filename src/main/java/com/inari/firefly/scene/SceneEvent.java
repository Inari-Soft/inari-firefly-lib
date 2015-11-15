package com.inari.firefly.scene;

import com.inari.commons.event.Event;

public class SceneEvent extends Event<SceneEventListener> {
    
    public static final EventTypeKey TYPE_KEY = createTypeKey( SceneEvent.class );

    public enum EventType {
        ACTIVATE,
        PAUSE,
        RESUME,
        STOP,
        DELETE
    }
    
    public final int sceneId;
    public final EventType type;

    private SceneEvent( int sceneId, EventType type ) {
        super( TYPE_KEY );
        this.sceneId = sceneId;
        this.type = type;
    }

    @Override
    public void notify( SceneEventListener listener ) {
        listener.notifySceneEvent( this );
    }

}
