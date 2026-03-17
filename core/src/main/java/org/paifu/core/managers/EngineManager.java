package org.paifu.core.managers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.paifu.Launcher;

/**
 * Manages the life cycle of the game eg. updating and rendering everything and handling input
 */
public class EngineManager {

    public static final long NANOSECOND = 1000000000L;
    private boolean isRunning;
    private WindowManager window;
    private GLFWErrorCallback errorCallback;

    private void init() {
        GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        window = Launcher.getWindow();
    }

    public void start() throws Exception {
        init();
        if (isRunning) return;
        run();
    }

    public void run() throws Exception {
        this.isRunning = true;
        long lastTime = System.nanoTime();

        while (isRunning) {
            long now = System.nanoTime();
            float delta = (now - lastTime) / (float) NANOSECOND;
            lastTime = now;

            GLFW.glfwPollEvents();

            update(delta);
            render();

            GLFW.glfwSwapBuffers(window.getWindow());

            if (window.windowShouldClose()) {
                stop();
            }
        }

        cleanup();
    }

    public void stop() {
        if(!isRunning) return;
        isRunning = false;
    }

    public void render() throws Exception {
        window.update();
    }

    private void update(float interval) {
        // When we add the game logic, mouse and keyboard input etc. it will be called here
    }

    private void cleanup() {
        window.cleanUp();
        errorCallback.free();
        GLFW.glfwTerminate();
    }
}
