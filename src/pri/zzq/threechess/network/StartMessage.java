/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import pri.zzq.threechess.def.PlayerType;

/**
 *
 * @author zzQ
 */
@Serializable
public class StartMessage extends AbstractMessage{
    private int roomId;
    private int playerId;
    private String type; 

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StartMessage{" + "roomId=" + roomId + ", playerId=" + playerId + ", type=" + type + '}';
    }
    
}
