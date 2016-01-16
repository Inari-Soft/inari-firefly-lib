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

import java.util.Iterator;

import com.inari.commons.geom.Orientation;
import com.inari.commons.geom.Vector2f;
import com.inari.commons.lang.IntIterator;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.indexed.IndexedTypeAspectBuilder;
import com.inari.commons.lang.indexed.IndexedTypeSet;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityComponent.EntityComponentTypeKey;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.component.SystemComponentBuilder;

public final class MovementSystem extends ComponentSystem<MovementSystem> implements UpdateEventListener {
    
    public static final FFSystemTypeKey<MovementSystem> SYSTEM_KEY = FFSystemTypeKey.create( MovementSystem.class );

    private static final SystemComponentKey<?>[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        VelocityVector.TYPE_KEY,
    };
    
    private final static AspectBitSet MOVEMENT_ASPECT = IndexedTypeAspectBuilder.build( EntityComponentTypeKey.class, EMovement.class );

    private FFContext context;
    private EntitySystem entitySystem;
    
    private final DynArray<VelocityVector> velocityVectors;
    private final MoveEvent moveEvent = new MoveEvent();
    private final Vector2f tmpVector = new Vector2f();

    MovementSystem() {
        super( SYSTEM_KEY );
        velocityVectors = new DynArray<VelocityVector>();
    }
    
    @Override
    public void init( FFContext context ) {
        this.context = context;
        
        entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        
        context.registerListener( UpdateEvent.class, this );
    }
    
    @Override
    public final void dispose( FFContext context ) {
        context.disposeListener( UpdateEvent.class, this );
    }

    @Override
    public final void update( UpdateEvent event ) {
        moveEvent.entityIds.clear();
        IntIterator entities = entitySystem.entities( MOVEMENT_ASPECT );
        while ( entities.hasNext() ) {
            int entityId = entities.next();
            IndexedTypeSet components = entitySystem.getComponents( entityId );
            EMovement movement = components.get( EMovement.TYPE_KEY );
            if ( !movement.active ) {
                continue;
            }
            
            calculateMovement( movement );
            if ( tmpVector.dx == 0 && tmpVector.dy == 0 ) {
                continue;
            }
            
            ETransform transform = components.get( ETransform.TYPE_KEY );
            transform.move( tmpVector.dx, tmpVector.dy, true );

            moveEvent.add( entityId );
        }
        context.notify( moveEvent );
    }
    
    public final VelocityVector getVelocityVector( int id ) {
        if ( !velocityVectors.contains( id ) ) {
            return null;
        }
        
        return velocityVectors.get( id );
    }
    
    public final VelocityVector getVelocityVector( String name ) {
        if ( name == null ) {
            return null;
        }
        
        for ( VelocityVector velocityVector : velocityVectors ) {
            if ( name.equals( velocityVector.getName() ) ) {
                return velocityVector;
            }
        }
        
        return null;
    }
    
    public final void deleteVelocityVector( int id ) {
        if ( !velocityVectors.contains( id ) ) {
            return;
        }
        
        velocityVectors.remove( id );
        
    }

    public void deleteVelocityVector( String name ) {
        if ( name == null ) {
            return;
        }
        
        for ( VelocityVector velocityVector : velocityVectors ) {
            if ( name.equals( velocityVector.getName() ) ) {
                velocityVectors.remove( velocityVector.getId() );
            }
        }
    }
    
    @Override
    public final void clear() {
        velocityVectors.clear();
    }

    public final VelocityVectorBuilder getVelocityVectorBuilder() {
        return new VelocityVectorBuilder();
    }
    
    @Override
    public final SystemComponentKey<?>[] supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final SystemBuilderAdapter<?>[] getSupportedBuilderAdapter() {
        return new SystemBuilderAdapter<?>[] {
            new VelocityVectorBuilderAdapter( this ),
        };
    }
    
    private final void calculateMovement( final EMovement movement ) {
        if ( movement.hasVelocityVectorIds() ) {
            
            movement.velocity.dx = 0;
            movement.velocity.dx = 0;
            
            for ( int i = 0; i < movement.velocityVectorIds.length(); i++ ) {
                if ( !movement.velocityVectorIds.contains( i ) ) {
                    continue;
                }
                
                final VelocityVector velocity = velocityVectors.get( movement.velocityVectorIds.get( i ) );
                movement.velocity.dx += velocity.vector.dx;
                movement.velocity.dy += velocity.vector.dy;
            }
        }
        
        if ( ( movement.velocity.dx < 0 && !movement.hasContact( Orientation.WEST ) ) || 
                ( movement.velocity.dx > 0 && !movement.hasContact( Orientation.EAST ) ) ) {
               tmpVector.dx = movement.velocity.dx;
           } else {
               tmpVector.dx = 0;
           }
           if ( ( movement.velocity.dy < 0 && !movement.hasContact( Orientation.NORTH ) ) || 
               ( movement.velocity.dy > 0 && !movement.hasContact( Orientation.SOUTH ) ) ) {
              tmpVector.dy = movement.velocity.dy;
          } else {
              tmpVector.dy = 0;
          }
    }
    
    
    
    public final class VelocityVectorBuilder extends SystemComponentBuilder {
        
        protected VelocityVectorBuilder() {}
        
        @Override
        public final SystemComponentKey<VelocityVector> systemComponentKey() {
            return VelocityVector.TYPE_KEY;
        }

        public int doBuild( int componentId, Class<?> componentType, boolean activate ) {
            VelocityVector vector = new VelocityVector( componentId );
            vector.fromAttributes( attributes );
            
            velocityVectors.set( vector.getId(), vector );
            
            return vector.getId();
        }
    }
    
    private final class VelocityVectorBuilderAdapter extends SystemBuilderAdapter<VelocityVector> {
        public VelocityVectorBuilderAdapter( MovementSystem system ) {
            super( system, new VelocityVectorBuilder() );
        }
        @Override
        public final SystemComponentKey<VelocityVector> componentTypeKey() {
            return VelocityVector.TYPE_KEY;
        }
        @Override
        public final VelocityVector getComponent( int id ) {
            return velocityVectors.get( id );
        }
        @Override
        public final Iterator<VelocityVector> getAll() {
            return velocityVectors.iterator();
        }
        @Override
        public final void deleteComponent( int id ) {
            deleteVelocityVector( id );
        }
        @Override
        public final void deleteComponent( String name ) {
            deleteVelocityVector( name );
        }
        @Override
        public final VelocityVector getComponent( String name ) {
            return getVelocityVector( name );
        }
    }

}
