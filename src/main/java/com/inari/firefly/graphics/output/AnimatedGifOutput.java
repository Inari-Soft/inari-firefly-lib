package com.inari.firefly.graphics.output;

import com.inari.commons.geom.Rectangle;
import com.inari.commons.graphics.AnimatedGifEncoder;
import com.inari.firefly.FFInitException;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.PostRenderEvent;
import com.inari.firefly.system.PostRenderEventListener;
import com.inari.firefly.system.external.FFTimer;

public final class AnimatedGifOutput implements PostRenderEventListener {
    
    
    private Rectangle area = null;
    private long startTime = -1;
    private int frameDelay = -1;
    private int frames = -1;
    private String fileName;
    
    private boolean running = false;
    private boolean started = false;
    private int frameNumber = 0;
    private long lastFrameTime = -1;
    
    private AnimatedGifEncoder animatedGifEncoder;

    public final Rectangle getArea() {
        return area;
    }

    public final void setArea( Rectangle area ) {
        this.area = area;
    }

    public final long getStartTime() {
        return startTime;
    }

    public final void setStartTime( long startTime ) {
        this.startTime = startTime;
    }

    public final int getFrameDelay() {
        return frameDelay;
    }

    public final void setFrameDelay( int frameDelay ) {
        this.frameDelay = frameDelay;
    }

    public final int getFrames() {
        return frames;
    }

    public final void setFrames( int frames ) {
        this.frames = frames;
    }

    public final String getFileName() {
        return fileName;
    }

    public final void setFileName( String fileName ) {
        this.fileName = fileName;
    }

    public final void run( FFContext context ) {
        if ( running ) {
            return;
        }
        
        if ( fileName == null ) {
            throw new FFInitException( "fileName is not defined" );
        }
        
        if ( area == null ) {
            area = new Rectangle( 0, 0, context.getGraphics().getScreenWidth(), context.getGraphics().getScreenHeight() );
        }
        if ( frameDelay < 0 ) {
            frameDelay = 1000;
        }
        
        if ( frames < 0 ) {
            frames = 10;
        }
        
        animatedGifEncoder = new AnimatedGifEncoder()
            .setDelay( frameDelay )
            .setRepeat( 0 )
            .setTransparent( null )
            .setSize( area.width, area.height );
        
        context.registerListener( PostRenderEvent.class, this );
        
        running = true;
    }
    
    public final void stop( FFContext context ) {
        if ( !running ) {
            return;
        }
        
        animatedGifEncoder.finish();
        animatedGifEncoder = null;
        running = false;
        started = false;
        frameNumber = 0;
        lastFrameTime = -1;
        context.disposeListener( PostRenderEvent.class, this );
    }

    @Override
    public final void postRendering( FFContext context ) {
        FFTimer timer = context.getTimer();
        if ( !started ) {
            if ( timer.getTime() < startTime ) {
                return;
            }
            animatedGifEncoder.start( fileName );
            createFrame( context, timer.getTime() );
            started = true;
            return;
        }
        
        if ( frameNumber >= frames ) {
            stop( context );
            return;
        }
        
        if ( timer.getTime() - lastFrameTime >= frameDelay ) {
            createFrame( context, timer.getTime() );
        }
        
    }

    

    private void createFrame( FFContext context, long time ) {
        frameNumber++;
        lastFrameTime = time;
        animatedGifEncoder.addFrame( context.getGraphics().getScreenshotPixels( area ) );
    }

}
