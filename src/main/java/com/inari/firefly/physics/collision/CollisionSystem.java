package com.inari.firefly.physics.collision;

import java.util.Iterator;

import com.inari.commons.lang.IntIterator;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.FFInitException;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.entity.event.EntityActivationEvent;
import com.inari.firefly.entity.event.EntityActivationListener;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.graphics.tile.TileGridSystem;
import com.inari.firefly.physics.movement.event.MoveEvent;
import com.inari.firefly.physics.movement.event.MoveEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.component.SystemComponentBuilder;
import com.inari.firefly.system.view.event.ViewEvent;
import com.inari.firefly.system.view.event.ViewEvent.Type;
import com.inari.firefly.system.view.event.ViewEventListener;

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
    public final void onMoveEvent( MoveEvent event ) {
        IntIterator movedEntiyIterator = event.entityIds.iterator();
        // TODO
        
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
