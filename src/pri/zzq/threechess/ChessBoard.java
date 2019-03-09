/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess;

import com.jme3.math.FastMath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import pri.zzq.threechess.def.GameProcess;
import pri.zzq.threechess.def.PieceState;

/**
 *
 * @author zzQ
 */
public class ChessBoard {
    
    private static final Logger LOG = Logger.getLogger(ChessBoard.class.getName());
    
    private Map<Integer, ChessLine> lineMap = new HashMap<>(20);
    private Map<Integer, ChessPieces> piecesMap = new HashMap<>(24);
    
    private List<ChessLine> completeLines = new ArrayList<>(5);
    
    private GameProcess process = GameProcess.Under;
    private PieceState nextStep = PieceState.none;
    private PieceState win = PieceState.none;
    
    private int white = 12, black = 12;
    private int whiteRmCount = 0, blackRmCount = 0;
    private int whiteExchange = 0, blackExchange = 0;
    
    private ChessPieces upPieces;
    
    private boolean invalid = false;
    
    public ChessBoard() {
        draw();
    }
    
    private void draw() {
        drawLine();
        drawPiece();
    }
    
    public void start() {
        this.nextStep = FastMath.rand.nextBoolean() ? PieceState.black : PieceState.white;
    }
    
    private void drawLine() {
        for (int i = 0; i < 3; i++) {
            int id_1 = i << 4 | 3 << 2;
            lineMap.put(id_1, new ChessLine(id_1));
            int id_2 = i << 4 | 2 << 2 | 3;
            lineMap.put(id_2, new ChessLine(id_2));
            int id_3 = i << 4 | 3 << 2 | 2;
            lineMap.put(id_3, new ChessLine(id_3));
            int id_4 = i << 4 | 3;
            lineMap.put(id_4, new ChessLine(id_4));
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) {
                    continue;
                }
                int id = 3 << 4 | i << 2 | j;
                lineMap.put(id, new ChessLine(id));
            }
        }
    }
    
    private void drawPiece() {
        ChessPieces piece;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (k == 1 && j == 1) {
                        continue;
                    }
                    int id = i << 4 | j << 2 | k;
                    piece = new ChessPieces(id);
                    piecesMap.put(id, piece);
                    linePiece(piece);
                }
            }
        }
    }
    
    private void linePiece(ChessPieces piece) {
        Integer coord = piece.getId();
        Integer xy = coord & 15 | 48;
        Integer ry = coord & 51 | 12;
        Integer rx = coord & 60 | 3;
        if (lineMap.containsKey(xy)) {
            lineMap.get(xy).set(piece);
        }
        if (lineMap.containsKey(ry)) {
            lineMap.get(ry).set(piece);
        }
        if (lineMap.containsKey(rx)) {
            lineMap.get(rx).set(piece);
        }
    }
    
    public ChessPieces getPiece(int id) {
        return piecesMap.get(id);
    }
    
    public Collection<ChessPieces> pieces() {
        return piecesMap.values();
    }
    
    private void judge() {
        
        boolean flag = false;
        for (Iterator<ChessLine> iterator = completeLines.iterator(); iterator.hasNext();) {
            ChessLine next = iterator.next();
            if (!next.can()) {
                iterator.remove();
            }
        }
        
        for (ChessLine line : lineMap.values()) {
            PieceState state = line.getPieces()[0].getState();
            if (!completeLines.contains(line) && line.can() && state == nextStep) {
                completeLines.add(line);
                flag = true;
                switch (state) {
                    case black:
                        blackRmCount++;
                        break;
                    case white:
                        whiteRmCount++;
                        break;
                    default:
                        break;
                }
            }
        }
        
        if (flag || whiteRmCount > 0 || blackRmCount > 0) {
            process = GameProcess.Perform;
            return;
        }
        
        nextStep = nextStep == PieceState.white ? PieceState.black : PieceState.white;
        
        if (isNotEmpty() || whiteExchange > 0 || blackExchange > 0) {
            process = GameProcess.exchange;
            return;
        }
        process = white == 0 && black == 0 ? GameProcess.Walk : GameProcess.Under;
        
        win = win();
        if (win != PieceState.none) {
            process = GameProcess.end;
        }
    }
    
    public boolean isNotEmpty() {
        for (ChessPieces p : piecesMap.values()) {
            if (PieceState.none == p.getState()) {
                return false;
            }
        }
        whiteExchange = 1;
        blackExchange = 1;
        return true;
    }
    
    public void Playing(int pieceId) {
        ChessPieces piece = piecesMap.get(pieceId);
        switch (process) {
            case Under:
                invalid = !under(piece);
                break;
            case Walk:
                invalid = !walk(upPieces, piece);
                break;
            
            case exchange:
                invalid = !exchange(upPieces, piece);
                break;
            case Perform:
                invalid = !perform(piece);
                break;
            default:
                break;
        }
        if (!invalid) {
            judge();
        }
        upPieces = piece;
    }
    
    public boolean under(ChessPieces p) {
        if (p.getState() != PieceState.none) {
            return false;
        }
        if (nextStep == PieceState.white) {
            white--;
        } else {
            black--;
        }
        p.setState(nextStep);
        return true;
    }
    
    public boolean walk(ChessPieces p1, ChessPieces p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        if (p2.getState() != PieceState.none && !p1.isAdjacent(p2)) {
            return false;
        }
        if (p1.getState() != nextStep) {
            return false;
        }
        
        PieceState state = p2.getState();
        p2.setState(p1.getState());
        p1.setState(state);
        upPieces = null;
        return true;
    }
    
    public boolean exchange(ChessPieces p1, ChessPieces p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        if (!p1.isAdjacent(p2)) {
            return false;
        }
        if (p1.getState() == p2.getState()) {
            return false;
        }
        if (p1.getState() != nextStep) {
            return false;
        }
        if (nextStep == PieceState.white) {
            whiteExchange = 0;
        } else {
            blackExchange = 0;
        }
        PieceState state = p2.getState();
        p2.setState(p1.getState());
        p1.setState(state);
        upPieces = null;
        return true;
    }
    
    public boolean perform(ChessPieces p) {
        if (p.getState() == PieceState.none || p.getState() == nextStep) {
            return false;
        }
        p.setState(PieceState.none);
        if (nextStep == PieceState.white) {
            whiteRmCount--;
        } else {
            blackRmCount--;
        }
        return true;
    }
    
    public PieceState win() {
        if (GameProcess.Walk != process) {
            return PieceState.none;
        }
        
        int w = 0, b = 0;
        int i = 0;
        for (ChessPieces p : piecesMap.values()) {
            switch (p.getState()) {
                case black:
                    b++;
                    break;
                case white:
                    w++;
                    break;
                case none:
                    ChessPieces[] ps = p.adjacent();
                    for (ChessPieces p1 : ps) {
                        if (nextStep == p1.getState()) {
                            i++;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        LOG.log(Level.WARNING, "{0}能移动的位置数：{1}", new Object[]{nextStep.name(), i});
        LOG.log(Level.WARNING, "黑:{0}, 白:{1}", new Object[]{b, w});
        if (i == 0) {
            return nextStep == PieceState.black ? PieceState.white : nextStep;
        }
        return w < b ? w < 3 ? PieceState.black : PieceState.none : b < 3 ? PieceState.white : PieceState.none;
    }
    
    public GameProcess getProcess() {
        return process;
    }
    
    public void setProcess(GameProcess process) {
        this.process = process;
    }
    
    public PieceState getNextStep() {
        return nextStep;
    }
    
    public void setNextStep(PieceState nextStep) {
        this.nextStep = nextStep;
    }
    
    public PieceState getWin() {
        return win;
    }
    
    public void setWin(PieceState win) {
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
    
    public int[] getComplete() {
        Set<Integer> ids = new HashSet<>();
        for (ChessLine line : completeLines) {
            for (ChessPieces piece : line.getPieces()) {
                ids.add(piece.getId());
            }
        }
        int[] res = new int[ids.size()];
        int i = 0;
        for (Integer id : ids) {
            res[i++] = id;
        }
        return res;
    }
    
    public void clear() {
        for (ChessPieces value : piecesMap.values()) {
            value.setState(PieceState.none);
        }
        process = GameProcess.Under;
        white = 12;
        black = 12;
        whiteExchange = 0;
        blackExchange = 0;
        whiteRmCount = 0;
        blackRmCount = 0;
    }
    
}
