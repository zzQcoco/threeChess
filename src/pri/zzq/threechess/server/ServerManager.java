/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.server;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import pri.zzq.threechess.ChessBoard;
import pri.zzq.threechess.ChessPieces;
import pri.zzq.threechess.Globals;
import pri.zzq.threechess.Player;
import pri.zzq.threechess.Room;
import pri.zzq.threechess.def.GameProcess;
import pri.zzq.threechess.def.PlayerType;
import pri.zzq.threechess.def.RoomOption;
import pri.zzq.threechess.network.BackMessage;
import pri.zzq.threechess.network.ChessMessage;
import pri.zzq.threechess.network.RoomListMessage;
import pri.zzq.threechess.network.RoomMessage;
import pri.zzq.threechess.network.ShowMessage;
import pri.zzq.threechess.network.StartMessage;
import pri.zzq.threechess.network.StepMessage;
import pri.zzq.threechess.util.RoomPool;

/**
 *
 * @author zzQ
 */
public class ServerManager extends AbstractAppState implements MessageListener<HostedConnection> , ConnectionListener{

    private static final Logger LOG = Logger.getLogger(ServerManager.class.getName());

    private RoomPool roomPool;
    private Map<Integer, Room> roomMap = new HashMap<>();
    private Server server;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        roomPool = new RoomPool();
        try {
            super.initialize(stateManager, app);
            server = Network.createServer(Globals.GAME_NAME, Globals.VERSION, Globals.TCP_PORT, Globals.UDP_PORT);
            Serializer.registerClasses(ChessMessage.class, RoomListMessage.class, RoomMessage.class,
                    ShowMessage.class, StartMessage.class, StepMessage.class, BackMessage.class);

            server.addMessageListener(this, ChessMessage.class, RoomListMessage.class, RoomMessage.class,
                    ShowMessage.class, StartMessage.class, StepMessage.class, BackMessage.class);
            server.addConnectionListener(this);
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, "服务器创建失败", ex);
        }

    }

    private Room retrieve() {
        Room room = null;
        for (Room value : roomMap.values()) {
            if (value.getState() == Room.State.Waiting && value.getOther() != null) {
                room = value;
            }
        }
        if (room == null) {
            room = roomPool.obtain();
            roomMap.put(room.getId(), room);
        }
        return room;
    }

    @Override
    public void messageReceived(HostedConnection source, Message m) {
        LOG.log(Level.WARNING, "[Server-Host-{0}]:{1}", new Object[]{source.getId(), m.toString()});
        if (m instanceof RoomMessage) {
            RoomMessage roomMessage = (RoomMessage) m;
            RoomMessage receipt = new RoomMessage();
            Room room = null;
            Player player = new Player();
            player.setId(source.getId());

            boolean playing = false;
            switch (RoomOption.valueOf(roomMessage.getOption())) {
                case create:
                    room = retrieve();
                    player.setType(PlayerType.black);
                    room.setMaster(player);
                    room.setState(Room.State.Waiting);
                    receipt.setRoomId(room.getId());
                    receipt.setSuccess(0);
                    receipt.setPlayerId(player.getId());
                    receipt.setType(player.getType().name());
                    break;
                case Join:
                    room = roomMap.get(roomMessage.getRoomId());
                    player.setType(PlayerType.white);
                    room.setOther(player);
                    room.setState(Room.State.Playing);
                    room.getBoard().start();
                    playing = true;

                    receipt.setRoomId(room.getId());
                    receipt.setPlayerId(player.getId());
                    receipt.setType(player.getType().name());
                    receipt.setSuccess(0);
                    break;
                case leave:
                    room = roomMap.get(roomMessage.getRoomId());
                    if (room.getMaster().getId() == source.getId()) {
                        room.setMaster(room.getOther());
                    } else {
                        room.setOther(null);
                    }
                    receipt.setRoomId(room.getId());
                    room.setState(Room.State.Waiting);
                    receipt.setSuccess(0);
                default:
                    break;
            }

            source.send(receipt);
            if (playing) {
                roomRadio(room);
            }
        } else if (m instanceof StepMessage) {
            StepMessage stepMessage = (StepMessage) m;
            Room room = roomMap.get(stepMessage.getRoomId());
            ChessBoard board = room.getBoard();
            Player player = room.getPlayer(source.getId());
            if (player != null && player.getType().name().equals(board.getNextStep().name())) {
                board.Playing(stepMessage.getPieceId());
                if (board.getProcess() == GameProcess.Perform) {
                    ShowMessage showMessage = new ShowMessage();
                    showMessage.setPieces(board.getComplete());
                    server.broadcast(Filters.in(server.getConnection(room.getMaster().getId()),
                            server.getConnection(room.getOther().getId())), showMessage);
                }
                roomRadio(room);
            }
        } else if (m instanceof RoomListMessage) {
            source.send(listMessage());
        } else if (m instanceof BackMessage) {
            BackMessage backMessage = (BackMessage) m;
            Room room = roomMap.get(backMessage.roomId);
            Player player = room.getPlayer(backMessage.playerId);
            int otherId = -1;
            if (player != null) {
                if (player.equals(room.getMaster())) {
                    room.setMaster(null);
                    otherId = room.getOther() != null ? room.getOther().getId() : -1;
                } else {
                    room.setOther(null);
                    otherId = room.getMaster() != null ? room.getMaster().getId() : -1;
                }
            }
            if (room.getMaster() == null && room.getOther() == null) {
                roomMap.remove(room.getId());
                roomPool.free(room);
            } else {
                room.setState(Room.State.Waiting);
                room.getBoard().clear();
                if (otherId != -1) {
                    BackMessage bm = new BackMessage();
                    bm.level = 2;
                    bm.playerId = otherId;
                    bm.roomId = room.getId();
                    server.broadcast(Filters.equalTo(server.getConnection(otherId)), bm);
                }
            }
            source.send(listMessage());
        }
    }

    private RoomListMessage listMessage() {
        RoomListMessage roomListMessage = new RoomListMessage();
        int len = roomMap.size();
        int[] roomIds = new int[len];
        String[] states = new String[len];
        int i = 0;
        for (Map.Entry<Integer, Room> entry : roomMap.entrySet()) {
            Integer key = entry.getKey();
            Room value = entry.getValue();
            roomIds[i] = key;
            states[i] = value.getState().name();
            i++;
        }
        roomListMessage.setRoomIds(roomIds);
        roomListMessage.setStates(states);
        return roomListMessage;
    }
    

    private void roomRadio(Room room) {
        ChessBoard board = room.getBoard();
        ChessMessage chessMessage = new ChessMessage();
        chessMessage.setRoomId(room.getId());

        chessMessage.setBlack(board.getBlack());
        chessMessage.setBlackExchange(board.getBlackExchange());
        chessMessage.setBlackRmCount(board.getBlackRmCount());

        chessMessage.setWhite(board.getWhite());
        chessMessage.setWhiteExchange(board.getWhiteExchange());
        chessMessage.setWhiteRmCount(board.getWhiteRmCount());

        Map<Integer, String> pieceStateMap = new HashMap<>(24);
        for (ChessPieces piece : board.pieces()) {
            pieceStateMap.put(piece.getId(), piece.getState().name());
        }
        chessMessage.setPieceStateMap(pieceStateMap);
        chessMessage.setProcess(board.getProcess().name());
        chessMessage.setWin(board.getWin().name());
        chessMessage.setNextStep(board.getNextStep().name());

        server.broadcast(Filters.in(server.getConnection(room.getMaster().getId()),
                server.getConnection(room.getOther().getId())), chessMessage);
    }

    @Override
    public void cleanup() {
        server.close();
        super.cleanup();
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        int roomId = -1;
        for (Room value : roomMap.values()) {
            Player p = value.getPlayer(conn.getId());
            if (p != null) {
                if (p.equals(value.getMaster())) {
                    value.setMaster(null);
                    value.setMaster(value.getOther());
                }else{
                    value.setOther(null);
                }
                value.setState(Room.State.Waiting);
                value.getBoard().clear();
                if (value.getMaster() == null) {
                    roomId = value.getId();
                }
                System.out.println(p.getId() + "离开");
            }
        }
        if (roomId != -1) {
            Room room = roomMap.remove(roomId);
            roomPool.free(room);
        }
    }

}
