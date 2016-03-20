package com.inari.firefly.platformer;

import com.inari.commons.lang.indexed.Indexed;

public enum PlatformerState implements Indexed {
    CONTACT_NORTH,
    CONTACT_EAST,
    CONTACT_SOUTH,
    CONTACT_WEST,
    ON_LADDER,
    JUMP,
    DOUBLE_JUMP
    ;
    
    @Override
    public final int index() {
        return ordinal();
    }
}
