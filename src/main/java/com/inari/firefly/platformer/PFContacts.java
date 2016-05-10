package com.inari.firefly.platformer;

import com.inari.firefly.physics.collision.ContactType;

public interface PFContacts {

    public static final ContactType GROUND = new ContactType( 0, "Ground" );
    public static final ContactType LADDER = new ContactType( 0, "LADDER" );
    
}
