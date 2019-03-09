/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author zzQ
 */
@Serializable
public class StepMessage extends AbstractMessage {
    private int roomId;
    private int pieceId;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getPieceId() {
        return pieceId;
    }

    public void setPieceId(int pieceId) {
        this.pieceId = pieceId;
    }

    @Override
    public String toString() {
        return "StepMessage{" + "roomId=" + roomId + ", pieceId=" + pieceId + '}';
    }
    
}
