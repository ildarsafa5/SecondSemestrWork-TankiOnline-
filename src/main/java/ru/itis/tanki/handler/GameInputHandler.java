package ru.itis.tanki.handler;

import java.awt.event.*;

/**
 * Простой универсальный обработчик
 */
public class GameInputHandler implements KeyListener {

    private volatile boolean wPressed = false;
    private volatile boolean aPressed = false;
    private volatile boolean sPressed = false;
    private volatile boolean dPressed = false;
    private volatile boolean spaceJustPressed = false;
    private volatile boolean spacePressed = false;

    private final boolean isArrowKeys;

    public GameInputHandler(boolean useArrowKeys) {
        this.isArrowKeys = useArrowKeys;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isArrowKeys) {
            handleArrowKeys(e.getKeyCode(), true);
        } else {
            handleWASD(e.getKeyCode(), true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isArrowKeys) {
            handleArrowKeys(e.getKeyCode(), false);
        } else {
            handleWASD(e.getKeyCode(), false);
        }
    }

    private void handleWASD(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W:
                wPressed = pressed;
                break;
            case KeyEvent.VK_A:
                aPressed = pressed;
                break;
            case KeyEvent.VK_S:
                sPressed = pressed;
                break;
            case KeyEvent.VK_D:
                dPressed = pressed;
                break;
            case KeyEvent.VK_SPACE:
                if (pressed) {
                    if (!spacePressed) {
                        spaceJustPressed = pressed;
                    }
                    spacePressed = true;
                    break;
                } else {
                    spacePressed = false;
                    break;
                }
        }
    }

    private void handleArrowKeys(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                wPressed = pressed;
                break;
            case KeyEvent.VK_LEFT:
                aPressed = pressed;
                break;
            case KeyEvent.VK_DOWN:
                sPressed = pressed;
                break;
            case KeyEvent.VK_RIGHT:
                dPressed = pressed;
                break;
            case KeyEvent.VK_M:
                if (pressed) {
                    if (!spacePressed) {
                        spaceJustPressed = pressed;
                    }
                    spacePressed = true;
                } else {
                    spacePressed = false;
                }
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public boolean iswPressed() {
        return wPressed;
    }

    public boolean isaPressed() {
        return aPressed;
    }

    public boolean issPressed() {
        return sPressed;
    }

    public boolean isdPressed() {
        return dPressed;
    }

    public boolean isSpaceJustPressed() {
        return spaceJustPressed;
    }

    public void setSpaceJustPressed(boolean spaceJustPressed) {
        this.spaceJustPressed = spaceJustPressed;
    }
}