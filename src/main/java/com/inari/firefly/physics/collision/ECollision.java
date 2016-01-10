package com.inari.firefly.physics.collision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;

public final class ECollision extends EntityComponent {
    
    public static final EntityComponentTypeKey<ECollision> TYPE_KEY = EntityComponentTypeKey.create( ECollision.class );
    
    public static final AttributeKey<Rectangle> BOUNDING = new AttributeKey<Rectangle>( "bounding", Rectangle.class, ECollision.class );
    public static final AttributeKey<Integer> PIXEL_PERFECT_ID = new AttributeKey<Integer>( "pixelPerfectBody", Integer.class, ECollision.class );
    public static final AttributeKey<int[]> COLLISION_LAYERS = new AttributeKey<int[]>( "collisionLayers", int[].class, ECollision.class );
    
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        PIXEL_PERFECT_ID,
        BOUNDING,
        COLLISION_LAYERS
    };
    
    int pixelPerfectId;
    Rectangle bounding;
    int[] collisionLayers;

    ECollision(  ) {
        super( TYPE_KEY );
        resetAttributes();
    }
    
    @Override
    public final void resetAttributes() {
        pixelPerfectId = -1;
        bounding = null;
    }

    public final int getPixelPerfectId() {
        return pixelPerfectId;
    }

    public final void setPixelPerfectId( int pixelPerfectId ) {
        this.pixelPerfectId = pixelPerfectId;
    }
    
    public final Rectangle getBounding() {
        return bounding;
    }

    public final void setBounding( Rectangle bounding ) {
        this.bounding = bounding;
    }

    public final int[] getCollisionLayers() {
        return collisionLayers;
    }

    public final void setCollisionLayers( int[] collisionLayers ) {
        this.collisionLayers = collisionLayers;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        pixelPerfectId = attributes.getValue( PIXEL_PERFECT_ID, pixelPerfectId );
        bounding = attributes.getValue( BOUNDING, bounding );
        collisionLayers = attributes.getValue( COLLISION_LAYERS, collisionLayers );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( PIXEL_PERFECT_ID, pixelPerfectId );
        attributes.put( BOUNDING, bounding );
        attributes.put( COLLISION_LAYERS, collisionLayers );
    }

}
