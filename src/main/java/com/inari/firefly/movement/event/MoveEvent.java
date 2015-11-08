/*******************************************************************************
 * Copyright (c) 2015, Andreas Hefti, inarisoft@yahoo.de 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/ 
package com.inari.firefly.movement.event;

import com.inari.commons.event.Event;
import com.inari.commons.lang.list.IntBag;

public final class MoveEvent extends Event<MoveEventListener> {
    
    public final IntBag entityIds = new IntBag( 100, -1 );
    
    public void add( int entityId ) {
        entityIds.add( entityId );
    }

    @Override
    public final void notify( MoveEventListener listener ) {
        listener.onMoveEvent( this );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "MoveEvent [entityIds=" );
        builder.append( entityIds );
        builder.append( "]" );
        return builder.toString();
    }

}