package com.inari.firefly.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class PositionAnimationController extends EntityController {

    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        ETransform.XPOSITION,
        ETransform.YPOSITION
    };
    
    public static final AttributeKey<Integer> X_POS_ANIMATION_ID = new AttributeKey<Integer>( "xPosAnimationId", Integer.class, PositionAnimationController.class );
    public static final AttributeKey<Integer> Y_POS_ANIMATION_ID = new AttributeKey<Integer>( "yPosAnimationId", Integer.class, PositionAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        X_POS_ANIMATION_ID,
        Y_POS_ANIMATION_ID
    };

    private final AnimationSystem animationSystem;

    private int xPosAnimationId, yPosAnimationId;
    
    PositionAnimationController( int id, FFContext context ) {
        super( id, context );
        xPosAnimationId = -1;
        yPosAnimationId = -1;
        animationSystem = context.getComponent( FFContext.Systems.ANIMATION_SYSTEM );
    }

    public final int getxPosAnimationId() {
        return xPosAnimationId;
    }

    public final void setxPosAnimationId( int xPosAnimationId ) {
        this.xPosAnimationId = xPosAnimationId;
    }

    public final int getyPosAnimationId() {
        return yPosAnimationId;
    }

    public final void setyPosAnimationId( int yPosAnimationId ) {
        this.yPosAnimationId = yPosAnimationId;
    }

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
        
        xPosAnimationId = attributes.getValue( X_POS_ANIMATION_ID, xPosAnimationId );
        yPosAnimationId = attributes.getValue( Y_POS_ANIMATION_ID, yPosAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( X_POS_ANIMATION_ID, xPosAnimationId );
        attributes.put( Y_POS_ANIMATION_ID, yPosAnimationId );
    }


    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ETransform transform = entitySystem.getComponent( entityId, ETransform.COMPONENT_TYPE );

        if ( xPosAnimationId >= 0 && animationSystem.exists( xPosAnimationId ) ) {
            transform.setXpos( animationSystem.getValue( xPosAnimationId, entityId, transform.getXpos() ) );
        } else {
            xPosAnimationId = -1;
        }

        if ( yPosAnimationId >= 0 && animationSystem.exists( yPosAnimationId ) ) {
            transform.setYpos( animationSystem.getValue( yPosAnimationId, entityId, transform.getYpos() ) );
        } else {
            yPosAnimationId = -1;
        }
    }

}
