/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.client;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Dome;
import com.jme3.ui.Picture;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import pri.zzq.threechess.ChessBoard;
import pri.zzq.threechess.ChessPieces;
import pri.zzq.threechess.Globals;
import pri.zzq.threechess.Room;
import pri.zzq.threechess.def.GameProcess;
import pri.zzq.threechess.def.PieceState;
import pri.zzq.threechess.def.PlayerType;
import pri.zzq.threechess.def.RoomOption;
import pri.zzq.threechess.main.PiecesControl;
import pri.zzq.threechess.network.BackMessage;
import pri.zzq.threechess.network.ChessMessage;
import pri.zzq.threechess.network.RoomListMessage;
import pri.zzq.threechess.network.RoomMessage;
import pri.zzq.threechess.network.ShowMessage;
import pri.zzq.threechess.network.StartMessage;
import pri.zzq.threechess.network.StepMessage;

/**
 *
 * @author zzQ
 */
public class ClientManager extends AbstractAppState implements MessageListener<Client> {

    private static final Logger LOG = Logger.getLogger(ClientManager.class.getName());

    private Node mainNode, roomListNode, roomNode, aboutNode;

    private Camera cam;
    private SimpleApplication simpleApp;

    private Node sceneNode;

    Map<Integer, PiecesControl> pieceMap = new HashMap<>(24);

    private Client client;

    private GameProcess process;
    private PieceState nextStep;
    private PieceState win;
    private PlayerType playerType;

    private float width, height;

    private final String transparentPNG = "Textures/transparent.png";

    private final String bgInfoPNG = "Textures/bg_info.png";

    private final String bgBtnPNG = "Textures/btn.png";

    private final String boardBG = "Textures/bg.png";

    private int roomId, playerId, white, black, whiteRmCount, blackRmCount, whiteExchange, blackExchange;

    private BitmapText tipText;
    private BitmapText infoText;
    private BitmapText playerText;

    private BitmapFont font;

    private final boolean debug = false;
    private boolean offline = true;
    private boolean isAI = false;
    private float ob;
    private boolean enableBGM = true;
    private AudioNode audioNode;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.simpleApp = (SimpleApplication) app;
        this.cam = app.getCamera();
        width = cam.getWidth();
        height = cam.getHeight();

        font = getAssetManager().loadFont("Interface/text.fnt");

        ob = FastMath.sqrt(FastMath.sqr(width) + FastMath.sqr(height));
        if (!debug) {
            Thread netThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        client = Network.connectToServer(Globals.GAME_NAME, Globals.VERSION, Globals.HOST, Globals.TCP_PORT, Globals.UDP_PORT);
                        Serializer.registerClasses(ChessMessage.class, RoomListMessage.class, RoomMessage.class,
                                ShowMessage.class, StartMessage.class, StepMessage.class, BackMessage.class);
                        client.addMessageListener(simpleApp.getStateManager().getState(ClientManager.class), ChessMessage.class,
                                RoomListMessage.class, RoomMessage.class, ShowMessage.class,
                                StartMessage.class, StepMessage.class, BackMessage.class);
                        client.start();
                        offline = false;
                    } catch (IOException ex) {
                        offline = true;
                    }
                }
            });
            netThread.start();
        }
        sceneNode = new Node("scene Node");
        simpleApp.getGuiNode().attachChild(sceneNode);
        init();
        main();
    }

    private ChessBoard board;
    private AIPlayer aIPlayer;

    private final float aiDelay = 1f;
    private float time = 0f;

    private void initAi() {
        isAI = true;
        if (board == null) {
            board = new ChessBoard();
        }
        if (aIPlayer == null) {
            playerType = PlayerType.white;
            aIPlayer = new AIPlayer(PieceState.black);
        }
        board.clear();
        board.start();
        updateAiBoard();
    }

    private void playing(int id) {
        if (playerType.name().equals(board.getNextStep().name())) {
            LOG.log(Level.WARNING, "玩家走");
            board.Playing(id);
        }
        updateAiBoard();
    }

    private void aiPlaying() {
        if (aIPlayer != null && aIPlayer.pieceState == board.getNextStep()) {
            int id = aIPlayer.setp(board);
            LOG.log(Level.WARNING, "AI {0}, {1}", new Object[]{process, id});
            if (id != -1) {
                board.Playing(id);
            }
        }
        updateAiBoard();
    }

    @Override
    public void update(float tpf) {
        if (aIPlayer != null && aIPlayer.pieceState == nextStep) {
            time += tpf;
        }
        if (isAI && time > aiDelay) {
            time = 0f;
            aiPlaying();
        }
        if (board != null) {
            for (ChessPieces piece : board.pieces()) {
                PiecesControl control = pieceMap.get(piece.getId());
                if (control.getState() != piece.getState()) {
                    control.setState(piece.getState());
                }
            }
        }
    }

    private void updateAiBoard() {
        LOG.log(Level.WARNING, "当前对战AI：{0}", isAI);
        if (!isAI) {
            return;
        }

        this.black = board.getBlack();
        this.blackExchange = board.getBlackExchange();
        this.blackRmCount = board.getBlackRmCount();
        this.white = board.getWhite();
        this.whiteExchange = board.getWhiteExchange();
        this.whiteRmCount = board.getWhiteRmCount();
        this.win = board.getWin();
        this.nextStep = board.getNextStep();
        this.process = board.getProcess();
        LOG.log(Level.WARNING, "更新房间状态nextStep:{0}", nextStep);
        updateTip();

        if (board.getProcess() == GameProcess.Perform) {
            show(board.getComplete());
        }
    }

    private void init() {
        initMain();
        initRoomList();
        initRoom();
        initAbout();
        initBGM();
        initBGMIoc();
    }

    private void initBGM() {
        audioNode = new AudioNode(getAssetManager(), "Sounds/bgSound.wav", AudioData.DataType.Buffer);
        audioNode.setPositional(false);
        audioNode.setPositional(false);
        audioNode.setVolume(3);
        audioNode.setLooping(true);
        simpleApp.getAudioRenderer().playSource(audioNode);
        enableBGM = true;
    }

    private Node bgmIocNode;
    
    private float bgmTimer = 0f;
    private void initBGMIoc() {
        MouseEventControl mec = new MouseEventControl(new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                float time = simpleApp.getTimer().getTimeInSeconds();
                if (event.isReleased() && (time - bgmTimer) > 0.2f) {
                    bgmTimer = time;
                    enableBGM = !enableBGM;
                    if (enableBGM) {
                        audioNode.play();
                    } else {
                        audioNode.stop();
                    }
                    LOG.log(Level.WARNING, "BGM:{0}", enableBGM);
                    if (target instanceof Picture) {
                        Picture pic = (Picture) target;
                        pic.setImage(getAssetManager(), enableBGM ? "Textures/bgmOff.png" : "Textures/bgmOn.png", true);
                    }
                }
            }
        });

        bgmIocNode = new Node("bgm btn");
        Picture bgmbtn = new Picture("bgmpic");
        bgmbtn.setImage(getAssetManager(), "Textures/bgmOff.png", true);
        float a = height * 0.08f;
        bgmbtn.setWidth(a);
        bgmbtn.setHeight(a);
        bgmbtn.setLocalTranslation(width * 0.98f - a, height * 0.9f, 5);
        bgmbtn.addControl(mec);
        bgmIocNode.attachChild(bgmbtn);
        simpleApp.getGuiNode().attachChild(bgmIocNode);
    }

    private void initMain() {
        float w = width * 0.1f;
        float h = height * 0.4f;
        float iv = height * 0.05f;

        mainNode = new Node("main Node");

        String[] nodeNames = new String[]{"npc Node", "create node", "join node", "about node"};
        String[] labels = new String[]{"人机对战", "创建房间", "加入房间", "关于游戏"};
        MouseEventControl[] mecs = new MouseEventControl[]{
            new MouseEventControl(new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (event.isReleased()) {
                        // TODO: 人机对战
                        isAI = true;
                        initAi();
                        room();
                    }
                }
            }),
            new MouseEventControl(new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (event.isReleased() && !offline) {
                        RoomMessage roomMessage = new RoomMessage();
                        roomMessage.setOption(RoomOption.create.name());
                        roomMessage.setRoomId(-1);
                        roomMessage.setSuccess(0);
                        client.send(roomMessage);
                    }
                }
            }),
            new MouseEventControl(new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (event.isReleased() && !offline) {
                        RoomListMessage m = new RoomListMessage();
                        client.send(m);
                    }
                }

            }),
            new MouseEventControl(new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (event.isReleased()) {
                        about();
                    }
                }
            })
        };

        for (int i = 0; i < 4; i++) {
            BitmapText text = new BitmapText(font);
            text.setText(labels[i]);
            text.setSize(ob * 0.03f);
            text.setColor(ColorRGBA.Black);
            text.setName("label");

            Picture picture = new Picture("pic " + i);
            picture.setImage(getAssetManager(), bgBtnPNG, true);
            picture.setWidth(text.getLineCount() * text.getLineWidth());
            picture.setHeight(text.getHeight());
            picture.setLocalTranslation(0, -text.getHeight(), -10);
            picture.addControl(mecs[i]);

            Node node = new Node(nodeNames[i]);
            node.attachChild(text);
            node.attachChild(picture);
            node.setLocalTranslation(w, h - iv * i, 1);
            mainNode.attachChild(node);
        }

        Picture bg_main = new Picture("bg main");
        bg_main.setImage(getAssetManager(), "Textures/bg_main.png", true);
        bg_main.setWidth(width);
        bg_main.setHeight(height);
        bg_main.setLocalTranslation(0, 0, -20);
        mainNode.attachChild(bg_main);
    }

    private void main() {
        Spatial cSpatial = mainNode.getChild("create node");
        if (cSpatial instanceof Node) {
            Node createNode = (Node) cSpatial;
            Spatial labelSpatial = createNode.getChild("label");
            if (labelSpatial instanceof BitmapText) {
                BitmapText label = (BitmapText) labelSpatial;
                label.setText(offline ? "创建房间(离线)" : "创建房间");
                if (offline) {
                    label.setColor(4, 8, ColorRGBA.Red);
                }
            }
        }

        Spatial jSpatial = mainNode.getChild("join node");
        if (jSpatial instanceof Node) {
            Node joinNode = (Node) jSpatial;
            Spatial labelSpatial = joinNode.getChild("label");
            if (labelSpatial instanceof BitmapText) {
                BitmapText label = (BitmapText) labelSpatial;
                label.setText(offline ? "加入房间(离线)" : "加入房间");
                if (offline) {
                    label.setColor(4, 8, ColorRGBA.Red);
                }
            }
        }
        simpleApp.enqueue(new Runnable() {
            @Override
            public void run() {
                sceneNode.detachAllChildren();
                sceneNode.attachChild(mainNode);
            }
        });
    }

    private void initRoomList() {
        roomListNode = new Node("room list");

        Picture returnIoc = returnIoc();
        returnIoc.addControl(new MouseEventControl(new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                if (event.isReleased()) {
                    main();
                }
            }

        }));
        roomListNode.attachChild(returnIoc);

        Picture gameListBg = new Picture("list bg");
        gameListBg.setImage(getAssetManager(), "/Textures/gameList.png", true);
        gameListBg.setWidth(width);
        gameListBg.setHeight(height);
        gameListBg.setLocalTranslation(0, 0, -20);
        roomListNode.attachChild(gameListBg);

        listNode = new Node("info list");
        roomListNode.attachChild(listNode);
    }

    private Node listNode;

    private void roomList(int[] roomids, String[] states) {
        listNode.detachAllChildren();

        float w = width * 0.3f;
        float h = height * 0.8f;
        float hiv = width * 0.1f, wiv = height * 0.1f;

        MouseEventControl mec = new MouseEventControl(new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                if (event.isReleased()) {
                    int fid = (int) target.getUserData("fromId");
                    RoomMessage m = new RoomMessage();
                    m.setRoomId(fid);
                    m.setOption(RoomOption.Join.name());
                    m.setSuccess(0);
                    client.send(m);
                }
            }

        });
        String stateName = null;
        for (int i = 0; i < roomids.length; i++) {
            ColorRGBA color = ColorRGBA.Black;

            switch (Room.State.valueOf(states[i])) {
                case Waiting:
                    color = ColorRGBA.Green;
                    stateName = "等待";
                    break;
                case Playing:
                    color = ColorRGBA.Red;
                    stateName = "游戏中";
                    break;
                default:
                    break;
            }

            BitmapText text_1 = new BitmapText(font);
            text_1.setText(String.valueOf(roomids[i]));
            text_1.setColor(color);
            text_1.setSize(ob * 0.02f);

            BitmapText text_2 = new BitmapText(font);
            text_2.setText(stateName);
            text_2.setColor(color);
            text_2.setSize(ob * 0.02f);

            text_1.setLocalTranslation(w, h - i * hiv, 10);
            text_2.setLocalTranslation(w + wiv, h - i * hiv, 10);

            Picture p = new Picture("bg_p1");
            p.setImage(getAssetManager(), transparentPNG, true);
            p.setWidth(width);
            p.setHeight(text_1.getHeight());
            p.setLocalTranslation(w, h - i * hiv - text_1.getHeight(), 0);
            p.setUserData("fromId", roomids[i]);

            p.addControl(mec.cloneForSpatial(p));

            listNode.attachChild(text_1);
            listNode.attachChild(text_2);
            listNode.attachChild(p);
        }

        simpleApp.enqueue(new Runnable() {
            @Override
            public void run() {
                sceneNode.detachAllChildren();
                sceneNode.attachChild(roomListNode);
            }
        });
    }

    private Picture returnIoc() {
        Picture returnIoc = new Picture("ioc_return");
        returnIoc.setImage(getAssetManager(), "/Textures/return_ioc.png", true);
        float a = height * 0.08f;
        returnIoc.setWidth(a);
        returnIoc.setHeight(a);
        returnIoc.setLocalTranslation(width * 0.02f, height * 0.9f, 5);
        return returnIoc;
    }

    private void initRoom() {
        roomNode = new Node("room node");
        Picture picture = new Picture("bg");
        picture.setImage(getAssetManager(), boardBG, true);
        picture.setWidth(width);
        picture.setHeight(width);
        picture.setLocalTranslation(0, (height - width) / 2, 5);
        roomNode.attachChild(picture);

        Picture returnIoc = returnIoc();
        returnIoc.addControl(new MouseEventControl(new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                if (event.isReleased()) {
                    if (isAI) {
                        main();
                    } else {
                        BackMessage m = new BackMessage();
                        m.roomId = roomId;
                        m.playerId = playerId;
                        m.level = 2;
                        client.send(m);
                    }
                }
            }

        }));
        roomNode.attachChild(returnIoc);

        Picture bgPicture = new Picture("bg pic");
        bgPicture.setImage(getAssetManager(), "/Textures/IMG_0106.JPG", true);
        bgPicture.setWidth(width);
        bgPicture.setHeight(height);
        bgPicture.setLocalTranslation(0, 0, -5);
        roomNode.attachChild(bgPicture);

        MouseEventControl mouseEventControl = new MouseEventControl();
        mouseEventControl.addMouseListener(new DefaultMouseListener() {
            @Override
            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                if (event.isReleased()) {
                    PiecesControl piece = target.getControl(PiecesControl.class);
                    if (piece != null && playerType.name().equals(nextStep.name())) {
                        LOG.log(Level.WARNING, "click -- playerType:{0}, nextStep:{1}", new Object[]{playerType, nextStep});
                        if (isAI) {
                            playing(piece.getId());
                        } else {
                            StepMessage stepMessage = new StepMessage();
                            stepMessage.setPieceId(piece.getId());
                            stepMessage.setRoomId(roomId);
                            client.send(stepMessage);
                        }
                    }
                }
            }
        });
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Black);
        mat.setTransparent(true);

        Dome dome = new Dome(10, 64, ob * 0.02f);
        Geometry geometry = new Geometry("pieces", dome);
        geometry.setMaterial(mat);
        geometry.rotate(-FastMath.HALF_PI, 0, 0);
        geometry.setLocalTranslation(width / 2, height / 2, 0);
        geometry.addControl(mouseEventControl);
//        roomNode.attachChild(geometry);

        Geometry temp;
        PiecesControl piece;

        for (int i = 0; i < 3; i++) {
            float rate = (10f + i * 50f) / 420f;
            float distance = (400f - i * 100f) / 420f * width / 2;
            Vector3f orgin = new Vector3f(rate * width, (height - width) / 2 + rate * width, 1);
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (k == 1 && j == 1) {
                        continue;
                    }
                    temp = geometry.clone();
                    int id = i << 4 | j << 2 | k;
                    temp.setName("piece-" + id);
                    temp.setLocalTranslation(orgin.add(distance * j, distance * k, -10));
                    piece = new PiecesControl(id);
                    pieceMap.put(id, piece);
                    temp.addControl(piece);
                    roomNode.attachChild(temp);
                }
            }
        }

        Node tipNode = new Node("tip Node");

        Picture bgpicInfo = new Picture("bg info");
        bgpicInfo.setImage(getAssetManager(), bgInfoPNG, true);
        bgpicInfo.setWidth(width * 0.9f);
        bgpicInfo.setHeight(height * 0.2f);
        bgpicInfo.setLocalTranslation(0, -height * 0.1f, -2);

        playerText = new BitmapText(font);
        playerText.setText("");
        playerText.setColor(ColorRGBA.Black);
        playerText.setSize(ob * 0.03f);
        playerText.setLocalTranslation(width * 0.2f, height * 0.04f, 0);

        tipText = new BitmapText(font);
        tipText.setText("等待玩家加入");
        tipText.setColor(ColorRGBA.Black);
        tipText.setSize(ob * 0.03f);
        tipText.setLocalTranslation(width * 0.2f, 0, 0);

        infoText = new BitmapText(font);
        infoText.setText("");
        infoText.setColor(ColorRGBA.Black);
        infoText.setSize(ob * 0.02f);
        infoText.setLocalTranslation(width * 0.2f, height * 0.07f, 0);

        tipNode.attachChild(playerText);
        tipNode.attachChild(tipText);
        tipNode.attachChild(infoText);
        tipNode.attachChild(bgpicInfo);
        tipNode.setLocalTranslation(width * 0.05f, height * 0.1f, 0);

        roomNode.attachChild(tipNode);
    }

    private void room() {
        simpleApp.enqueue(new Runnable() {
            @Override
            public void run() {
                sceneNode.detachAllChildren();
                sceneNode.attachChild(roomNode);
            }
        });

    }

    public void updateBoard(final Map<Integer, String> stateMap) {
        simpleApp.enqueue(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Integer, String> entry : stateMap.entrySet()) {
                    Integer key = entry.getKey();
                    PieceState value = PieceState.valueOf(entry.getValue());
                    pieceMap.get(key).setState(value);
                }
            }
        });
    }

    public void show(int[] ids) {
        for (int id : ids) {
            pieceMap.get(id).show();
        }
    }

    private AssetManager getAssetManager() {
        return simpleApp.getAssetManager();
    }

    @Override
    public void messageReceived(Client source, final Message m) {
        LOG.log(Level.WARNING, "[Client]:{0}", m.toString());
        if (m instanceof ChessMessage) {
            ChessMessage chessMessage = (ChessMessage) m;
            updateBoard(chessMessage.getPieceStateMap());
            this.black = chessMessage.getBlack();
            this.blackExchange = chessMessage.getBlackExchange();
            this.blackRmCount = chessMessage.getBlackRmCount();
            this.white = chessMessage.getWhite();
            this.whiteExchange = chessMessage.getWhiteExchange();
            this.whiteRmCount = chessMessage.getWhiteRmCount();
            this.win = PieceState.valueOf(chessMessage.getWin());
            this.nextStep = PieceState.valueOf(chessMessage.getNextStep());
            this.process = GameProcess.valueOf(chessMessage.getProcess());

            updateTip();
        } else if (m instanceof ShowMessage) {
            ShowMessage showMessage = (ShowMessage) m;
            int[] pieces = showMessage.getPieces();
            for (int i = 0; i < pieces.length; i++) {
                pieceMap.get(pieces[i]).show();
            }
        } else if (m instanceof RoomListMessage) {
            simpleApp.enqueue(new Runnable() {
                @Override
                public void run() {
                    RoomListMessage roomListMessage = (RoomListMessage) m;
                    roomList(roomListMessage.getRoomIds(), roomListMessage.getStates());
                }
            });
        } else if (m instanceof RoomMessage) {
            RoomMessage roomMessage = (RoomMessage) m;
            if (roomMessage.getSuccess() == 0) {
                roomId = roomMessage.getRoomId();
                playerId = roomMessage.getPlayerId();
                playerType = PlayerType.valueOf(roomMessage.getType());
                room();
            }
        }
    }

    private void initAbout() {
        aboutNode = new Node("about Node");
        {
            Picture returnIoc = returnIoc();
            returnIoc.addControl(new MouseEventControl(new DefaultMouseListener() {
                @Override
                protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
                    if (event.isReleased()) {
                        main();
                    }
                }

            }));
            aboutNode.attachChild(returnIoc);
        }
        {
            Picture bgAbout = new Picture("bg about");
            bgAbout.setImage(getAssetManager(), "Textures/about.png", true);
            bgAbout.setWidth(width);
            bgAbout.setHeight(height);
            bgAbout.setLocalTranslation(0, 0, -10);
            aboutNode.attachChild(bgAbout);
        }

    }

    private void about() {
        simpleApp.enqueue(new Runnable() {
            @Override
            public void run() {
                sceneNode.detachAllChildren();
                sceneNode.attachChild(aboutNode);
            }
        });
    }

    StringBuilder tipSb = new StringBuilder();

    private void updateTip() {
        if (tipText == null || infoText == null) {
            return;
        }
        tipSb.setLength(0);

        if (nextStep == PieceState.white) {
            tipSb.append("白");
        } else {
            tipSb.append("黑");
        }
        tipSb.append("棋");
        if (null != process) {
            switch (process) {
                case Under:
                    tipSb.append("落子");
                    break;
                case Walk:
                    tipSb.append("走");
                    break;
                case Perform:
                    tipSb.append("吃").append(nextStep == PieceState.white ? whiteRmCount : blackRmCount).append("子");
                    break;
                case exchange:
                    tipSb.append("换子");
                    break;
                case end:
                    tipSb.setLength(0);
                    tipSb.append(win.name().equals(playerType.name()) ? "你赢了" : "你输了");
                default:
                    break;
            }
        }

        infoText.setText("余子[ 黑: " + black + "; 白:" + white + " ]");
        playerText.setText("玩家:" + (playerType == PlayerType.white ? "白" : "黑") + "棋");
        tipText.setText(tipSb.toString());
        tipSb.setLength(0);
    }

    @Override
    public void cleanup() {
        if (client != null) {
            client.close();
        }
        super.cleanup();
    }

}
