package com.inari.firefly.platformer;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.physics.collision.CollisionResolver;
import com.inari.firefly.physics.collision.Collisions;
import com.inari.firefly.physics.movement.EMovement;

public final class PFCollisionResolver extends CollisionResolver {

    protected PFCollisionResolver( int id ) {
        super( id );
    }

    @Override
    public final void resolve( Collisions  collisions ) {
        
        //System.out.println( "collisions: "+collisions );
        
        int movingEntityId = collisions.movingEntityId();
        ETransform transform = context.getEntityComponent( movingEntityId, ETransform.TYPE_KEY );
        EMovement movement = context.getEntityComponent( movingEntityId, EMovement.TYPE_KEY );
        
        final float velocityY = movement.getVelocityY();
        final float velocityX = movement.getVelocityX();
        
        if ( yAxisFirst ) {
            if ( velocityY != 0 ) {
                if ( velocityX != 0 ) {
                    transform.move( -velocityX, 0f );
                    collisions.update();
                }
                resolveYAxisCollision( transform, collisions, velocityY );
                transform.move( velocityX, 0f );
            }
            
            if ( velocityX != 0 ) {
                collisions.update();
                resolveXAxisCollision( transform, collisions, velocityX );
            }
            
            return;
        }

        if ( velocityX != 0 ) {
            if ( velocityY != 0 ) {
                transform.move( 0f, -velocityY );
                collisions.update();
            }
            resolveXAxisCollision( transform, collisions, velocityX );
            transform.move( 0f, velocityY );
        }
        
        if ( velocityY != 0) {
            collisions.update();
            resolveYAxisCollision( transform, collisions, velocityY );
        }
    }
    
    private void resolveXAxisCollision( ETransform transform, Collisions collisions, float velocityX ) {
        
        int xCorrection = 0;
        int collisionHeight = (int) Math.floor( collisions.worldBounds().height * 0.6 );
        for ( Collisions.CollisionData collision : collisions ) {
            final Rectangle bounds = collision.intersectionBounds();
            if ( bounds.y > collisionHeight ) {
                continue;
            }
            
            if ( collision.intersectionMask() == null ) {
                if ( bounds.width > xCorrection ) {
                    xCorrection = bounds.width;
                }
            }
            
            //System.out.println( "collision: " + collision );
        }

        if ( xCorrection != 0 ) {
            transform.setXpos(  
                ( velocityX < 0 )? 
                    (float) Math.ceil( transform.getXpos() ) + xCorrection - 1 : 
                        (float) Math.ceil( transform.getXpos() ) - xCorrection 
            );
        }
    }

    private void resolveYAxisCollision( ETransform transform, Collisions collisions, float velocityY ) {
        if ( velocityY >= 0 ) {
            return;
        }

        int yCorrection = 0;
        for ( Collisions.CollisionData collision : collisions ) {
            final Rectangle bounds = collision.intersectionBounds();
            if ( bounds.width > 1 && bounds.height > yCorrection ) {
                yCorrection = bounds.height;
            }
        }
        
        if ( yCorrection != 0 ) {
            transform.setYpos( 
                ( velocityY < 0 )? 
                    (float) Math.floor( transform.getYpos() ) + yCorrection : 
                        (float) Math.ceil( transform.getYpos() ) - yCorrection 
            );
        }
    }
 
}
