package com.inari.firefly.physics.collision;

import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.physics.movement.event.MoveEvent;
import com.inari.firefly.physics.movement.event.MoveEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;

public final class CollisionSystem extends ComponentSystem<CollisionSystem> implements MoveEventListener {
    
    public static final FFSystemTypeKey<CollisionSystem> SYSTEM_KEY = FFSystemTypeKey.create( CollisionSystem.class );
    
    private static final SystemComponentKey<?>[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        PixelPerfect.TYPE_KEY,
    };
    
    private final PixelPerfectUtils pixelPerfectUtils;
    private final DynArray<PixelPerfect> pixelPerfectRegions;

    CollisionSystem() {
        super( SYSTEM_KEY );
        pixelPerfectUtils = new PixelPerfectUtils();
        pixelPerfectRegions = new DynArray<PixelPerfect>();
    }
    
    @Override
    public void init( FFContext context ) {
        super.init( context );
        
    }

    @Override
    public void dispose( FFContext context ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final SystemComponentKey<?>[] supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public SystemBuilderAdapter<?>[] getSupportedBuilderAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMoveEvent( MoveEvent event ) {
        // TODO Auto-generated method stub
        
    }

}
