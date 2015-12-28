package com.inari.firefly.asset;

import com.inari.commons.geom.Rectangle;

public final class AnimatedSpriteData {
    
    private int stateId;
    private long animationTime;
    private Rectangle textureRegion;
    
    public final int getStateId() {
        return stateId;
    }
    
    public final void setStateId( int stateId ) {
        this.stateId = stateId;
    }
    
    public final long getAnimationTime() {
        return animationTime;
    }
    
    public final void setAnimationTime( long animationTime ) {
        this.animationTime = animationTime;
    }
    
    public final Rectangle getTextureRegion() {
        return textureRegion;
    }
    
    public final void setTextureRegion( Rectangle textureRegion ) {
        this.textureRegion = textureRegion;
    }

}
