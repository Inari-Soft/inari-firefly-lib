package com.inari.firefly.controller.view;

import com.inari.commons.geom.Position;
import com.inari.firefly.system.FFContext;

public interface CameraPivot {
    
    void init( FFContext context );
    
    Position getPivot();

}
