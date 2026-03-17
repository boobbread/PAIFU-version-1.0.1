package org.paifu;

import org.paifu.core.managers.EngineManager;
import org.paifu.core.managers.WindowManager;

import static org.paifu.core.Constants.*;

public class Launcher {

    private static WindowManager windowManager;

    /**
     * Pipeline - First creates instances of WindowManager and EngineManager,
     * then attempts to start the engine manager
     * @param args Unused, but will be later used to enter the game in developer mode
     */
    public static void main(String[] args) {

        windowManager = new WindowManager(TITLE + " - " + VERSION, WINDOW_WIDTH, WINDOW_HEIGHT, false);
        EngineManager engineManager = new EngineManager();

        try {
            engineManager.start();
        } catch (Exception e) {
            System.err.println("Error starting engine: " + e.getMessage());
        }
    }

    public static WindowManager getWindow() {
        return windowManager;
    }

    public static void setWindow(WindowManager window) {
        Launcher.windowManager = window;
    }
}
