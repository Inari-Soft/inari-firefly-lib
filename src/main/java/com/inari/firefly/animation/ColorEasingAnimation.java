package com.inari.firefly.animation;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.graphics.RGBColor;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFTimer;

public class ColorEasingAnimation extends ValueAnimation<RGBColor> {

    public static final AttributeKey<EasingData> EASING_DATA_RED = new AttributeKey<EasingData>( "easingDataRed", EasingData.class, EasingAnimation.class );
    public static final AttributeKey<EasingData> EASING_DATA_GREEN = new AttributeKey<EasingData>( "easingDataGreen", EasingData.class, EasingAnimation.class );
    public static final AttributeKey<EasingData> EASING_DATA_BLUE = new AttributeKey<EasingData>( "easingDataBlue", EasingData.class, EasingAnimation.class );
    public static final AttributeKey<EasingData> EASING_DATA_ALPHA = new AttributeKey<EasingData>( "easingDataAlpha", EasingData.class, EasingAnimation.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        EASING_DATA_RED
    };
    
    private EasingData easingDataRed;
    private EasingData easingDataGreen;
    private EasingData easingDataBlue;
    private EasingData easingDataAlpha;
    
    private long time;
    
    protected ColorEasingAnimation( int id ) {
        super( id );
        easingDataRed = null;
        easingDataGreen = null;
        easingDataBlue = null;
        easingDataAlpha = null;
    }
    
    @Override
    public final void update( FFTimer timer ) {
        super.update( timer );
        if ( active ) {
            time = timer.getTime() - startTime;
            active = update( easingDataRed ) || 
                     update( easingDataGreen ) || 
                     update( easingDataBlue ) || 
                     update( easingDataAlpha );
            finished = !active;
        }
    }
    
    private boolean update( EasingData easingData ) {
        if ( easingData == null ) {
            return false;
        }
        
        if ( time > startTime + easingData.duration ) {
            if ( looping ) {
                float temp = easingData.changeInValue;
                easingData.changeInValue = easingData.startValue;
                easingData.startValue = temp;
                startTime = time;
            } else {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public RGBColor getValue( int componentId, RGBColor currentValue ) {
        currentValue.r = calc( easingDataRed, currentValue.r );
        currentValue.g = calc( easingDataGreen, currentValue.g );
        currentValue.b = calc( easingDataBlue, currentValue.b );
        currentValue.a = calc( easingDataAlpha, currentValue.a );
        
        return currentValue;
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
        easingDataRed = attributes.getValue( EASING_DATA_RED, easingDataRed );
        easingDataGreen = attributes.getValue( EASING_DATA_GREEN, easingDataGreen );
        easingDataBlue = attributes.getValue( EASING_DATA_BLUE, easingDataBlue );
        easingDataAlpha = attributes.getValue( EASING_DATA_ALPHA, easingDataAlpha );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( EASING_DATA_RED, easingDataRed );
        attributes.put( EASING_DATA_GREEN, easingDataGreen );
        attributes.put( EASING_DATA_BLUE, easingDataBlue );
        attributes.put( EASING_DATA_ALPHA, easingDataAlpha );
    }
    

    private float calc( EasingData easingData, float currentValue ) {
        if ( easingData == null ) {
            return currentValue;
        }
        return easingData.easingType.calc( time , easingData.startValue, easingData.changeInValue, easingData.duration );
    }

}