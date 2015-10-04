package com.inari.firefly.app;

import com.inari.firefly.Disposable;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;

public interface FFApplicationManager extends FFContextInitiable, Disposable {

    void handlePause( FFContext context );
    
    void handleResume( FFContext context );

}
