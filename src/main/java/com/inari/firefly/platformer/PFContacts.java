package com.inari.firefly.platformer;

import com.inari.commons.lang.aspect.Aspect;
import com.inari.firefly.physics.collision.CollisionSystem;

public interface PFContacts {

    public static final Aspect GROUND = CollisionSystem.CONTACT_ASPECT_TYPE.createAspect( "GROUND" );
    public static final Aspect LADDER = CollisionSystem.CONTACT_ASPECT_TYPE.createAspect( "LADDER" );
    
}
