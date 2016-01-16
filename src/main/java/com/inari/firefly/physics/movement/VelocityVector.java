package com.inari.firefly.physics.movement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Vector2f;
import com.inari.commons.lang.indexed.IIndexedTypeKey;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.component.SystemComponent;

public final class VelocityVector extends SystemComponent {
    
    public static final SystemComponentKey<VelocityVector> TYPE_KEY = SystemComponentKey.create( VelocityVector.class );
    
    public static final AttributeKey<Float> VELOCITY_X = new AttributeKey<Float>( "dx", Float.class, VelocityVector.class );
    public static final AttributeKey<Float> VELOCITY_Y = new AttributeKey<Float>( "dy", Float.class, VelocityVector.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        VELOCITY_X,
        VELOCITY_Y
    };
    
    final Vector2f vector = new Vector2f();

    protected VelocityVector( int id ) {
        super( id );
    }

    public final void setVelocityX( float velocityX ) {
        vector.dx = velocityX;
    }

    public final float getVelocityX() {
        return vector.dx;
    }
    
    public final void setVelocityY( float velocityY ) {
        vector.dy = velocityY;
    }
    
    public final float getVelocityY() {
        return vector.dy;
    }



    @Override
    public final IIndexedTypeKey indexedTypeKey() {
        return TYPE_KEY;
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        vector.dx = attributes.getValue( VELOCITY_X, vector.dx );
        vector.dy = attributes.getValue( VELOCITY_Y, vector.dy );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( VELOCITY_X, vector.dx );
        attributes.put( VELOCITY_Y, vector.dy );
    }

}
