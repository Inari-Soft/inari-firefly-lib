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

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.renderer.sprite.ESprite;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class SpriteIdAnimationController extends EntityController {
    
    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        ESprite.SPRITE_ID
    };
    
    public static final AttributeKey<Integer> SPRITE_ID_ANIMATION_ID = new AttributeKey<Integer>( "spriteAnimationId", Integer.class, SpriteIdAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        SPRITE_ID_ANIMATION_ID,
    };
    
    private int spriteAnimationId;
    
    SpriteIdAnimationController( int id, FFContext context ) {
        super( id, context );
        
        spriteAnimationId = -1;
    }

    public final int getSpriteAnimationId() {
        return spriteAnimationId;
    }

    public final void setSpriteAnimationId( int spriteAnimationId ) {
        this.spriteAnimationId = spriteAnimationId;
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
        
        spriteAnimationId = attributes.getValue( SPRITE_ID_ANIMATION_ID, spriteAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( SPRITE_ID_ANIMATION_ID, spriteAnimationId );
    } 

    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        if ( !entitySystem.isActive( entityId ) ) {
            System.out.println( "stop in debugger" );
        }
        ESprite sprite = entitySystem.getComponent( entityId, COMPONENT_ID_ESPRITE );

        if ( spriteAnimationId >= 0 ) {
            sprite.setSpriteId( animationSystem.getValue( spriteAnimationId, entityId, sprite.getSpriteId() ) );
        }
    }

}
