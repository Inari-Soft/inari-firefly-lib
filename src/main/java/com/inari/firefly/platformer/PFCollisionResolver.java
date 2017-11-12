package com.inari.firefly.platformer;

import com.inari.commons.geom.BitMask;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.FFInitException;
import com.inari.firefly.entity.EEntity;
import com.inari.firefly.graphics.ETransform;
import com.inari.firefly.physics.collision.CollisionResolver;
import com.inari.firefly.physics.collision.CollisionSystem;
import com.inari.firefly.physics.collision.ContactConstraint;
import com.inari.firefly.physics.collision.ContactScan;
import com.inari.firefly.physics.collision.Contacts;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.movement.EMovement;

public final class PFCollisionResolver extends CollisionResolver {
    
    private CollisionSystem collisionSystem;
    
    private Rectangle vScan = new Rectangle( 0, 0, 1, 5 );
    private Rectangle hScan = new Rectangle( 2, 0, 4, 1 );

    protected PFCollisionResolver( int id ) {
        super( id );
    }
    
    

    @Override
    protected void init() throws FFInitException {
        super.init();
        
        collisionSystem = context.getSystem( CollisionSystem.SYSTEM_KEY );
    }

    @Override
    public final void resolve( final int entityId ) {
        final EEntity entity = context.getEntityComponent( entityId, EEntity.TYPE_KEY );
        final ECollision collision = context.getEntityComponent( entityId, ECollision.TYPE_KEY );
        final ETransform transform = context.getEntityComponent( entityId, ETransform.TYPE_KEY );
        final EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        
        final ContactScan contactScan = collision.getContactScan();
        final Contacts solidContacts = contactScan.getContacts( context.getSystemComponentId( ContactConstraint.TYPE_KEY, PFContact.PLATFORMER_SOLID_CONTACT_SCAN ) );
        final Contacts ladderContacts = contactScan.getContacts( context.getSystemComponentId( ContactConstraint.TYPE_KEY, PFContact.PLATFORMER_LADDER_CONTACT_SCAN ) );
        final BitMask intersectionMask = solidContacts.getIntersectionMask();
        final boolean groundContact = entity.hasAspect( PFState.ON_GROUND );

        final float velocityY = movement.getVelocityY();
        final float velocityX = movement.getVelocityX();

        entity.resetAspect( PFState.ON_GROUND );
        if ( groundContact && contactScan.hasContact( PFContact.LADDER ) ) {
            entity.setAspect( PFState.ON_GROUND );
        }
        
        if ( ladderContacts != null && !ladderContacts.hasAnyContact() && entity.hasAspect( PFState.ON_LADDER ) ) {
            entity.resetAspect( PFState.ON_LADDER );
            if ( entity.hasAspect( PFState.CLIMB_UP ) ) {
                entity.setAspect( PFState.ON_GROUND );
                entity.resetAspect( PFState.CLIMB_UP );
                transform.setYpos( (float) Math.ceil( transform.getYpos() ) - movement.getVelocityY() );
            }
        }

        if ( intersectionMask.isEmpty() && !contactScan.hasContact( PFContact.LADDER ) ) {
            return;
        }

        int ycorrection = 0;
        if ( velocityY >= 0 ) {
            ycorrection = adjustToGround( intersectionMask, contactScan );
            if ( ycorrection != 0 && !( ycorrection > 0 && !groundContact ) ) {
                entity.setAspect( PFState.ON_GROUND );
            } else {
                ycorrection = 0;
            }
        } else if ( velocityY < 0 ) {
            hScan.y = 0;
            while( intersectionMask.hasIntersection( hScan ) ) {
                ycorrection++;
                hScan.y++;
            }
        }
        
        if ( ycorrection != 0 ) {
            if ( ycorrection < 0 ) { 
                transform.setYpos( (float) Math.ceil( transform.getYpos() ) + ycorrection );
            } else {
                transform.setYpos( (float) Math.floor( transform.getYpos() ) + ycorrection );
            }
            
            collisionSystem.updateContacts( entityId );
        }

        int xcorrection = 0;
        if ( velocityX > 0 ) {
            vScan.x = 7;
            while( intersectionMask.hasIntersection( vScan ) ) {
                xcorrection++;
                vScan.x--;
            }
            
            if ( xcorrection != 0 ) {
                transform.setXpos( (float) Math.ceil( transform.getXpos() ) - xcorrection );
            }
        } else if ( velocityX < 0 ) {
            vScan.x = 0;
            while( intersectionMask.hasIntersection( vScan ) ) {
                xcorrection++;
                vScan.x++;
            }
            
            if ( xcorrection != 0 ) {
                transform.setXpos( (float) Math.floor( transform.getXpos() ) + xcorrection );
            }
        }
    }
    
    private int adjustToGround( final BitMask intersectionMask, final ContactScan contactScan ) {
        int correction = 0;
        final int halfHeight = 5;
        for ( int i = 0; i < 10; i++ ) {
            if ( intersectionMask.getBit( 3, 3 + i ) || intersectionMask.getBit( 4, 3 + i ) ) {
                correction = -( halfHeight - i );
                break;
            }
        }
        
        return correction;
    }
 
}
