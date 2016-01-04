/*******************************************************************************
 * Copyright (c) 2015 - 2016, Andreas Hefti, inarisoft@yahoo.de 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/ 
package com.inari.firefly.physics.movement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Vector2f;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;

public final class EMovement extends EntityComponent {
    
    public static final EntityComponentTypeKey<EMovement> TYPE_KEY = EntityComponentTypeKey.create( EMovement.class );
    
    public static final AttributeKey<Float> VELOCITY_X = new AttributeKey<Float>( "dx", Float.class, EMovement.class );
    public static final AttributeKey<Float> VELOCITY_Y = new AttributeKey<Float>( "dy", Float.class, EMovement.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        VELOCITY_X,
        VELOCITY_Y
    };
    
    private final Vector2f velocityVector = new Vector2f( 0, 0 );

    public EMovement() {
        super( TYPE_KEY );
        resetAttributes();
    }

    @Override
    public final void resetAttributes() {
        setVelocityX( 0f );
        setVelocityY( 0f );
    }
    
    public final void setVelocityX( float velocityX ) {
        velocityVector.dx = velocityVector.dx;
    }
    
    public final float getVelocityX() {
        return velocityVector.dx;
    }
    
    public final void setVelocityY( float velocityY ) {
        velocityVector.dy = velocityVector.dy;
    }
    
    public final float getVelocityY() {
        return velocityVector.dy;
    }

    public final Vector2f getVelocityVector() {
        return velocityVector;
    }

    public final boolean isMoving() {
        return ( velocityVector.dx != 0 || velocityVector.dy != 0 );
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        velocityVector.dx = attributes.getValue( VELOCITY_X, velocityVector.dx );
        velocityVector.dy = attributes.getValue( VELOCITY_Y, velocityVector.dy );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( VELOCITY_X, velocityVector.dx );
        attributes.put( VELOCITY_Y, velocityVector.dy );
    }

}
