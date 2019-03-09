/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess;

import pri.zzq.threechess.def.PieceState;


/**
 *
 * @author zzQ
 */
public class ChessLine {
    private int id;
    private ChessPieces[] pieces = new ChessPieces[3];
    
    private boolean isFull = false;

    public ChessLine(int id) {
        this.id = id;
    }

    public void set(ChessPieces p) {
        if (isFull) {
            return;
        }
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] == null) {
                pieces[i] = p;
                p.addLine(this);
                isFull = i == pieces.length - 1;
                return;
            }
        }
    }

    public boolean exi(ChessPieces p) {
        boolean flag = false;
        for (ChessPieces piece : pieces) {
            if (piece.equals(p)) {
                flag = true;
            }
        }
        return flag;
    }
    
    public boolean can() {
        return !PieceState.none.equals(pieces[0].getState()) && pieces[0].getState().equals(pieces[1].getState()) && pieces[0].getState().equals(pieces[2].getState());
    }


    public ChessPieces[] getPieces() {
        return pieces;
    }

    @Override
    public String toString() {
        return pieces[0].toString() + "," + pieces[1].toString() + "," + pieces[2].toString();
    }

    public boolean isIsFull() {
        return isFull;
    }

}
