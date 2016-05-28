package com.inari.firefly.platformer;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.physics.collision.CollisionResolver;
import com.inari.firefly.physics.collision.CollisionSystem;
import com.inari.firefly.physics.collision.Contact;
import com.inari.firefly.physics.collision.ECollision;
import com.inari.firefly.physics.movement.EMovement;

public final class PFCollisionResolver extends CollisionResolver {

    protected PFCollisionResolver( int id ) {
        super( id );
    }

    @Override
    public final void resolve( int entityId ) {
        
        ECollision collision = context.getEntityComponent( entityId, ECollision.TYPE_KEY );
        ETransform transform = context.getEntityComponent( entityId, ETransform.TYPE_KEY );
        EMovement movement = context.getEntityComponent( entityId, EMovement.TYPE_KEY );
        DynArray<Contact> contacts = collision.getContacts();
        
        //System.out.println( "contacts: "+contacts );

        final float velocityY = movement.getVelocityY();
        final float velocityX = movement.getVelocityX();
        
        if ( yAxisFirst ) {
            if ( velocityY != 0 ) {
                if ( velocityX != 0 ) {
                    transform.move( -velocityX, 0f );
                    // TODO notify with event
                    context.getSystem( CollisionSystem.SYSTEM_KEY ).updateContacts( entityId );
                    contacts = collision.getContacts();
                }
                resolveYAxisCollision( transform, contacts, velocityY );
                transform.move( velocityX, 0f );
            }
            
            if ( velocityX != 0 ) {
                // TODO notify with event
                context.getSystem( CollisionSystem.SYSTEM_KEY ).updateContacts( entityId );
                contacts = collision.getContacts();
                resolveXAxisCollision( transform, contacts, velocityX );
            }
            
            return;
        }

        if ( velocityX != 0 ) {
            if ( velocityY != 0 ) {
                transform.move( 0f, -velocityY );
                // TODO notify with event
                context.getSystem( CollisionSystem.SYSTEM_KEY ).updateContacts( entityId );
                contacts = collision.getContacts();
            }
            resolveXAxisCollision( transform, contacts, velocityX );
            transform.move( 0f, velocityY );
        }
        
        if ( velocityY != 0) {
            // TODO notify with event
            context.getSystem( CollisionSystem.SYSTEM_KEY ).updateContacts( entityId );
            contacts = collision.getContacts();
            resolveYAxisCollision( transform, contacts, velocityY );
        }
    }
    
    private void resolveXAxisCollision( ETransform transform, DynArray<Contact> contacts, float velocityX ) {
        
        int xCorrection = 0;
        for ( Contact contact : contacts ) {
            if ( !contact.isSolid() ) {
                continue;
            }
            final Rectangle bounds = contact.intersectionBounds();
            if ( contact.intersectionMask() == null && bounds.width > xCorrection && bounds.height > 3  ) {
                xCorrection = bounds.width;
            }
            
            //System.out.println( "collision: " + collision );
        }

        if ( xCorrection != 0 ) {
            transform.setXpos(  
                ( velocityX < 0 )? 
                    (float) Math.floor( transform.getXpos() ) + xCorrection: 
                        (float) Math.ceil( transform.getXpos() ) - xCorrection
            );
            
            //System.out.println( "xCorrection: " + xCorrection + " xpos: " + transform.getXpos() );
        }
    }

    private void resolveYAxisCollision( ETransform transform, DynArray<Contact> contacts, float velocityY ) {
        if ( velocityY >= 0 ) {
            return;
        }

        int yCorrection = 0;
        for ( Contact contact : contacts ) {
            if ( !contact.isSolid() ) {
                continue;
            }
            final Rectangle bounds = contact.intersectionBounds();
            if ( bounds.width > 1 && bounds.height > yCorrection ) {
                yCorrection = bounds.height;
            }
        }
        
        if ( yCorrection != 0 ) {
 
            transform.setYpos( 
                ( velocityY < 0 )? 
                    (float) Math.floor( transform.getYpos() ) + yCorrection : 
                        (float) Math.floor( transform.getYpos() ) - yCorrection 
            );
            
            //System.out.println( "yCorrection: " + yCorrection + " ypos: " + transform.getYpos() );
        }
    }
 
}
