package ru.itis.tanki.model;

/**
 * Простой класс пули
 */
public class Bullet {
    private float x, y;
    private float velX, velY;
    private int ownerId;

    public Bullet(float x, float y, float velX, float velY, int ownerId) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.ownerId = ownerId;
    }

    public void update() {
        x += velX;
        y += velY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelX() {
        return velX;
    }

    public float getVelY() {
        return velY;
    }

    public int getOwnerId() {
        return ownerId;
    }
}