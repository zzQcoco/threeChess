/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.Arrays;

/**
 *
 * @author zzQ
 */
@Serializable
public class ShowMessage extends AbstractMessage{
    private int[] pieces;

    public int[] getPieces() {
        return pieces;
    }

    public void setPieces(int[] pieces) {
        this.pieces = pieces;
    }

    @Override
    public String toString() {
        return "ShowMessage{" + "pieces=" + Arrays.toString(pieces) + '}';
    }
    
}
