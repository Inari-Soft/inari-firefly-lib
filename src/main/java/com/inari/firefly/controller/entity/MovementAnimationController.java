/*******************************************************************************
 * Copyright (c) 2015, Andreas Hefti, inarisoft@yahoo.de 
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
package com.inari.firefly.controller.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Vector2f;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.movement.EMovement;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class MovementAnimationController extends EntityController {
    
    protected final int COMPONENT_ID_EMOVEMENT = Indexer.getIndexForType( EMovement.class, EntityComponent.class );
    
    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        EMovement.VELOCITY_X,
        EMovement.VELOCITY_Y
    };
    
    public static final AttributeKey<Integer> VELOCITY_X_ANIMATION_ID = new AttributeKey<Integer>( "velocityXAnimationId", Integer.class, MovementAnimationController.class );
    public static final AttributeKey<Integer> VELOCITY_Y_ANIMATION_ID = new AttributeKey<Integer>( "velocityYAnimationId", Integer.class, MovementAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        VELOCITY_X_ANIMATION_ID,
        VELOCITY_Y_ANIMATION_ID
    };

    private int velocityXAnimationId, velocityYAnimationId;

    MovementAnimationController( int id, FFContext context ) {
        super( id, context );
        velocityXAnimationId = -1;
        velocityYAnimationId = -1;
    }

    public final int getVelocityXAnimationId() {
        return velocityXAnimationId;
    }

    public final void setVelocityXAnimationId( int velocityXAnimationId ) {
        this.velocityXAnimationId = velocityXAnimationId;
    }

    public final int getVelocityYAnimationId() {
        return velocityYAnimationId;
    }

    public final void setVelocityYAnimationId( int velocityYAnimationId ) {
        this.velocityYAnimationId = velocityYAnimationId;
    }
    
    @Override
    public final AttributeKey<?>[] getControlledAttribute() {
        return CONTROLLED_ATTRIBUTES;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) ) );
        return attributeKeys;
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        velocityXAnimationId = attributes.getValue( VELOCITY_X_ANIMATION_ID, velocityXAnimationId );
        velocityYAnimationId = attributes.getValue( VELOCITY_Y_ANIMATION_ID, velocityYAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( VELOCITY_X_ANIMATION_ID, velocityXAnimationId );
        attributes.put( VELOCITY_Y_ANIMATION_ID, velocityYAnimationId );
    }


    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        EMovement movement = entitySystem.getComponent( entityId, COMPONENT_ID_EMOVEMENT );
        Vector2f velocityVector = movement.getVelocityVector();

        if ( velocityXAnimationId >= 0 && animationSystem.exists( velocityXAnimationId ) ) {
            velocityVector.dx = animationSystem.getValue( velocityXAnimationId, entityId, velocityVector.dx );
        } else {
            velocityXAnimationId = -1;
            velocityVector.dx = 0;
        }

        if ( velocityYAnimationId >= 0 && animationSystem.exists( velocityYAnimationId ) ) {
            velocityVector.dy = animationSystem.getValue( velocityYAnimationId, entityId, velocityVector.dy );
        } else {
            velocityYAnimationId = -1;
            velocityVector.dy = 0;
        }
    }

}