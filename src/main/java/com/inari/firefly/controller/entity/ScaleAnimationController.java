package com.inari.firefly.controller.entity;

import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.ETransform;
import com.inari.firefly.entity.EntityController;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;

public class ScaleAnimationController extends EntityController {
    
    public static final AttributeKey<?>[] CONTROLLED_ATTRIBUTES = new AttributeKey[] {
        ETransform.SCALE_X,
        ETransform.SCALE_Y
    };
    
    public static final AttributeKey<Integer> SCALE_X_ANIMATION_ID = new AttributeKey<Integer>( "scaleXAnimationId", Integer.class, PositionAnimationController.class );
    public static final AttributeKey<Integer> SCALE_Y_ANIMATION_ID = new AttributeKey<Integer>( "scaleYAnimationId", Integer.class, PositionAnimationController.class );
    public static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        SCALE_X_ANIMATION_ID,
        SCALE_Y_ANIMATION_ID
    };

    private int scaleXAnimationId, scaleYAnimationId;
    
    ScaleAnimationController( int id, FFContext context ) {
        super( id, context );
        scaleXAnimationId = -1;
        scaleYAnimationId = -1;
    }

    public final int getScaleXAnimationId() {
        return scaleXAnimationId;
    }

    public final void setScaleXAnimationId( int scaleXAnimationId ) {
        this.scaleXAnimationId = scaleXAnimationId;
    }

    public final int getScaleYAnimationId() {
        return scaleYAnimationId;
    }

    public final void setScaleYAnimationId( int scaleYAnimationId ) {
        this.scaleYAnimationId = scaleYAnimationId;
    }
    
    @Override
    public final AttributeKey<?>[] getControlledAttribute() {
        return CONTROLLED_ATTRIBUTES;
    }
    
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        scaleXAnimationId = attributes.getValue( SCALE_X_ANIMATION_ID, scaleXAnimationId );
        scaleYAnimationId = attributes.getValue( SCALE_Y_ANIMATION_ID, scaleYAnimationId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( SCALE_X_ANIMATION_ID, scaleXAnimationId );
        attributes.put( SCALE_Y_ANIMATION_ID, scaleYAnimationId );
    }

    @Override
    protected final void update( final FFTimer timer, int entityId ) {
        ETransform transform = entitySystem.getComponent( entityId, COMPONENT_ID_ETRANSFORM );

        if ( scaleXAnimationId >= 0 && animationSystem.exists( scaleXAnimationId ) ) {
            transform.setScalex( animationSystem.getValue( scaleXAnimationId, entityId, transform.getScalex() ) );
        } else {
            scaleXAnimationId = -1;
        }

        if ( scaleYAnimationId >= 0 && animationSystem.exists( scaleYAnimationId ) ) {
            transform.setScaley( animationSystem.getValue( scaleYAnimationId, entityId, transform.getScaley() ) );
        } else {
            scaleYAnimationId = -1;
        }
    }

}
