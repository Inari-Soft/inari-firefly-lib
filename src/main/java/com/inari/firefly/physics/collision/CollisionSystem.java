package com.inari.firefly.physics.collision;

import java.util.Iterator;

import com.inari.commons.GeomUtils;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.IntIterator;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityActivationEvent;
import com.inari.firefly.entity.EntityActivationListener;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.physics.movement.MoveEvent;
import com.inari.firefly.physics.movement.MoveEventListener;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.view.ViewEvent;
import com.inari.firefly.system.view.ViewEventListener;
import com.inari.firefly.system.view.ViewEvent.Type;
import com.inari.firefly.system.component.SystemComponentBuilder;

public final class CollisionSystem 
    extends 
        ComponentSystem<CollisionSystem> 
    implements 
        EntityActivationListener, 
        ViewEventListener,
        MoveEventListener {
    
    public static final FFSystemTypeKey<CollisionSystem> SYSTEM_KEY = FFSystemTypeKey.create( CollisionSystem.class );

    private static final SystemComponentKey<?>[] SUPPORTED_COMPONENT_TYPES = new SystemComponentKey[] {
        BitMask.TYPE_KEY,
        CollisionQuadTree.TYPE_KEY
    };
    
    private final DynArray<BitMask> bitmasks;
    private final DynArray<CollisionQuadTree> quadTrees;
    private final DynArray<DynArray<CollisionQuadTree>> quadTreesPerViewAndLayer;
    
    private EntitySystem entitySystem;
    private TileGridSystem tileGridSystem;
    
    private final Rectangle tmpCollisionBounds1 = new Rectangle();
    private final Rectangle tmpCollisionBounds2 = new Rectangle();
    private final CollisionEvent collisionEvent = new CollisionEvent();

    CollisionSystem() {
        super( SYSTEM_KEY );
        bitmasks = new DynArray<BitMask>();
        quadTrees = new DynArray<CollisionQuadTree>();
        quadTreesPerViewAndLayer = new DynArray<DynArray<CollisionQuadTree>>();
    }
    
    @Override
    public final void init( FFContext context ) {
        super.init( context );
        entitySystem = context.getSystem( EntitySystem.SYSTEM_KEY );
        tileGridSystem = context.getSystem( TileGridSystem.SYSTEM_KEY );
        
        context.registerListener( EntityActivationEvent.class, this );
        context.registerListener( ViewEvent.class, this );
        context.registerListener( MoveEvent.class, this );
    }

    @Override
    public final void dispose( FFContext context ) {
        clear();
        context.disposeListener( EntityActivationEvent.class, this );
        context.disposeListener( ViewEvent.class, this );
        context.disposeListener( MoveEvent.class, this );
    }
    
    @Override
    public final void onViewEvent( ViewEvent event ) {
        int viewId = event.view.getId();
        if ( event.eventType == Type.VIEW_DELETED ) {
            quadTreesPerViewAndLayer.remove( viewId );
            return;
        }
    }

    @Override
    public final void onEntityActivationEvent( EntityActivationEvent event ) {
        ETransform transform = entitySystem.getComponent( event.entityId, ETransform.TYPE_KEY );
        int viewId = transform.getViewId();
        int layerId = transform.getLayerId();
        if ( !quadTreesPerViewAndLayer.contains( viewId ) ) {
            return;
        }
        
        DynArray<CollisionQuadTree> quadTreesPerView = quadTreesPerViewAndLayer.get( viewId );
        if ( !quadTreesPerView.contains( layerId ) ) {
            return;
        }
        CollisionQuadTree quadTree = quadTreesPerView.get( layerId );
        if ( quadTree == null ) {
            return;
        }
        
        switch ( event.eventType ) {
            case ENTITY_ACTIVATED: {
                quadTree.add( event.entityId );
                break;
            }
            case ENTITY_DEACTIVATED: {
                quadTree.remove( event.entityId );
                break;
            }
        }
    }
    
    @Override
    public final void onMoveEvent( final MoveEvent event ) {
        IntIterator movedEntiyIterator = event.movedEntityIds();
        while ( movedEntiyIterator.hasNext() ) {
            final int entityId = movedEntiyIterator.next();
            final AspectBitSet aspect = entitySystem.getAspect( entityId );
            if ( !aspect.contains( ECollision.TYPE_KEY ) ) {
                continue;
            }
            
            final ETransform transform1 = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
            final ECollision collision1 = entitySystem.getComponent( entityId, ECollision.TYPE_KEY );
            final int bitmaskId1 = collision1.getBitmaskId();
            final int x1 = (int) transform1.getXpos();
            final int y1 = (int) transform1.getYpos();
            final int viewId = transform1.getViewId();
            final int layerId = transform1.getLayerId();
            
            setTmpBounds( tmpCollisionBounds1, 0, 0, collision1.bounding.width, collision1.bounding.height );
            checkTileCollision( entityId, bitmaskId1, x1, y1, viewId, layerId );
            checkSpriteCollision( entityId, bitmaskId1, x1, y1, viewId, layerId );
            
            if ( collision1.collisionLayers != null ) {
                final IntIterator iterator = collision1.collisionLayers.iterator();
                while ( iterator.hasNext() ) {
                    final int layerId2 = iterator.next();
                    if ( layerId2 == layerId ) {
                        continue;
                    }
                    checkTileCollision( entityId, bitmaskId1, x1, y1, viewId, layerId2 );
                    checkSpriteCollision( entityId, bitmaskId1, x1, y1, viewId, layerId2 );
                }
             }
        }
    }

    private void checkSpriteCollision( final int entityId, final int bitmaskId1, final int x1, final int y1, final int viewId, final int layerId ) {
        if ( !quadTreesPerViewAndLayer.contains( viewId ) ) {
            return;
        }
        
        DynArray<CollisionQuadTree> ofLayer = quadTreesPerViewAndLayer.get( viewId );
        if ( !ofLayer.contains( layerId ) ) {
            return;
        }
        
        CollisionQuadTree quadTree = ofLayer.get( layerId );
        IntIterator entityIterator = quadTree.get( entityId );
        if ( entityIterator == null || !entityIterator.hasNext() ) {
            return;
        }
        
        while ( entityIterator.hasNext() ) {
            final int entity2Id = entityIterator.next();
            if ( entityId == entity2Id ) {
                continue;
            }
            
            final ETransform transform2 = entitySystem.getComponent( entity2Id, ETransform.TYPE_KEY );
            final ECollision collision2 = entitySystem.getComponent( entity2Id, ECollision.TYPE_KEY );
            
            checkCollision( 
                (int) transform2.getXpos() - x1, (int) transform2.getYpos() - y1,
                collision2.bounding.width, collision2.bounding.height,
                bitmaskId1, collision2.getBitmaskId(),
                entityId, entity2Id
            );
        }
    }

    private final void checkTileCollision( final int entityId, final int bitmaskId1, final int x1, final int y1, final int viewId, final int layerId ) {
        TileGrid tileGrid = tileGridSystem.getTileGrid( viewId, layerId );
        if ( tileGrid == null ) {
            return;
        }
        
        TileIterator tileIterator = tileGrid.iterator( tmpCollisionBounds1 );
        if ( tileIterator == null || !tileIterator.hasNext() ) {
            return;
        }
        
        while ( tileIterator.hasNext() ) {
            int tileId = tileIterator.next();
            if ( !entitySystem.getAspect( tileId ).contains( ECollision.TYPE_KEY ) ) {
                continue;
            }
            
            final ECollision collision2 = entitySystem.getComponent( entityId, ECollision.TYPE_KEY );
            
            checkCollision( 
                (int) tileIterator.getWorldXPos() - x1, (int) tileIterator.getWorldYPos() - y1,
                collision2.bounding.width, collision2.bounding.height,
                bitmaskId1, collision2.getBitmaskId(),
                entityId, tileId
            );
        }
    }
    
    private final void checkCollision( final int x2, final int y2, final int width2, final int heigh2, final int bitmaskId1, final int bitmaskId2, final int entityId1, final int entityId2 ) {
        setTmpBounds( tmpCollisionBounds2, x2, y2, width2, heigh2 );
        if ( !GeomUtils.intersect( tmpCollisionBounds1, tmpCollisionBounds2 ) ) {
            return;
        }
        
        GeomUtils.intersection( tmpCollisionBounds1, tmpCollisionBounds2, collisionEvent.collisionIntersectionBounds );

        if ( bitmaskId1 < 0 && bitmaskId2 < 0 ) {
            notify( entityId1, entityId2, null );
            return;
        }
        
        BitMask bitmask1 = ( bitmaskId1 < 0 )? null : bitmasks.get( bitmaskId1 );
        BitMask bitmask2 = ( bitmaskId2 < 0 )? null : bitmasks.get( bitmaskId2 );
        
        if ( bitmask1 != null && bitmask2 != null && bitmask2.intersects( x2, y2, bitmask1 ) ) {
            notify( entityId1, entityId2, bitmask2 );
            return;
        }
        
        if ( bitmask1 == null && bitmask2 != null && bitmask2.intersects( tmpCollisionBounds2 ) ) {
            notify( entityId1, entityId2, bitmask2 );
            return;
        }
        
        if ( bitmask1 != null && bitmask2 == null && bitmask1.intersects( -x2, -y2, tmpCollisionBounds1 ) ) {
            notify( entityId1, entityId2, bitmask2 );
            return;
        }
    }

    private void notify( final int entityId1, final int entityId2, final BitMask intersection ) {
        collisionEvent.movedEntityId = entityId1;
        collisionEvent.collidingEntityId = entityId2;
        collisionEvent.collisionIntersectionMask = ( intersection != null )? intersection.intersectionMask: null;
        context.notify( collisionEvent );
    }
    
    private final void setTmpBounds( final Rectangle bounds, float x, float y, final int width, final int height ) {
        bounds.x = (int) x;
        bounds.y = (int) y;
        bounds.width = width;
        bounds.height = height;
    }
    
    public final BitMask getBitMask( int bitMaskId ) {
        if ( !bitmasks.contains( bitMaskId ) ) {
            return null;
        }
        return bitmasks.get( bitMaskId );
    }
    
    public final int getBitMaskId( String name ) {
        for ( BitMask bitmask : bitmasks ) {
            if ( bitmask.getName().equals( name ) ) {
                return bitmask.getId();
            }
        }
        return -1;
    }
    
    public final BitMask getBitMask( String name ) {
        for ( BitMask bitmask : bitmasks ) {
            if ( bitmask.getName().equals( name ) ) {
                return bitmask;
            }
        }
        return null;
    }
    
    public final void deleteBitMask( int bitMaskId ) {
        bitmasks.remove( bitMaskId );
    }
    
    public final void deleteBitMask( String name ) {
        int bitmaskId = getBitMaskId( name );
        if ( bitmaskId < 0 ) {
            return;
        }
        bitmasks.remove( bitmaskId );
    }
    
    public final CollisionQuadTree getCollisionQuadTree( int id ) {
        return quadTrees.get( id );
    }
    
    public final CollisionQuadTree getCollisionQuadTree( String name ) {
        for ( CollisionQuadTree quadTree : quadTrees ) {
            if ( name.equals( quadTree.getName() ) ) {
                return quadTree;
            }
        }
        return null;
    }
    
    public final void deleteCollisionQuadTree( int id ) {
        CollisionQuadTree quadTree = getCollisionQuadTree( id );
        if ( quadTree == null ) {
            return;
        }
        quadTrees.remove( quadTree.getLayerId() );
        quadTreesPerViewAndLayer.get( quadTree.getViewId() ).remove( quadTree.getLayerId() );
    }
    
    public final void deleteCollisionQuadTree( String name ) {
        CollisionQuadTree quadTree = getCollisionQuadTree( name );
        if ( quadTree == null ) {
            return;
        }
        quadTrees.remove( quadTree.getLayerId() );
        quadTreesPerViewAndLayer.get( quadTree.getViewId() ).remove( quadTree.getLayerId() );
    }
    
    @Override
    public final boolean match( AspectBitSet aspect ) {
        return aspect.contains( ECollision.TYPE_KEY ) && !aspect.contains( ETile.TYPE_KEY );
    }

    @Override
    public final SystemComponentKey<?>[] supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final SystemBuilderAdapter<?>[] getSupportedBuilderAdapter() {
        return new SystemBuilderAdapter<?>[] {
            new BitMaskBuilderAdapter( this ),
            new CollisionQuadTreeBuilderAdapter( this )
        };
    }
    
    public final BitMaskBuilder getBitMaskBuilder() {
        return new BitMaskBuilder();
    }
    
    public final CollisionQuadTreeBuilder getCollisionQuadTreeBuilder() {
        return new CollisionQuadTreeBuilder();
    }

    @Override
    public final void clear() {
        bitmasks.clear();
        quadTrees.clear();
        quadTreesPerViewAndLayer.clear();
    }
    
    public final class BitMaskBuilder extends SystemComponentBuilder {
        
        protected BitMaskBuilder() {}
        
        @Override
        public final SystemComponentKey<BitMask> systemComponentKey() {
            return BitMask.TYPE_KEY;
        }

        public int doBuild( int componentId, Class<?> componentType, boolean activate ) {
            BitMask bitmask = new BitMask( componentId );
            bitmask.fromAttributes( attributes );
            
            bitmasks.set( bitmask.getId(), bitmask );
            
            return bitmask.getId();
        }
    }
    
    public final class CollisionQuadTreeBuilder extends SystemComponentBuilder {
        
        protected CollisionQuadTreeBuilder() {}
        
        @Override
        public final SystemComponentKey<CollisionQuadTree> systemComponentKey() {
            return CollisionQuadTree.TYPE_KEY;
        }

        public int doBuild( int componentId, Class<?> componentType, boolean activate ) {
            CollisionQuadTree quadTree = new CollisionQuadTree( componentId, context );
            quadTree.fromAttributes( attributes );
            
            int viewId = quadTree.getViewId();
            int layerId = quadTree.getLayerId();
            
            if ( viewId < 0 ) {
                throw new FFInitException( "ViewId is mandatory for CollisionQuadTree" );
            }
            
            if ( layerId < 0 ) {
                throw new FFInitException( "LayerId is mandatory for CollisionQuadTree" );
            }
            
            if ( quadTree.getWorldArea() == null ) {
                throw new FFInitException( "WorldArea is mandatory for CollisionQuadTree" );
            }
            
            if ( !quadTreesPerViewAndLayer.contains( viewId ) ) {
                quadTreesPerViewAndLayer.set( viewId, new DynArray<CollisionQuadTree>() );
            }
            
            quadTrees.set( quadTree.getId(), quadTree );
            quadTreesPerViewAndLayer
                .get( viewId )
                .set( layerId, quadTree );
            
            return quadTree.getId();
        }
    }

    private final class BitMaskBuilderAdapter extends SystemBuilderAdapter<BitMask> {
        public BitMaskBuilderAdapter( CollisionSystem system ) {
            super( system, new BitMaskBuilder() );
        }
        @Override
        public final SystemComponentKey<BitMask> componentTypeKey() {
            return BitMask.TYPE_KEY;
        }
        @Override
        public final BitMask getComponent( int id ) {
            return bitmasks.get( id );
        }
        @Override
        public final Iterator<BitMask> getAll() {
            return bitmasks.iterator();
        }
        @Override
        public final void deleteComponent( int id ) {
            deleteBitMask( id );
        }
        @Override
        public final void deleteComponent( String name ) {
            deleteBitMask( name );
            
        }
        @Override
        public final BitMask getComponent( String name ) {
            return getBitMask( name );
        }
    }

    private final class CollisionQuadTreeBuilderAdapter extends SystemBuilderAdapter<CollisionQuadTree> {
        public CollisionQuadTreeBuilderAdapter( CollisionSystem system ) {
            super( system, new CollisionQuadTreeBuilder() );
        }
        @Override
        public final SystemComponentKey<CollisionQuadTree> componentTypeKey() {
            return CollisionQuadTree.TYPE_KEY;
        }
        @Override
        public final CollisionQuadTree getComponent( int id ) {
            return getCollisionQuadTree( id );
        }
        @Override
        public final Iterator<CollisionQuadTree> getAll() {
            return quadTrees.iterator();
        }
        @Override
        public final void deleteComponent( int id ) {
            deleteCollisionQuadTree( id );
        }
        @Override
        public final void deleteComponent( String name ) {
            deleteCollisionQuadTree( name );
            
        }
        @Override
        public final CollisionQuadTree getComponent( String name ) {
            return getCollisionQuadTree( name );
        }
    }

}