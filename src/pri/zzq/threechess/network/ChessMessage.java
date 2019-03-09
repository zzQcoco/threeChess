/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.HashMap;
import java.util.Map;
import pri.zzq.threechess.def.GameProcess;
import pri.zzq.threechess.def.PieceState;

/**
 *
 * @author zzQ
 */
@Serializable
public class ChessMessage extends AbstractMessage {
    private int roomId;
    private Map<Integer, String> pieceStateMap = new HashMap<>(24);
    private String process;
    private String nextStep;
    private String win;
    private int white, black;
    private int whiteRmCount, blackRmCount;
    private int whiteExchange, blackExchange;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public Map<Integer, String> getPieceStateMap() {
        return pieceStateMap;
    }

    public void setPieceStateMap(Map<Integer, String> pieceStateMap) {
        this.pieceStateMap = pieceStateMap;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public String getWin() {
        return win;
    }

    public void setWin(String win) {
        this.win = win;
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(int white) {
        this.white = white;
    }

    public int getBlack() {
        return black;
    }

    public void setBlack(int black) {
        this.black = black;
    }

    public int getWhiteRmCount() {
        return whiteRmCount;
    }

    public void setWhiteRmCount(int whiteRmCount) {
        this.whiteRmCount = whiteRmCount;
    }

    public int getBlackRmCount() {
        return blackRmCount;
    }

    public void setBlackRmCount(int blackRmCount) {
        this.blackRmCount = blackRmCount;
    }

    public int getWhiteExchange() {
        return whiteExchange;
    }

    public void setWhiteExchange(int whiteExchange) {
        this.whiteExchange = whiteExchange;
    }

    public int getBlackExchange() {
        return blackExchange;
    }

    public void setBlackExchange(int blackExchange) {
        this.blackExchange = blackExchange;
    }

    @Override
    public String toString() {
        return "ChessMessage{" + "roomId=" + roomId + ", pieceStateMap=" + pieceStateMap + ", process=" + process + ", nextStep=" + nextStep + ", win=" + win + ", white=" + white + ", black=" + black + ", whiteRmCount=" + whiteRmCount + ", blackRmCount=" + blackRmCount + ", whiteExchange=" + whiteExchange + ", blackExchange=" + blackExchange + '}';
    }
    
    
}
