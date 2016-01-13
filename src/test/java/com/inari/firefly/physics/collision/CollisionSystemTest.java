package com.inari.firefly.physics.collision;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.EventDispatcherTestLog;
import com.inari.firefly.FireFlyMock;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.physics.movement.MovementSystem;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.external.FFTimer;

public class CollisionSystemTest {
    
    private static final Rectangle WORLD_BOUNDS = new Rectangle( 0, 0, 100, 100 );
    
    @Test
    public void testSimpleCollision() {
        EventDispatcherTestLog eventLog = new EventDispatcherTestLog();
        FireFlyMock firefly = new FireFlyMock( eventLog );
        FFContext context = firefly.getContext();
        
        EntitySystem entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        MovementSystem movementSystem = context.getSystem( MovementSystem.SYSTEM_KEY );
        CollisionSystem collisionSystem = context.getSystem( CollisionSystem.SYSTEM_KEY );
        
        FFTimer timer = context.getTimer();
        UpdateEvent updateEvent = new UpdateEvent( timer );
        
        collisionSystem.getCollisionQuadTreeBuilder()
            .set( CollisionQuadTree.VIEW_ID, 0 )
            .set( CollisionQuadTree.LAYER_ID, 0 )
            .set( CollisionQuadTree.MAX_ENTRIES_OF_AREA, 10 )
            .set( CollisionQuadTree.MAX_LEVEL, 5 )
            .set( CollisionQuadTree.WORLD_AREA, WORLD_BOUNDS )
        .build();
            
        entitySystem.getEntityBuilder()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.XPOSITION, 10 )
            .set( ETransform.YPOSITION, 10 )
            .set( ECollision.BOUNDING, new Rectangle( 0, 0, 10, 10 ) )
            .set( EMovement.VELOCITY_X, 3f )
        .activateAndNext()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.XPOSITION, 30 )
            .set( ETransform.YPOSITION, 10 )
            .set( ECollision.BOUNDING, new Rectangle( 0, 0, 10, 10 ) )
        .activate();
        
        assertEquals( 
            "EventLog [events=["
            + "ViewEvent [eventType=VIEW_CREATED, view=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2}, types={ETransform,ECollision} ]]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( updateEvent );
        
        assertEquals( 
            "EventLog [events=["
            + "ViewEvent [eventType=VIEW_CREATED, view=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2}, types={ETransform,ECollision} ]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( updateEvent );
        timer.tick();
        movementSystem.update( updateEvent );
        timer.tick();
        movementSystem.update( updateEvent );
        
        assertEquals( 
            "EventLog [events=["
            + "ViewEvent [eventType=VIEW_CREATED, view=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2}, types={ETransform,ECollision} ]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "CollisionEvent [movedEntityId=0, collidingEntityId=1, collisionIntersectionBounds=[x=8,y=0,width=2,height=10], collisionIntersectionMask=null]]]", 
            eventLog.toString() 
        );
    }

}
