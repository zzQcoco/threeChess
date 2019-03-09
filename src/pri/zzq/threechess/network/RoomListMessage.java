/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.Arrays;
import pri.zzq.threechess.Room;

/**
 *
 * @author zzQ
 */
@Serializable
public class RoomListMessage extends AbstractMessage{
    private int[] roomIds;
    private String[] states;

    public int[] getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(int[] roomIds) {
        this.roomIds = roomIds;
    }

    public String[] getStates() {
        return states;
    }

    public void setStates(String[] states) {
        this.states = states;
    }

    @Override
    public String toString() {
        return "RoomListMessage{" + "roomIds=" + Arrays.toString(roomIds) + ", states=" + Arrays.toString(states) + '}';
    }
    
}
