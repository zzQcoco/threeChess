/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.main;

import pri.zzq.threechess.client.ClientManager;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioNode;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.event.TouchAppState;
import java.util.logging.Level;
import java.util.logging.Logger;
import pri.zzq.threechess.Globals;

/**
 *
 * @author zzQ
 */
public class Game extends SimpleApplication {

    private ClientManager clientManager;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(300, 500);
        settings.setFrameRate(60);
        settings.setSamples(8);
        settings.setTitle(Globals.GAME_NAME);
        Game game = new Game();
        game.setSettings(settings);
        game.setShowSettings(false);

        game.start();
    }

    @Override
    public void simpleInitApp() {
        Logger.getGlobal().setLevel(Level.SEVERE);
        flyCam.setEnabled(false);
        inputManager.setCursorVisible(true);
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        this.setPauseOnLostFocus(false);
        
        AudioNode audioNode = new AudioNode(assetManager, "Sounds/bgSound.wav", AudioData.DataType.Buffer);
        audioNode.setPositional(false);
        audioNode.setPositional(false);
        audioNode.setVolume(3);
        audioNode.setLooping(true);
        audioRenderer.playSource(audioNode);

        stateManager.attach(new MouseAppState(this));
        stateManager.attach(new TouchAppState(this));
        clientManager = new ClientManager();
        stateManager.attach(clientManager);
    }

}
