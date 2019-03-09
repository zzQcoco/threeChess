/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.client;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import java.util.Arrays;
import pri.zzq.threechess.ChessBoard;
import pri.zzq.threechess.ChessPieces;
import pri.zzq.threechess.def.PieceState;

/**
 *
 * @author zzQ
 */
public class AIPlayer {

    public PieceState pieceState;
    private int meIntState, otherIntState;
    private int pieceCount;
    private boolean isContinuous = false;

    public AIPlayer(PieceState pieceState) {
        this.pieceState = pieceState;
    }

    public AIPlayer() {
    }

    public int setp(ChessBoard board) {
        int id = -1;
        pieceCount = pieceState == PieceState.white ? board.getWhite() : board.getBlack();
        meIntState = getIntState(pieceState);
        otherIntState = meIntState == 1 ? 2 : 1;
        Matrix3f[] rings = new Matrix3f[]{new Matrix3f(), new Matrix3f(), new Matrix3f()};

        for (ChessPieces piece : board.pieces()) {
            int state = getIntState(piece.getState());
            rings[piece.getR()].set(piece.getY(), piece.getX(), state);
        }

        if (board.getProcess() != null) {
            switch (board.getProcess()) {
                case Under:
                    id = under(rings);
                    break;
                case Walk:
                    id = walk(rings);
                    break;
                case Perform:
                    id = eat(rings);
                    break;
                case exchange:
                    id = exchange(rings);
                case end:
                    end();
                    break;
                default:
                    break;
            }

        }
        return id;
    }

    private int getIntState(PieceState state) {
        int i = 0;
        if (null != state) {
            switch (state) {
                case none:
                    i = 0;
                    break;
                case white:
                    i = 1;
                    break;
                case black:
                    i = 2;
                    break;
                default:
                    break;
            }
        }
        return i;
    }

    private int under(Matrix3f[] rings) {
        return underPredict(rings, true);
    }

    /**
     * 思考怎么下？
     *
     * @param rings
     * @param isMe
     */
    private int underPredict(Matrix3f[] rings, boolean isMe) {
        Matrix3f[] mix4 = edgeSurface(rings);
        int level = -1;
        int area = -1, x = -1, y = -1;
        int id;
        int state = isMe ? meIntState : otherIntState;
        for (int i = 0; i < mix4.length; i++) {
            Matrix3f mix = mix4[i];
            int[] lxy = underSurface(mix, state);
            if (level < lxy[0]) {
                level = lxy[0];
                x = lxy[1];
                y = lxy[2];
                area = i;
            }
        }
        for (int i = 0; i < mix4.length; i++) {
            Matrix3f mix = mix4[i];
            int[] lxy = underOblique(mix, state);
            if (level < lxy[0]) {
                level = lxy[0];
                x = lxy[1];
                y = lxy[2];
                area = i;
            }
        }
        if (level > 1) {
            id = mix4ToId(area, x, y);
            float v = mix4[area].get(y, x);
            if (v != 0) {
                id = underRandom(rings);
            }
        } else {
            id = underRandom(rings);
        }

        return id;
    }

    private int mix4ToId(int a, int x0, int y0) {
        int r = -1, x = -1, y = -1;
        switch (a) {
            case 0:
                x = 0;
                r = x0;
                y = y0;
                break;
            case 1:
                x = x0;
                r = y0;
                y = 0;
                break;
            case 2:
                x = 2;
                r = x0;
                y = y0;
                break;
            case 3:
                x = x0;
                r = y0;
                y = 2;
                break;
            default:
                break;
        }
        return r << 4 | x << 2 | y;
    }

    /**
     * 随机原则
     *
     * @param rings
     * @return
     */
    private int underRandom(Matrix3f[] rings) {
        int[] nones = board(rings, 0);
        return nones[FastMath.rand.nextInt(nones.length)];
    }

    private int[] underSurface(Matrix3f mix, int state) {
        Vector3f line = new Vector3f();
        int x = -1, y = -1, tl, level = -1;
        for (int i = 0; i < 3; i++) {
            mix.getColumn(i, line);
            tl = decision(line, state);
            if (level < tl) {
                level = tl;
                if (tl != -1) {
                    int c = coordinates(line);
                    x = i;
                    y = c;
                }
            }
            mix.getRow(i, line);
            tl = decision(line, state);
            if (level < tl) {
                level = tl;
                if (tl != -1) {
                    int c = coordinates(line);
                    y = i;
                    x = c;
                }
            }
        }
        return new int[]{level, x, y};
    }

    private int[] underOblique(Matrix3f mix, int state) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                float v = mix.get(y, x);
                if (v == state) {
                    int[] lv = decisionOblique(x, y, state, mix);
                    if (lv[0] != -1) {
                        return lv;
                    }
                }
            }
        }
        return new int[]{-1, -1, -1};
    }

    /**
     * 判断现有落子的斜对点
     *
     * @param x0
     * @param y0
     * @param state
     * @param mix
     * @return 返回级别和点坐标 -1:不能放。3.放对点。4.放交点
     */
    private int[] decisionOblique(int x0, int y0, int state, Matrix3f mix) {
        int level = -1, rx = -1, ry = -1;
        float v0 = mix.get(y0, x0);
        Vector3f line1 = new Vector3f();
        Vector3f line2 = new Vector3f();
        boolean flag = false, flag1 = false;
        float vp;
        f:
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int abx = Math.abs(x0 - x);
                int aby = Math.abs(y0 - y);
                if (abx == aby) {
                    float v1 = mix.get(y, x);
                    mix.getRow(y0, line1);
                    mix.getColumn(x, line2);

                    if (((line1.x == 0 || line1.x == state)
                            && (line1.y == 0 || line1.y == state)
                            && (line1.z == 0 || line1.z == state))
                            || ((line2.x == 0 || line2.x == state)
                            && (line2.y == 0 || line2.y == state)
                            && (line2.z == 0 || line2.z == state))) {
                        rx = x;
                        ry = y0;
                        vp = mix.get(y0, x);
                        if (vp == 0) {
                            flag1 = true;
                        }
                        flag = true;
                    }

                    mix.getRow(y, line1);
                    mix.getColumn(x0, line2);

                    if (((line1.x == 0 || line1.x == state)
                            && (line1.y == 0 || line1.y == state)
                            && (line1.z == 0 || line1.z == state))
                            || ((line2.x == 0 || line2.x == state)
                            && (line2.y == 0 || line2.y == state)
                            && (line2.z == 0 || line2.z == state))) {
                        rx = x0;
                        ry = y;
                        vp = mix.get(y, x0);
                        if (vp == 0) {
                            flag1 = true;
                        }
                        flag = true;
                    }

                    if (v1 == 0 && flag) {
                        level = 3;
                        rx = x;
                        ry = y;
                        break f;
                    }

                    if (v1 == v0 && flag1) {
                        level = 4;
                        break f;
                    }
                }
            }
        }

        return new int[]{level, rx, ry};
    }

    private int coordinates(Vector3f line) {
        if (line.x == 0) {
            return 0;
        } else if (line.y == 0) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     *
     * @param line
     * @param state
     * @return 返回级别 -1:不能放. 0:线上有其它子，但可落. 1:线上无落子. 2:线上已有一子. 9. 线上有两子
     */
    private int decision(Vector3f line, int state) {
        int x = (int) line.x;
        int y = (int) line.y;
        int z = (int) line.z;
        int l = x | y | z;
        if (l == state) {
            int sum = x + y + z;
            if (sum == state) {
                return 2;
            } else if (sum == (2 * state)) {
                return 9;
            } else {
                return -1;
            }
        } else if (l == 0) {
            return 1;
        } else {
            if (x == 0 || y == 0 || z == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * x:0, y:0, x:2, y:2 讲3矩阵，转换为4矩阵
     *
     * @param rings
     * @return
     */
    private Matrix3f[] edgeSurface(Matrix3f[] rings) {
        Matrix3f[] mix4 = new Matrix3f[]{new Matrix3f(), new Matrix3f(), new Matrix3f(), new Matrix3f()};
        for (int i = 0; i < 3; i++) {
            Matrix3f ring = rings[i];
            mix4[0].setColumn(i, ring.getColumn(0));
            mix4[1].setRow(i, ring.getRow(0));
            mix4[2].setColumn(i, ring.getColumn(2));
            mix4[3].setRow(i, ring.getRow(2));
        }
        return mix4;
    }
    private int nextId;

    private int walk(Matrix3f[] rings) {
        int id = -1;
        if (isContinuous) {
            id = nextId;
            isContinuous = false;
        } else {
            int[] vs = adjacent(rings, meIntState, 0);
            if (vs != null) {
                id = vs[0] << 4 | vs[1] << 2 | vs[2];
                nextId = vs[3] << 4 | vs[4] << 2 | vs[5];
            }
            isContinuous = true;
        }
        return id;
    }

    private int exchange(Matrix3f[] rings) {
        int id = -1;
        if (isContinuous) {
            id = nextId;
            isContinuous = false;
        } else {
            int[] vs = adjacent(rings, meIntState, otherIntState);
            if (vs != null) {
                id = vs[0] << 4 | vs[1] << 2 | vs[2];
                nextId = vs[3] << 4 | vs[4] << 2 | vs[5];
            }
            isContinuous = true;
        }
        return id;
    }

    private int[] adjacent(Matrix3f[] rings, int state, int otherState) {
        Vector3f rVec = new Vector3f();
        Vector3f xVec = new Vector3f();
        Vector3f yVec = new Vector3f();
        int level = -1;
        int r0 = -1, x0 = -1, y0 = -1, r1 = -1, x1 = -1, y1 = -1;
        for (int r = 0; r < 3; r++) {
            Matrix3f ring = rings[r];
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (y == 1 && x == 1) {
                        continue;
                    }
                    if (ring.get(y, x) != otherState) {
                        continue;
                    }
                    line3(rings, r, x, y, rVec, xVec, yVec);
                    int[] learr = level(r, x, y, rVec, xVec, yVec, state);
                    if (level < learr[0]) {
                        level = learr[0];
                        r0 = learr[1];
                        x0 = learr[2];
                        y0 = learr[3];
                        r1 = r;
                        x1 = x;
                        y1 = y;
                    }
                }
            }
        }
        return new int[]{r0, x0, y0, r1, x1, y1};
    }

    private int isAdj(int c, Vector3f vec, int state) {
        if (c == 1) {
            if (vec.get(0) == state && vec.get(2) == state) {
                return 3;
            } else if (vec.get(0) == state) {
                return 0;
            } else if (vec.get(2) == state) {
                return 2;
            } else {
                return -1;
            }
        } else {
            if (vec.get(1) == state) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private int[] level(int r, int x, int y, Vector3f rVec, Vector3f xVec, Vector3f yVec, int state) {
        int level = -1, x0 = x, y0 = y, r0 = r;
        int rAdj, xAdj, yAdj, adjCount = 0;
        if ((rAdj = isAdj(r, rVec, state)) != -1) {
            if (rAdj == 3) {
                adjCount++;
            }
            adjCount++;
        }
        if ((xAdj = isAdj(x, xVec, state)) != -1) {
            if (xAdj == 3) {
                adjCount++;
            }
            adjCount++;
        }
        if ((yAdj = isAdj(y, yVec, state)) != -1) {
            if (yAdj == 3) {
                adjCount++;
            }
            adjCount++;
        }
        if (adjCount == 0) {
            return new int[]{level, r0, x0, y0};
        }

        if (adjCount > 3) {
            level = 3;
            if (rAdj == 3 && xAdj == 3) {
                if (FastMath.rand.nextBoolean()) {
                    x0 = FastMath.rand.nextBoolean() ? 0 : 2;
                } else {
                    r0 = FastMath.rand.nextBoolean() ? 0 : 2;
                }
            } else if (rAdj == 3 && yAdj == 3) {
                if (FastMath.rand.nextBoolean()) {
                    y0 = FastMath.rand.nextBoolean() ? 0 : 2;
                } else {
                    r0 = FastMath.rand.nextBoolean() ? 0 : 2;
                }
            } else {
                int sx = (int) (xVec.x + xVec.y + xVec.z - 2 * state);
                if (sx == 0) {
                    y0 = yAdj;
                } else {
                    x0 = xAdj;
                }
            }
        } else if (adjCount > 2) {
            level = 2;
            int sx = (int) (xVec.x + xVec.y + xVec.z - 2 * state);
            int sy = (int) (yVec.x + yVec.y + yVec.z - 2 * state);
            int sr = (int) (rVec.x + rVec.y + rVec.z - 2 * state);
            if (xAdj != -1 && yAdj != -1) {
                boolean flag = FastMath.rand.nextBoolean();
                if (sx == 0 && sy == 0) {
                    r0 = rAdj;
                } else if (sx == 0 && sr == 0) {
                    y0 = yAdj;
                } else if (sr == 0 && sy == 0) {
                    x0 = xAdj;
                } else if (sx == 0) {
                    if (flag) {
                        y0 = yAdj;
                    } else {
                        r0 = rAdj;
                    }
                } else if (sy == 0) {
                    if (flag) {
                        x0 = xAdj;
                    } else {
                        r0 = rAdj;
                    }
                } else if (sr == 0) {
                    if (flag) {
                        x0 = xAdj;
                    } else {
                        y0 = yAdj;
                    }
                } else {
                    int ra = FastMath.nextRandomInt(0, 2);
                    switch (ra) {
                        case 0:
                            r0 = rAdj;
                            break;
                        case 1:
                            x0 = xAdj;
                            break;
                        default:
                            y0 = yAdj;
                            break;
                    }
                }
            } else if (xAdj != -1) {
                if (sr == 0) {
                    x0 = xAdj;
                } else {
                    r0 = rAdj;
                }
            } else {
                if (sr == 0) {
                    y0 = yAdj;
                } else {
                    r0 = rAdj;
                }
            }
        } else if (adjCount > 1) {
            level = 1;
            boolean flag = FastMath.rand.nextBoolean();
            if (rAdj != -1) {
                r0 = rAdj == 3 ? flag ? 0 : 2 : rAdj;
            } else if (xAdj != -1) {
                x0 = xAdj == 3 ? flag ? 0 : 2 : xAdj;
            } else {
                y0 = yAdj == 3 ? flag ? 0 : 2 : yAdj;
            }
        } else {
            level = 0;
            if (rAdj != -1) {
                r0 = rAdj;
            } else if (xAdj != -1) {
                x0 = xAdj;
            } else {
                y0 = yAdj;
            }
        }
        return new int[]{level, r0, x0, y0};
    }

    private void line3(Matrix3f[] rings, int r, int x, int y, Vector3f rVec, Vector3f xVec, Vector3f yVec) {
        for (int i = 0; i < 3; i++) {
            rVec.set(i, rings[i].get(y, x));
            if (x != 1) {
                yVec.set(i, rings[r].get(i, x));
            } else {
                yVec.set(i, -1);
            }
            if (y != 1) {
                xVec.set(i, rings[r].get(y, i));
            } else {
                xVec.set(i, -1);
            }

        }
    }

    private int eat(Matrix3f[] rings) {
        int[] ids = board(rings, otherIntState);
        return ids[FastMath.rand.nextInt(ids.length)];
    }

    private void end() {

    }

    private int[] board(Matrix3f[] rings, int state) {
        int[] data = new int[24];
        int offset = 0;
        for (int i = 0; i < rings.length; i++) {
            Matrix3f ring = rings[i];
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    if (y == 1 && x == 1) {
                        continue;
                    }
                    if (ring.get(y, x) == state) {
                        data[offset++] = i << 4 | x << 2 | y;
                    }
                }
            }
        }
        return Arrays.copyOf(data, offset);
    }

    private int judge(int r, int x, int y, Matrix3f[] rings) {
        int count = 0;
        if (x == 1 && y == 1) {
            return count;
        }

        if (rings[0].get(y, x) != 0 && rings[0].get(y, x) == rings[1].get(y, x) && rings[0].get(y, x) == rings[2].get(y, x)) {
            count++;
        }
        if (x != 1) {
            Vector3f vec = rings[r].getColumn(x);
            if (vec.x != 0 && vec.x == vec.y && vec.x == vec.z) {
                count++;
            }
        }
        if (y != 1) {
            Vector3f vec = rings[r].getRow(y);
            if (vec.x != 0 && vec.x == vec.y && vec.x == vec.z) {
                count++;
            }
        }
        return count;

    }

}
