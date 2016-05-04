package com.inari.firefly.platformer;

import com.inari.commons.lang.aspect.Aspect;

public enum PFState implements Aspect {
    GO_LEFT,
    GO_RIGHT,
    FALLING,
    ON_GROUND,
    WALK_LADDER_UP,
    WALK_LADDER_DOWN,
    JUMP,
    DOUBLE_JUMP
    ;
    
    @Override
    public final int aspectId() {
        return ordinal();
    }
}
