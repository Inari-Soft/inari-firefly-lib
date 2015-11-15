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
package com.inari.firefly.movement;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.indexed.IndexedTypeAspectBuilder;
import com.inari.commons.lang.indexed.IndexedTypeSet;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.Entity;
import com.inari.firefly.entity.EntityComponent.EntityComponentTypeKey;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.movement.event.MoveEvent;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;

public final class MovementSystem implements FFContextInitiable, UpdateEventListener {
    
    public static final TypedKey<MovementSystem> CONTEXT_KEY = TypedKey.create( "FF_MOVEMENT_SYSTEM", MovementSystem.class );
    
    private final static AspectBitSet MOVEMENT_ASPECT = IndexedTypeAspectBuilder.build( EntityComponentTypeKey.class, EMovement.class );

    private IEventDispatcher eventDispatcher;
    private EntitySystem entitySystem;

    MovementSystem() {
    }
    
    @Override
    public void init( FFContext context ) {
        eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        entitySystem = context.getComponent( EntitySystem.CONTEXT_KEY );
        
        eventDispatcher.register( UpdateEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        eventDispatcher.unregister( UpdateEvent.class, this );
    }

    @Override
    public final void update( UpdateEvent event ) {
        MoveEvent moveEvent = new MoveEvent();
        for ( Entity entity : entitySystem.entities( MOVEMENT_ASPECT ) ) {
            IndexedTypeSet components = entitySystem.getComponents( entity.getId() );
            EMovement movement = components.get( EMovement.TYPE_KEY );
            if ( !movement.isMoving() ) {
                continue;
            }
            
            ETransform transform = components.get( ETransform.TYPE_KEY );
            transform.move( movement.getVelocityVector(), true );
            
            moveEvent.add( entity.getId() );
        }
        eventDispatcher.notify( moveEvent );
    }

}
