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
package com.inari.firefly.controller.entity;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.control.AnimatedEntityAttribute;
import com.inari.firefly.entity.EntityAttributeMap;
import com.inari.firefly.graphics.sprite.ESprite;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.physics.animation.IntAnimation;

public final class SpriteIdAnimationController extends AnimatedEntityAttribute {
    
    private AnimationSystem animationSystem;

    SpriteIdAnimationController( int id ) {
        super( id );
    }

    @Override
    public final void init() {
        super.init();
        
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
    }

    @Override
    public final AttributeKey<Integer> getControlledAttribute() {
        return ESprite.SPRITE_ID;
    }

    @Override
    public final void initEntity( EntityAttributeMap attributes ) {
        animationId = animationSystem.getAnimationId( animationResolverId, animationId );
        IntAnimation animation = animationSystem.getAnimationAs( animationId, IntAnimation.class );
        int value = animation.getInitValue();
        attributes.put( getControlledAttribute(), value );
    }

    @Override
    protected final void update( int entityId ) {
        updateAnimationId( animationSystem );
        ESprite sprite = context.getEntityComponent( entityId, ESprite.TYPE_KEY );
        sprite.setSpriteId( animationSystem.getValue( animationId, entityId, sprite.getSpriteId() ) );
    }

}
