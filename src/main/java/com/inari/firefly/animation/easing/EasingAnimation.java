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
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        EASING_DATA
    };
    
    private EasingData easingData;
    
    private boolean startValueSet = false;
    private long time;

    EasingAnimation( int id ) {
        super( id );
    }

    public final Easing.Type getEasingType() {
        return easingData.easingType;
    }

    public final void setEasingType( Easing.Type easingType ) {
        easingData.easingType = easingType;
    }

    public final float getStartValue() {
        return easingData.startValue;
    }

    public final void setStartValue( float startValue ) {
        easingData.startValue = startValue;
    }

    public final float getChangeInValue() {
        return easingData.changeInValue;
    }

    public final void setChangeInValue( float changeInValue ) {
        easingData.changeInValue = changeInValue;
    }

    public final long getDuration() {
        return easingData.duration;
    }

    public final void setDuration( long duration ) {
        easingData.duration = duration;
    }

    @Override
    public final void update( FFTimer timer ) {
        super.update( timer );
        if ( active ) {
            time = timer.getTime() - startTime;
            
            if ( time > startTime + easingData.duration ) {
                if ( looping ) {
                    float temp = easingData.changeInValue;
                    easingData.changeInValue = easingData.startValue;
                    easingData.startValue = temp;
                    startTime = time;
                } else {
                    active = false;
                    finished = true;
                }
            }
        }
    }

    @Override
    public final float getValue( int componentId, float currentValue ) {
        if ( !startValueSet ) {
            easingData.startValue = currentValue;
            startValueSet = true;
        }

        return easingData.easingType.calc( time , easingData.startValue, easingData.changeInValue, easingData.duration );
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
