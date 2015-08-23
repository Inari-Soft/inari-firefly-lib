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
package com.inari.firefly.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.animation.AnimationSystem;
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
//    public static final AttributeKey<Integer> TINT_RED_ANIMATION_ID = new AttributeKey<Integer>( "tintRedAnimationId", Integer.class, SpriteIdAnimationController.class );
//    public static final AttributeKey<Integer> TINT_GREEN_ANIMATION_ID = new AttributeKey<Integer>( "tintGreenAnimationId", Integer.class, SpriteIdAnimationController.class );
//    public static final AttributeKey<Integer> TINT_BLUE_ANIMATION_ID = new AttributeKey<Integer>( "tintBlueAnimationId", Integer.class, SpriteIdAnimationController.class );
//    public static final AttributeKey<Integer> TINT_ALPHA_ANIMATION_ID = new AttributeKey<Integer>( "tintAlphaAnimationId", Integer.class, SpriteIdAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        SPRITE_ID_ANIMATION_ID,
//        TINT_RED_ANIMATION_ID,
//        TINT_GREEN_ANIMATION_ID,
//        TINT_BLUE_ANIMATION_ID,
//        TINT_ALPHA_ANIMATION_ID
    };

    private final AnimationSystem animationSystem;
    
    private int spriteAnimationId;
    
    SpriteIdAnimationController( int id, FFContext context ) {
        super( id, context );
        animationSystem = context.getComponent( FFContext.Systems.ANIMATION_SYSTEM );
        
        spriteAnimationId = -1;
    }

    public final int getSpriteAnimationId() {
        return spriteAnimationId;
    }

    public final void setSpriteAnimationId( int spriteAnimationId ) {
        this.spriteAnimationId = spriteAnimationId;
    }

//    public final int getTintRedAnimationId() {
//        return tintRedAnimationId;
//    }
//
//    public final void setTintRedAnimationId( int tintRedAnimationId ) {
//        this.tintRedAnimationId = tintRedAnimationId;
//    }
//
//    public final int getTintGreenAnimationId() {
//        return tintGreenAnimationId;
//    }
//
//    public final void setTintGreenAnimationId( int tintGreenAnimationId ) {
//        this.tintGreenAnimationId = tintGreenAnimationId;
//    }
//
//    public final int getTintBlueAnimationId() {
//        return tintBlueAnimationId;
//    }
//
//    public final void setTintBlueAnimationId( int tintBlueAnimationId ) {
//        this.tintBlueAnimationId = tintBlueAnimationId;
//    }
//
//    public final int getTintAlphaAnimationId() {
//        return tintAlphaAnimationId;
//    }
//
//    public final void setTintAlphaAnimationId( int tintAlphaAnimationId ) {
//        this.tintAlphaAnimationId = tintAlphaAnimationId;
//    }
    
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
//        tintRedAnimationId = attributes.getValue( TINT_RED_ANIMATION_ID, tintRedAnimationId );
//        tintGreenAnimationId = attributes.getValue( TINT_GREEN_ANIMATION_ID, tintGreenAnimationId );
//        tintBlueAnimationId = attributes.getValue( TINT_BLUE_ANIMATION_ID, tintBlueAnimationId );
//        tintAlphaAnimationId = attributes.getValue( TINT_ALPHA_ANIMATION_ID, tintAlphaAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( SPRITE_ID_ANIMATION_ID, spriteAnimationId );
//        attributes.put( TINT_RED_ANIMATION_ID, tintRedAnimationId );
//        attributes.put( TINT_GREEN_ANIMATION_ID, tintGreenAnimationId );
//        attributes.put( TINT_BLUE_ANIMATION_ID, tintBlueAnimationId );
//        attributes.put( TINT_ALPHA_ANIMATION_ID, tintAlphaAnimationId );
    } 

    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ESprite sprite = entitySystem.getComponent( entityId, ESprite.COMPONENT_TYPE );

        if ( spriteAnimationId >= 0 && animationSystem.exists( spriteAnimationId ) ) {
            sprite.setSpriteId( animationSystem.getValue( spriteAnimationId, entityId, sprite.getSpriteId() ) );
        } else {
            spriteAnimationId = -1;
        }

//        if ( tintRedAnimationId >= 0 && animationSystem.exists( tintRedAnimationId ) ) {
//            tintColor.r = animationSystem.getValue( tintRedAnimationId, entityId, tintColor.r );
//        } else {
//            tintRedAnimationId = -1;
//        }
//
//        if ( tintGreenAnimationId >= 0 && animationSystem.exists( tintGreenAnimationId ) ) {
//            tintColor.g = animationSystem.getValue( tintGreenAnimationId, entityId, tintColor.r );
//        } else {
//            tintGreenAnimationId = -1;
//        }
//
//        if ( tintBlueAnimationId >= 0 && animationSystem.exists( tintBlueAnimationId ) ) {
//            tintColor.b = animationSystem.getValue( tintBlueAnimationId, entityId, tintColor.r );
//        } else {
//            tintBlueAnimationId = -1;
//        }
//
//        if ( tintAlphaAnimationId >= 0 && animationSystem.exists( tintAlphaAnimationId ) ) {
//            tintColor.a = animationSystem.getValue( tintAlphaAnimationId, entityId, tintColor.r );
//        } else {
//            tintAlphaAnimationId = -1;
//        }
    }

}
