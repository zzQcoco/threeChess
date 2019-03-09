/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import pri.zzq.threechess.def.PieceState;

/**
 *
 * @author zzQ
 */
public class ChessPieces {
    private int id;
    private PieceState state = PieceState.none;
    private LinkedList<ChessLine> lines = new LinkedList<>();
    
    private int r;
    private int x;
    private int y;
    
    public void addLine(ChessLine line) {
        lines.add(line);
    }
    
    public ChessPieces(int id) {
        this.id = id;
        pars();
    }
    
    private void pars(){
        r = id >>> 4;
        x = id >>> 2 & 3;
        y = id & 3;
    }
    
    public boolean isAdjacent(ChessPieces piece){
        int dis = Math.abs(piece.r - r) + Math.abs(piece.x - x) + Math.abs(piece.y - y);
        return dis == 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PieceState getState() {
        return state;
    }

    public void setState(PieceState state) {
        this.state = state;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public ChessPieces[] adjacent() {
        List<ChessPieces> adjacents = new ArrayList<>(4);
        for (ChessLine line : lines) {
            ChessPieces[] ps = line.getPieces();
            for (ChessPieces p : ps) {
                if (this.isAdjacent(p)) {
                    adjacents.add(p);
                    break;
                }
            }
        }
        return adjacents.toArray(new ChessPieces[adjacents.size()]);
    }

    @Override
    public String toString() {
        return "ChessPieces-" + id;
    }
    
    
}
