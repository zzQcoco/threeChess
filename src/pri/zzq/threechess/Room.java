/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess;

import pri.zzq.threechess.def.GameProcess;
import pri.zzq.threechess.util.Pool;

/**
 *
 * @author zzQ
 */
public class Room implements Pool.Poolable {
    
    public enum State {
        Waiting, Playing;
    }
    
    private int id;
    private Player master, other;
    private final ChessBoard board;
    private State state = State.Waiting;
    
    public Room(){
        board = new ChessBoard();
    }
    
    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Player getMaster() {
        return master;
    }
    
    public void join(Player player) {
        this.other = player;
    }
    
    public void setMaster(Player player){
        this.master = player;
    }

    public Player getOther() {
        return other;
    }

    public void setOther(Player other) {
        this.other = other;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    
    public Player getPlayer(int id){
        if (master != null && master.getId() == id) {
            return master;
        }
        if (other != null && other.getId() == id) {
            return other;
        }
        return null;
    }
    
    public void step(int pieceId){
        if (board != null && board.getProcess() != GameProcess.end) {
            board.Playing(pieceId);
        }
    }
    
    public ChessBoard getBoard() {
        return this.board;
    }
    
    @Override
    public void reset() {
        board.clear();
        id = 0;
        master = null;
        other = null;
    }

}
