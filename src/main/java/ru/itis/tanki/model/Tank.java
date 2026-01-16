package ru.itis.tanki.model;


public class Tank {
    private int id;
    private String name;

    // Позиция и угол
    private float x, y;
    private float angle = 0; // в радианах, 0 = смотрит вправо

    // Состояние
    private int health = 100;
    private boolean alive = true;

    // Характеристики
    private float maxSpeed = 3.0f;
    private float rotationSpeed = 0.05f;
    private float speed = 0;
    private int kills = 0;
    private int deaths = 0;

    // Размер
    private float width = 40;
    private float height = 30;

    // Управление (устанавливается извне)
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean turningLeft = false;
    private boolean turningRight = false;
    private boolean wantToShoot = false;

    // Стрельба
    private boolean canShoot = true;
    private long lastShotTime = 0;
    private long reloadTime = 2000; // 2 секунды

    public Tank(int id, String name, float x, float y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public void update() {
        // 1. Поворот
        if (turningLeft) angle -= rotationSpeed;
        if (turningRight) angle += rotationSpeed;

        while (angle > Math.PI * 2) angle -= Math.PI * 2;
        while (angle < 0) angle += Math.PI * 2;

        // Движение
        if (movingForward) {
            speed = maxSpeed;
        } else if (movingBackward) {
            speed = -maxSpeed * 0.7f;
        } else {
            // Плавное торможение
            speed *= 0.9f;
            if (Math.abs(speed) < 0.1f) speed = 0;
        }

        // 3. Перемещение
        float moveX = (float)Math.cos(angle) * speed;
        float moveY = (float)Math.sin(angle) * speed;

        x += moveX;
        y += moveY;

        // 4. Перезарядка
        if (!canShoot && System.currentTimeMillis() - lastShotTime > reloadTime) {
            canShoot = true;
        }
    }


    public Bullet shoot() {
        if (!canShoot || !alive) return null;

        canShoot = false;
        lastShotTime = System.currentTimeMillis();

        // Позиция дула (немного впереди танка)
        float barrelLength = width / 2 + 10;
        float bulletX = x + (float)Math.cos(angle) * barrelLength;
        float bulletY = y + (float)Math.sin(angle) * barrelLength;

        // Направление
        float bulletSpeed = 8.0f;
        float velX = (float)Math.cos(angle) * bulletSpeed;
        float velY = (float)Math.sin(angle) * bulletSpeed;

        return new Bullet(bulletX, bulletY, velX, velY, id);
    }

    public void takeDamage(int damage) {
        if (!alive) return;

        health -= damage;
        if (health <= 0) {
            health = 0;
            alive = false;
        }
    }


    public void respawn(float newX, float newY) {
        x = newX;
        y = newY;
        angle = 0;
        speed = 0;
        health = 100;
        alive = true;
        canShoot = true;
    }

    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public String getName() {
        return name;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setMovingForward(boolean movingForward) {
        this.movingForward = movingForward;
    }

    public void setMovingBackward(boolean movingBackward) {
        this.movingBackward = movingBackward;
    }

    public void setTurningLeft(boolean turningLeft) {
        this.turningLeft = turningLeft;
    }

    public void setTurningRight(boolean turningRight) {
        this.turningRight = turningRight;
    }

    public boolean isWantToShoot() {
        return wantToShoot;
    }

    public void setWantToShoot(boolean wantToShoot) {
        this.wantToShoot = wantToShoot;
    }
}