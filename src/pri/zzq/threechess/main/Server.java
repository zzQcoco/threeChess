/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.main;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import pri.zzq.threechess.server.ServerManager;

/**
 *
 * @author zzQ
 */
public class Server extends SimpleApplication {

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(60);
        settings.setRenderer(null);
        //FIXME: strange way of setting null audio renderer..
        settings.setAudioRenderer(null);
        Server app = new Server();
        app.setShowSettings(false);
        app.setDisplayFps(false);
        app.setDisplayStatView(false);
        app.setPauseOnLostFocus(false);
        app.setSettings(settings);
        app.start(JmeContext.Type.Display);
    }
    
    @Override
    public void simpleInitApp() {
        stateManager.attach(new ServerManager());
        System.out.println("server start!");
    }
    
}
