package com.inari.firefly.platformer;

import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.aspect.AspectGroup;
import com.inari.firefly.physics.collision.CollisionSystem;

public enum PFContact implements Aspect {
    LADDER,
    SPIKE
    ;

    private Aspect aspect;
    private PFContact() {
        aspect = CollisionSystem.CONTACT_ASPECT_GROUP.createAspect( name() );
    }
    
    @Override
    public AspectGroup aspectGroup() {
        return aspect.aspectGroup();
    }
    @Override
    public int index() {
        return aspect.index();
    }
}
