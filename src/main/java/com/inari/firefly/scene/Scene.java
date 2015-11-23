package com.inari.firefly.scene;

import java.util.Arrays;
import java.util.Set;

import com.inari.firefly.Disposable;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;
import com.inari.firefly.system.component.SystemComponent;

public abstract class Scene extends SystemComponent implements Disposable {
    
    public static final SystemComponentKey TYPE_KEY = SystemComponentKey.create( Scene.class );
    
    public static final AttributeKey<Integer> VIEW_ID = new AttributeKey<Integer>( "viewId", Integer.class, Scene.class );
    public static final AttributeKey<Integer> LAYER_ID = new AttributeKey<Integer>( "layerId", Integer.class, Scene.class );
    public static final AttributeKey<Integer> UPDATE_RESOLUTION = new AttributeKey<Integer>( "updateResolution", Integer.class, Scene.class );
    public static final AttributeKey<Boolean> RUN_ONCE = new AttributeKey<Boolean>( "runOnce", Boolean.class, Scene.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        VIEW_ID,
        LAYER_ID,
        UPDATE_RESOLUTION,
        RUN_ONCE
    };
    
    private int viewId;
    private int layerId;
    private int updateResolution;
    private boolean runOnce;
    
    private boolean active;
    private boolean paused;
    private FFTimer.UpdateScheduler updateScheduler;

    protected Scene( int id ) {
        super( id );
        viewId = -1;
        layerId = -1;
        updateResolution = 50;
        runOnce = true;
        active = false;
        paused = false;
        updateScheduler = null;
    }

    public final int getViewId() {
        return viewId;
    }

    public final void setViewId( int viewId ) {
        this.viewId = viewId;
    }

    public final int getLayerId() {
        return layerId;
    }

    public final void setLayerId( int layerId ) {
        this.layerId = layerId;
    }

    public final int getUpdateResolution() {
        return updateResolution;
    }

    public final void setUpdateResolution( int updateResolution ) {
        this.updateResolution = updateResolution;
    }

    public final boolean isRunOnce() {
        return runOnce;
    }

    public final void setRunOnce( boolean runOnce ) {
        this.runOnce = runOnce;
    }
    
    public final boolean isActive() {
        return active;
    }
    
    public final boolean isPaused() {
        return paused;
    }
    
    final void tick( FFTimer timer ) {
        if ( updateScheduler != null && updateScheduler.needsUpdate() ) {
            update( updateScheduler.getTick() );
        } else {
            update( timer.getTime() );
        }
    }
    
    
    public void run( FFContext context ) {
        if ( active ) {
            return;
        }
        
        if ( updateScheduler == null && updateResolution >= 0 ) {
            updateScheduler = context.getTimer().createUpdateScheduler( updateResolution );
        }
        
        active = true;
    }
    
    public void pause() {
        if ( !active ) {
            return;
        }
        
        paused = true;
    }
    
    public void resume() {
        if ( !paused ) {
            return;
        }
        
        paused = false;
    }
    
    public void stop( FFContext context ) {
        if ( !active ) {
            return;
        }
        
        active = false;
    }
    
    public abstract void update( long tick );
    public abstract void render( int layerId );


    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        viewId = attributes.getValue( VIEW_ID, viewId );
        layerId = attributes.getValue( LAYER_ID, viewId );
        updateResolution = attributes.getValue( UPDATE_RESOLUTION, updateResolution );
        runOnce = attributes.getValue( RUN_ONCE, runOnce );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( VIEW_ID, viewId );
        attributes.put( LAYER_ID, layerId );
        attributes.put( UPDATE_RESOLUTION, updateResolution );
        attributes.put( RUN_ONCE, runOnce );
    }
}
