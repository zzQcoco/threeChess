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
public class BackMessage extends AbstractMessage{
    public int roomId;
    public int playerId;
    public int level;

    @Override
    public String toString() {
        return "BackMessage{" + "roomId=" + roomId + ", playerId=" + playerId + ", level=" + level + '}';
    }
    
}
