package com.inari.firefly.controller;

import com.inari.commons.event.IEventDispatcher;
import com.inari.firefly.control.Controller;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.view.View;
import com.inari.firefly.system.view.ViewSystem;
import com.inari.firefly.system.view.event.ViewEvent;
import com.inari.firefly.system.view.event.ViewEventListener;

public abstract class ViewController extends Controller implements ViewEventListener {
    
    protected ViewSystem viewSystem;
    protected IEventDispatcher eventDispatcher;
    
    protected ViewController( int id, FFContext context ) {
        super( id );
        viewSystem = context.getComponent( FFContext.Systems.VIEW_SYSTEM );
        eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        
        eventDispatcher.register( ViewEvent.class, this );
    }

    @Override
    public void dispose( FFContext context ) {
        eventDispatcher.unregister( ViewEvent.class, this );
    }

    @Override
    public void onViewEvent( ViewEvent event ) {
        switch ( event.eventType ) {
        case VIEW_ACTIVATED: {
            if ( event.view.getControllerId() == indexedId ) {
                componentIds.add( event.view.index() );
            }
            break;
        } 
        case VIEW_DISPOSED: {
            componentIds.remove( event.view.index() );
            break;
        }
        default: {}
    }
    }

    @Override
    public void update( long time ) {
        for ( int i = 0; i < componentIds.length(); i++ ) {
            if ( componentIds.isEmpty( i ) ) {
                continue;
            }
            int viewId = componentIds.get( i );
            update( time, viewSystem.getView( viewId ) );
        }
    }
    
    
    public abstract void update( long time, View view );

}
