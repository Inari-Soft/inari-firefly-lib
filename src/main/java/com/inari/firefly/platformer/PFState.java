package com.inari.firefly.platformer;

import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.aspect.AspectGroup;
import com.inari.firefly.entity.EEntity;

public enum PFState implements Aspect {
    ON_GROUND,
    WALK_LEFT,
    WALK_RIGHT,
    ON_LADDER,
    CLIMB_UP,
    CLIMB_DOWN,
    JUMP,
    DOUBLE_JUMP
    ;
    
    private Aspect aspect;
    private PFState() {
        aspect = EEntity.ENTITY_ASPECT_GROUP.createAspect( name() );
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
