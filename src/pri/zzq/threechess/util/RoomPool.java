/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.util;

import java.util.concurrent.atomic.AtomicInteger;
import pri.zzq.threechess.Room;

/**
 *
 * @author zzQ
 */
public class RoomPool extends Pool<Room>{

    private AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    protected Room newObject() {
        Room room = new Room();
        return tag(room);
    }
    
    private Room tag(Room room){
        room.setId(counter.addAndGet(1));
        return room;
    }

    @Override
    public Room obtain() {
        return tag(super.obtain());
    }
    
    
    
}
