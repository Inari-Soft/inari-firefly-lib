package com.inari.firefly.asset;

import com.inari.commons.geom.Direction;
import com.inari.commons.geom.Rectangle;

public final class AnimatedSpriteData {
    
    public final int stateId;
    public final long frameTime;
    public final Rectangle textureRegion;
    
    public AnimatedSpriteData( int stateId, long frameTime, Rectangle textureRegion ) {
        this.stateId = stateId;
        this.frameTime = frameTime;
        this.textureRegion = textureRegion;
    }
    
    public static final AnimatedSpriteData[] create( long frameTime, Rectangle startRegion, int steps, Direction direction ) {
        return create( -1, frameTime, startRegion, steps, direction );
    }
    
    public static final AnimatedSpriteData[] create( int stateId, long frameTime, Rectangle startRegion, int steps, Direction direction ) {
        AnimatedSpriteData[] result = new AnimatedSpriteData[ steps ];
        
        for ( int i = 0; i< steps; i++ ) {
            Rectangle region = getRegion( startRegion, i, direction );
            result[ i ] = new AnimatedSpriteData( stateId, frameTime, region );
        }
        
        return result;
    }

    private static Rectangle getRegion( Rectangle startRegion, int steps, Direction direction ) {
        Rectangle result = new Rectangle( startRegion );
        
        switch ( direction ) {
            case NORTH: 
                result.y -= steps * startRegion.height;
                return result;
            case SOUTH: {
                result.y += steps * startRegion.height;
                return result;
            }
            case WEST: 
                result.x -= steps * startRegion.width;
                return result;
            case EAST: {
                result.x += steps * startRegion.width;
                return result;
            }
            default :
                return result;
        }
    }
    
}
