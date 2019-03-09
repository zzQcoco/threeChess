/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pri.zzq.threechess.main;

import pri.zzq.threechess.def.PieceState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author zzQ
 */
public class PiecesControl extends AbstractControl {

    private int id;
    private PieceState state = PieceState.none;
    

    private final float interval = 0.25f;
    private final int count = 6;
    private boolean show = false;

    public PiecesControl(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void setState(PieceState state) {
        this.state = state;
        updatePiece();
    }

    public void updatePiece() {
        switch (state) {
            case none:
                spatial.setLocalTranslation(spatial.getLocalTranslation().setZ(-10));
                if (spatial instanceof Geometry) {
                    Geometry geometry = (Geometry) spatial;
                    geometry.getMaterial().setColor("Color", ColorRGBA.White);
                }
                break;
            case white:
                setColor(ColorRGBA.White);
                break;
            case black:
                setColor(ColorRGBA.Black);
                break;
            default:
                break;

        }
    }

    public void setColor(ColorRGBA color) {
        spatial.setLocalTranslation(spatial.getLocalTranslation().setZ(10));
        if (spatial instanceof Geometry) {
            Geometry geo = (Geometry) spatial;
            geo.getMaterial().setColor("Color", color);
            if (geo.getMaterial().isTransparent()) {
                geo.getMaterial().setTransparent(false);
            }
        }
    }

    public void show() {
        show = true;
    }

    private float tempTime = 0f;
    private int tempCount = 0;

    private void showAnim(float tpf) {
        if (show) {
            tempTime += tpf;
            if (tempTime >= interval) {
                tempTime = 0f;
                tempCount++;
                ColorRGBA color = state == PieceState.white ? ColorRGBA.White : ColorRGBA.Black;
                if (tempCount > count) {
                    tempCount = 0;
                    show = false;
                    setColor(color);
                } else {
                    setColor(tempCount % 2 == 1 ? ColorRGBA.Red : color);
                }
            }
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        showAnim(tpf);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public PieceState getState() {
        return state;
    }

    @Override
    public String toString() {
        return spatial.getName() + "-" + state.name();
    }

}
