package com.inari.firefly.animation.easing;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.geom.Easing;
import com.inari.firefly.animation.FloatAnimation;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.external.FFTimer;

public final class EasingAnimation extends FloatAnimation {
    
    public static final AttributeKey<EasingData> EASING_DATA = new AttributeKey<EasingData>( "easingData", EasingData.class, EasingAnimation.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        EASING_DATA
    };
    
    private EasingData easingData;

    EasingAnimation( int id ) {
        super( id );
    }

    public final Easing.Type getEasingType() {
        return easingData.easingType;
    }

    public final EasingData getEasingData() {
        return easingData;
    }

    public final void setEasingData( EasingData easingData ) {
        this.easingData = easingData;
    }

    @Override
    public final void update( FFTimer timer ) {
        if ( runningTime > easingData.duration ) {
            if ( looping ) {
                reset();
                startTime = timer.getTime();
                activate();
            } else {
                finish();
            }
        }
    }
    
    @Override
    public final float getInitValue() {
        return easingData.getStartValue();
    }

    @Override
    public final float getValue( int componentId, float currentValue ) {
        if ( !isActive() ) {
            return currentValue;
        }
        
        return easingData.easingType.calc( runningTime , easingData.startValue, easingData.changeInValue, easingData.duration );
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        easingData = attributes.getValue( EASING_DATA, easingData );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( EASING_DATA, easingData );
    }

}
