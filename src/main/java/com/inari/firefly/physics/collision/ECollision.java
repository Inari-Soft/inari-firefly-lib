package com.inari.firefly.physics.collision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Position;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;

public final class ECollision extends EntityComponent {
    
    public static final EntityComponentTypeKey<ECollision> TYPE_KEY = EntityComponentTypeKey.create( ECollision.class );
    
    public static final AttributeKey<TileRegionBody> TILE_REGION_BODY = new AttributeKey<TileRegionBody>( "tileRegionBody", TileRegionBody.class, ECollision.class );
    public static final AttributeKey<Integer> PIXEL_PERFECT_ID = new AttributeKey<Integer>( "pixelPerfectBody", Integer.class, ECollision.class );
    public static final AttributeKey<Rectangle> RECTANGLE_BODY = new AttributeKey<Rectangle>( "rectangleBody", Rectangle.class, ECollision.class );
    public static final AttributeKey<Position[]> CHECK_POINTS = new AttributeKey<Position[]>( "checkPoints", Position[].class, ECollision.class );
    
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] { 
        TILE_REGION_BODY,
        PIXEL_PERFECT_ID,
        RECTANGLE_BODY,
        CHECK_POINTS
    };
    
    private TileRegionBody tileRegionBody;
    private int pixelPerfectId;
    private Rectangle rectangleBody;
    private Position[] checkPoints;

    protected ECollision(  ) {
        super( TYPE_KEY );
        resetAttributes();
    }
    
    @Override
    public final void resetAttributes() {
        tileRegionBody = TileRegionBody.NONE;
        pixelPerfectId = -1;
        rectangleBody = null;
        checkPoints = null;
    }

    public final TileRegionBody getTileRegionBody() {
        return tileRegionBody;
    }

    public final void setTileRegionBody( TileRegionBody tileRegionBody ) {
        this.tileRegionBody = tileRegionBody;
    }

    public final int getPixelPerfectId() {
        return pixelPerfectId;
    }

    public final void setPixelPerfectId( int pixelPerfectId ) {
        this.pixelPerfectId = pixelPerfectId;
    }

    public final Rectangle getRectangleBody() {
        return rectangleBody;
    }

    public final void setRectangleBody( Rectangle rectangleBody ) {
        this.rectangleBody = rectangleBody;
    }

    public final Position[] getCheckPoints() {
        return checkPoints;
    }

    public final void setCheckPoints( Position[] checkPoints ) {
        this.checkPoints = checkPoints;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return new HashSet<AttributeKey<?>>( Arrays.asList( ATTRIBUTE_KEYS ) );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        tileRegionBody = attributes.getValue( TILE_REGION_BODY, tileRegionBody );
        pixelPerfectId = attributes.getValue( PIXEL_PERFECT_ID, pixelPerfectId );
        rectangleBody = attributes.getValue( RECTANGLE_BODY, rectangleBody );
        checkPoints = attributes.getValue( CHECK_POINTS, checkPoints );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( TILE_REGION_BODY, tileRegionBody );
        attributes.put( PIXEL_PERFECT_ID, pixelPerfectId );
        attributes.put( RECTANGLE_BODY, rectangleBody );
        attributes.put( CHECK_POINTS, checkPoints );
    }

}
