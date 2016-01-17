package com.inari.firefly.physics.collision;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.EventDispatcherTestLog;
import com.inari.firefly.FireFlyMock;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.physics.movement.MovementSystem;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.external.FFTimer;

public class SimpleCollisionTest {

    private static final Rectangle WORLD_BOUNDS = new Rectangle( 0, 0, 100, 100 );
    
    private final static EventDispatcherTestLog eventLog = new EventDispatcherTestLog();
    private final static FireFlyMock firefly = new FireFlyMock( eventLog );
    private final static FFContext context = firefly.getContext();
    
    private final static EntitySystem entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
    private final static MovementSystem movementSystem = context.getSystem( MovementSystem.SYSTEM_KEY );
    private final static CollisionSystem collisionSystem = context.getSystem( CollisionSystem.SYSTEM_KEY );
    private final static TileGridSystem tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
    
    @Before
    public void init() {
        entitySystem.deleteAll();
        collisionSystem.clear();
        tileGridSystem.clear();
        eventLog.clearLog();
    }
    
    @Test
    public void testSimpleCollision() {

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
            .set( EMovement.ACTIVE, true )
        .activateAndNext()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.XPOSITION, 30 )
            .set( ETransform.YPOSITION, 10 )
            .set( ECollision.BOUNDING, new Rectangle( 0, 0, 10, 10 ) )
        .activate();
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2}, types={ETransform,ECollision} ]]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( updateEvent );
        
        assertEquals( 
            "EventLog [events=["
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
    
    @Test
    public void testSimpleTileCollision() {
        
        FFTimer timer = context.getTimer();
        UpdateEvent updateEvent = new UpdateEvent( timer );
        
        tileGridSystem.getTileGridBuilder()
            .set( TileGrid.CELL_WIDTH, 16 )
            .set( TileGrid.CELL_HEIGHT, 16 )
            .set( TileGrid.WORLD_XPOS, 0 )
            .set( TileGrid.WORLD_YPOS, 0 )
            .set( TileGrid.WIDTH, 10 )
            .set( TileGrid.HEIGHT, 10 )
            .set( TileGrid.VIEW_ID, 0 )
            .set( TileGrid.LAYER_ID, 0 )
        .build();

        entitySystem.getEntityBuilder()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.XPOSITION, 10 )
            .set( ETransform.YPOSITION, 10 )
            .set( ECollision.BOUNDING, new Rectangle( 0, 0, 10, 10 ) )
            .set( EMovement.VELOCITY_X, 3f )
            .set( EMovement.ACTIVE, true )
        .activateAndNext()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETile.GRID_X_POSITION, 2 )
            .set( ETile.GRID_Y_POSITION, 1 )
            .set( ECollision.BOUNDING, new Rectangle( 0, 0, 16, 16 ) )
        .activate();
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2, 5}, types={ETransform,ECollision,ETile} ]]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( updateEvent );
        timer.tick();
        movementSystem.update( updateEvent );
        timer.tick();
        movementSystem.update( updateEvent );
        timer.tick();
        movementSystem.update( updateEvent );
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={0, 1, 2}, types={EMovement,ETransform,ECollision} ]], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1, aspect=IndexedAspect [ indexedBaseType=EntityComponentTypeKey, bitset={1, 2, 5}, types={ETransform,ECollision,ETile} ]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "CollisionEvent [movedEntityId=0, collidingEntityId=1, collisionIntersectionBounds=[x=10,y=6,width=0,height=4], collisionIntersectionMask=null]]]", 
            eventLog.toString()
        );
    }

}
