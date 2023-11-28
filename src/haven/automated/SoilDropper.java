package haven.automated;

import haven.*;

import java.util.Objects;

import static haven.OCache.posres;

public class SoilDropper extends Window implements Runnable {
    private final GameUI gui;
    private boolean stop;
    private boolean active;
    private final Button startButton;
    private ISBox isBox;

    public SoilDropper(GameUI gameUI) {
        super(UI.scale(120, 140), "Soil Dropper");
        this.gui = gameUI;
        this.stop = false;
        this.active = false;



        startButton = add(new Button(UI.scale(50), "Start") {
            @Override
            public void click() {
                active = !active;
                if (active) {
                    this.change("Stop");
                    GameUI.dropAllSoil = true;
                } else {
                    this.change("Start");
                    GameUI.dropAllSoil = false;
                }
            }
        }, UI.scale(30, 100));



    }


    @Override
    public void run() {
        while (!stop) {
            if (active) {
                for (Widget widget : ui.rwidgets.keySet()) {
                    if (widget instanceof ISBox) {
                        isBox = (ISBox) widget;
                        break;
                    }
                }
                if(isBox != null && isBox.count != null && isBox.count > 20){
                    int number = isBox.count - 20;
                    for (int i = 0; i < number; i++) {
                        isBox.wdgmsg("xfer2", -1, -1);
                    }
                }
            }
            sleep(2000);
        }
    }

    private void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException ignored) {
        }
    }



    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if ((sender == this) && (Objects.equals(msg, "close"))) {
            stop = true;
            stop();
            reqdestroy();
            gui.soilDropper = null;
            gui.soilDropperThread = null;
            GameUI.dropAllSoil = false;
        } else
            super.wdgmsg(sender, msg, args);
    }

    public void stop() {
        gui.map.wdgmsg("click", Coord.z, gui.map.player().rc.floor(posres), 1, 0);
        if (gui.map.pfthread != null) {
            gui.map.pfthread.interrupt();
        }
        this.destroy();
    }
}
