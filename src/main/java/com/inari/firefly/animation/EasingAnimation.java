package com.inari.firefly.animation;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.geom.Easing;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;

public final class EasingAnimation extends FloatAnimation {
    
    public static final AttributeKey<Easing.Type> EASING_TYPE = new AttributeKey<Easing.Type>( "easingType", Easing.Type.class, EasingAnimation.class );
    public static final AttributeKey<Float> START_VALUE = new AttributeKey<Float>( "startValue", Float.class, EasingAnimation.class );
    public static final AttributeKey<Float> CHANGE_IN_VALUE = new AttributeKey<Float>( "changeInValue", Float.class, EasingAnimation.class );
    public static final AttributeKey<Long> DURATION = new AttributeKey<Long>( "duration", Long.class, EasingAnimation.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        EASING_TYPE,
        START_VALUE,
        CHANGE_IN_VALUE,
        DURATION
    };
    
    private Easing.Type easingType;
    private float startValue;
    private float changeInValue;
    private long duration;
    
    private boolean startValueSet = false;

    EasingAnimation( int id ) {
        super( id );
    }

    public final Easing.Type getEasingType() {
        return easingType;
    }

    public final void setEasingType( Easing.Type easingType ) {
        this.easingType = easingType;
    }

    public final float getStartValue() {
        return startValue;
    }

    public final void setStartValue( float startValue ) {
        this.startValue = startValue;
    }

    public final float getChangeInValue() {
        return changeInValue;
    }

    public final void setChangeInValue( float changeInValue ) {
        this.changeInValue = changeInValue;
    }

    public final long getDuration() {
        return duration;
    }

    public final void setDuration( long duration ) {
        this.duration = duration;
    }
    
    private long time;

    @Override
    public void update( long update ) {
        super.update( update );
        if ( active ) {
            time = update - startTime;
            
            if ( time > startTime + duration ) {
                if ( looping ) {
                    float temp = changeInValue;
                    changeInValue = startValue;
                    startValue = temp;
                    startTime = time;
                } else {
                    active = false;
                }
            }
        }
    }

    @Override
    public final float getValue( int componentId, float currentValue ) {
        if ( !startValueSet ) {
            startValue = currentValue;
            startValueSet = true;
        }

        return easingType.calc( time , startValue, changeInValue, duration );
    }
    
    @Override
    public Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }

    @Override
    public void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        easingType = attributes.getValue( EASING_TYPE, easingType );
        if ( attributes.contains( START_VALUE) ) {
            startValue = attributes.getValue( START_VALUE );
            startValueSet = true;
        }
        changeInValue = attributes.getValue( CHANGE_IN_VALUE, changeInValue );
        duration = attributes.getValue( DURATION, duration );
    }

    @Override
    public void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( EASING_TYPE, easingType );
        attributes.put( START_VALUE, startValue );
        attributes.put( CHANGE_IN_VALUE, changeInValue );
        attributes.put( DURATION, duration );
    }

}
