package com.inari.firefly.platformer;

import com.inari.commons.lang.indexed.Indexed;

public enum PFState implements Indexed {
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
    public final int index() {
        return ordinal();
    }
}
