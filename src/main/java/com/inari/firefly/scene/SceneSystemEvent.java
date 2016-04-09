package com.inari.firefly.scene;

import com.inari.commons.event.Event;

public class SceneSystemEvent extends Event<SceneSystem> {
    
    public static final EventTypeKey TYPE_KEY = createTypeKey( SceneSystemEvent.class );

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

    public SceneSystemEvent( int sceneId, EventType type ) {
        super( TYPE_KEY );
        sceneName = null;
        this.sceneId = sceneId;
        this.type = type;
    }
    
    public SceneSystemEvent( String sceneName, EventType type ) {
        super( TYPE_KEY );
        this.sceneName = sceneName;
        this.sceneId = -1;
        this.type = type;
    }

    @Override
    protected void notify( SceneSystem listener ) {
        listener.notifySceneEvent( this );
    }

}
