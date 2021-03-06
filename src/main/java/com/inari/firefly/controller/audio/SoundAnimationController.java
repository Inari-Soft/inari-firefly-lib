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
package com.inari.firefly.controller.audio;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.audio.Sound;
import com.inari.firefly.audio.SoundController;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.physics.animation.Animation;
import com.inari.firefly.physics.animation.AnimationSystem;
import com.inari.firefly.system.external.FFAudio;


public final class SoundAnimationController extends SoundController {
    
    public static final AttributeKey<String> VOLUME_ANIMATION_NAME = new AttributeKey<String>( "volumeAnimationName", String.class, SoundAnimationController.class );
    public static final AttributeKey<Integer> VOLUME_ANIMATION_ID = new AttributeKey<Integer>( "volumeAnimationId", Integer.class, SoundAnimationController.class );
    public static final AttributeKey<String> PITCH_ANIMATION_NAME = new AttributeKey<String>( "pitchAnimationNamed", String.class, SoundAnimationController.class );
    public static final AttributeKey<Integer> PITCH_ANIMATION_ID = new AttributeKey<Integer>( "pitchAnimationId", Integer.class, SoundAnimationController.class );
    public static final AttributeKey<String> PAN_ANIMATION_NAME = new AttributeKey<String>( "panAnimationName", String.class, SoundAnimationController.class );
    public static final AttributeKey<Integer> PAN_ANIMATION_ID = new AttributeKey<Integer>( "panAnimationId", Integer.class, SoundAnimationController.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        VOLUME_ANIMATION_ID,
        PITCH_ANIMATION_ID,
        PAN_ANIMATION_ID
    };
    
    private AnimationSystem animationSystem;
    private FFAudio audio;
    
    private int volumeAnimationId = -1;
    private int pitchAnimationId = -1;
    private int panAnimationId = -1;

    SoundAnimationController( int id ) {
        super( id );
    }

    @Override
    public final void init() {
        super.init();
        
        animationSystem = context.getSystem( AnimationSystem.SYSTEM_KEY );
        audio = context.getAudio();
    }

    public final int getVolumeAnimationId() {
        return volumeAnimationId;
    }

    public final void setVolumeAnimationId( int volumeAnimationId ) {
        this.volumeAnimationId = volumeAnimationId;
    }

    public final int getPitchAnimationId() {
        return pitchAnimationId;
    }

    public final void setPitchAnimationId( int pitchAnimationId ) {
        this.pitchAnimationId = pitchAnimationId;
    }

    public final int getPanAnimationId() {
        return panAnimationId;
    }

    public final void setPanAnimationId( int panAnimationId ) {
        this.panAnimationId = panAnimationId;
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
        
        volumeAnimationId = attributes.getIdForName( VOLUME_ANIMATION_NAME, VOLUME_ANIMATION_ID, Animation.TYPE_KEY, volumeAnimationId );
        pitchAnimationId = attributes.getIdForName( PITCH_ANIMATION_NAME, PITCH_ANIMATION_ID, Animation.TYPE_KEY, pitchAnimationId );
        panAnimationId = attributes.getIdForName( PAN_ANIMATION_NAME, PAN_ANIMATION_ID, Animation.TYPE_KEY, panAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( VOLUME_ANIMATION_ID, volumeAnimationId );
        attributes.put( PITCH_ANIMATION_ID, pitchAnimationId );
        attributes.put( PAN_ANIMATION_ID, panAnimationId );
    }

    @Override
    public final void update( final Sound sound ) {
        if ( volumeAnimationId >= 0 ) {
            float volume = sound.getVolume();
            float newVolume = animationSystem.getValue( volumeAnimationId, sound.index(), volume );
            if ( newVolume != volume ) {
                sound.setVolume( newVolume );
            }
        } else {
            volumeAnimationId = -1;
        }

        if ( pitchAnimationId >= 0 ) {
            float pitch = sound.getPitch();
            float newPitch = animationSystem.getValue( pitchAnimationId, sound.index(), pitch );
            if ( newPitch != pitch ) {
                sound.setPitch( newPitch );
            }
        } else {
            pitchAnimationId = -1;
        }

        if ( panAnimationId >= 0 ) {
            float pan = sound.getPan();
            float newPan = animationSystem.getValue( panAnimationId, sound.index(), pan );
            if ( newPan != pan ) {
                sound.setPan( newPan );
            }
        } else {
            panAnimationId = -1;
        }
        
        if ( volumeAnimationId >= 0 || pitchAnimationId >= 0 || panAnimationId >= 0 ) {
            if ( sound.isStreaming() ) {
                audio.changeMusic( sound.getSoundId(), sound.getVolume(), sound.getPan() );
            } else {
                audio.changeSound( sound.getSoundId(), sound.getInstanceId(), sound.getVolume(), sound.getPitch(), sound.getPan() );
            }
        }
    }

}
