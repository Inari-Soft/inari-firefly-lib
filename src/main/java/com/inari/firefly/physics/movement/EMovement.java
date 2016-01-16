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
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Orientation;
import com.inari.commons.geom.Vector2f;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;

public final class EMovement extends EntityComponent {
    
    public static final EntityComponentTypeKey<EMovement> TYPE_KEY = EntityComponentTypeKey.create( EMovement.class );
    
    public static final AttributeKey<Boolean> ACTIVE = new AttributeKey<Boolean>( "active", Boolean.class, EMovement.class );
    public static final AttributeKey<Float> VELOCITY_X = new AttributeKey<Float>( "dx", Float.class, EMovement.class );
    public static final AttributeKey<Float> VELOCITY_Y = new AttributeKey<Float>( "dy", Float.class, EMovement.class );
    public static final AttributeKey<IntBag> VELOCITY_VECTOR_IDS = new AttributeKey<IntBag>( "velocityVectorIds", IntBag.class, EMovement.class );
    
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        ACTIVE,
        VELOCITY_X,
        VELOCITY_Y,
        VELOCITY_VECTOR_IDS
    };
    
    boolean active;
    final Vector2f velocity = new Vector2f( 0, 0 );
    IntBag velocityVectorIds;
    
    final BitSet contact;

    public EMovement() {
        super( TYPE_KEY );
        contact = new BitSet( 5 );
        resetAttributes();
    }

    @Override
    public final void resetAttributes() {
        active = false;
        setVelocityX( 0f );
        setVelocityY( 0f );
        velocityVectorIds = null;
        contact.clear();
    }

    public final boolean isActive() {
        return active;
    }

    public final void setActive( boolean active ) {
        this.active = active;
    }
    
    public final void setVelocityX( float velocityX ) {
        velocity.dx = velocityX;
    }

    public final float getVelocityX() {
        return velocity.dx;
    }
    
    public final void setVelocityY( float velocityY ) {
        velocity.dy = velocityY;
    }
    
    public final float getVelocityY() {
        return velocity.dy;
    }
    
    public final boolean hasVelocityVectorIds() {
        return velocityVectorIds != null && !velocityVectorIds.isEmpty();
    }

    public final IntBag getVelocityVectorIds() {
        return velocityVectorIds;
    }

    public final void setVelocityVectorIds( IntBag velocityVectorIds ) {
        this.velocityVectorIds = velocityVectorIds;
    }

    public final void addVelocityVectorIds( int vectorId ) {
        if ( velocityVectorIds == null ) {
            velocityVectorIds = new IntBag( 5, -1 );
        }
        velocityVectorIds.add( vectorId );
    }

    public final void setContact( Orientation orientation ) {
        if ( orientation == Orientation.NONE ) {
            return;
        }
        
        contact.set( orientation.ordinal() );
    }
    
    public final void removeContact( Orientation orientation ) {
        if ( orientation == Orientation.NONE ) {
            return;
        }
        
        contact.set( orientation.ordinal(), false );
    }
    
    public final void clearContact() {
        contact.clear();
    }
    
    public final boolean hasContact( Orientation orientation ) {
        if ( orientation == Orientation.NONE ) {
            return false;
        }
        
        return contact.get( orientation.ordinal() ) == true;
    }

    public final boolean isMoving() {
        return active && ( velocity.dx != 0 || velocity.dy != 0 );
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        active = attributes.getValue( ACTIVE, active );
        velocity.dx = attributes.getValue( VELOCITY_X, velocity.dx );
        velocity.dy = attributes.getValue( VELOCITY_Y, velocity.dy );
        velocityVectorIds = attributes.getValue( VELOCITY_VECTOR_IDS, velocityVectorIds );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( ACTIVE, active );
        attributes.put( VELOCITY_X, velocity.dx );
        attributes.put( VELOCITY_Y, velocity.dy );
        attributes.put( VELOCITY_VECTOR_IDS, velocityVectorIds );
    }

}
