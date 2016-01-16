package com.inari.firefly.physics.movement;

import com.inari.commons.geom.Vector2f;
import com.inari.commons.lang.indexed.IIndexedTypeKey;
import com.inari.firefly.system.component.SystemComponent;

public final class VelocityVector extends SystemComponent {
    
    final Vector2f vector = new Vector2f();
    

    protected VelocityVector( int id ) {
        super( id );
    }

    @Override
    public IIndexedTypeKey indexedTypeKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
