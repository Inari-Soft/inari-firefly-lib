package com.inari.firefly.platformer;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.physics.collision.CollisionResolver;
import com.inari.firefly.physics.collision.Collisions;
import com.inari.firefly.physics.collision.Collisions.CollisionData;

public final class PlatformerCollisionResolver extends CollisionResolver {

    protected PlatformerCollisionResolver( int id ) {
        super( id );
    }

    @Override
    public final void resolve( Collisions collisions ) {
        final float velocityY = collisions.entityData.movement.getVelocityY();
        final float velocityX = collisions.entityData.movement.getVelocityX();
        
        //System.out.println( "collisions: " + collisions );
        
        if ( velocityX != 0 ) {
            if ( velocityY != 0 ) {
                collisions.entityData.transform.move( 0f, -velocityY );
                collisions.update();
            }
            
            resolveXAxisCollision( collisions, velocityX );
            collisions.entityData.transform.move( 0f, velocityY );
        }
        
        if ( velocityY != 0) {
            collisions.update();
            resolveYAxisCollision( collisions, velocityY );
        }
    }
    
    private void resolveXAxisCollision( Collisions collisions, float velocityX ) {
        
        int xCorrection = 0;
        
        for ( CollisionData collision : collisions ) {
            final Rectangle bounds = collision.intersectionBounds;
            if ( bounds.y > 10 ) {
                continue;
            }
            
            if ( collision.intersectionMask == null ) {
                if ( bounds.width > xCorrection ) {
                    xCorrection = bounds.width;
                }
            }
            
            //System.out.println( "collision: " + collision );
        }

        if ( xCorrection != 0 ) {
            collisions.entityData.transform.setXpos( 
                ( velocityX < 0 )? 
                    (float) Math.floor( collisions.entityData.transform.getXpos() ) + xCorrection : 
                        (float) Math.ceil( collisions.entityData.transform.getXpos() ) - xCorrection 
            );
        }
    }
    
    

    private void resolveYAxisCollision( Collisions collisions, float velocityY ) {
        if ( velocityY >= 0 ) {
            return;
        }

        int yCorrection = 0;
        for ( CollisionData collision : collisions ) {
            final Rectangle bounds = collision.intersectionBounds;
            if ( bounds.width > 2 && bounds.height > yCorrection ) {
                yCorrection = bounds.height;
            }
        }
        
        if ( yCorrection != 0 ) {
            collisions.entityData.transform.setYpos( 
                ( velocityY < 0 )? 
                    (float) Math.floor( collisions.entityData.transform.getYpos() ) + yCorrection : 
                        (float) Math.ceil( collisions.entityData.transform.getYpos() ) - yCorrection 
            );
        }
    }
 
}
