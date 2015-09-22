package com.inari.firefly.animation;

import com.inari.commons.geom.Easing;
import com.inari.commons.geom.Easing.Type;

public class EasingData {
    
    Easing.Type easingType;
    float startValue;
    float changeInValue;
    long duration;
    
    public EasingData( Type easingType, float startValue, float changeInValue, long duration ) {
        this.easingType = easingType;
        this.startValue = startValue;
        this.changeInValue = changeInValue;
        this.duration = duration;
    }

    public final Easing.Type getEasingType() {
        return easingType;
    }

    public final float getStartValue() {
        return startValue;
    }

    public final float getChangeInValue() {
        return changeInValue;
    }

    public final long getDuration() {
        return duration;
    }

}
