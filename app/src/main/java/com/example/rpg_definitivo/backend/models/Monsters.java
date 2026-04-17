package com.example.rpg_definitivo.backend.models;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Monsters.java — Classe base de todos os inimigos do jogo
 * ============================================================
 * NOTA DE MIGRAÇÃO (Android):
 * Os campos de imagem (imagePath e battleImagePath) foram
 * alterados de String para int (IDs do Android).
 */
public class Monsters {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private String name;
    private int life;
    private int maxLife;
    private int damage;
    private int dropCoin;
    private int dropXp;
    private int speed;
    private int resistance;

    /** ID da drawable usada no mapa (Android) */
    protected int imageResId;

    /** ID da drawable usada na batalha (Android) */
    protected int battleImageResId;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Monsters(String name, int life, int damage,
                    int dropCoin, int dropXp, int speed, int resistance) {
        this.name       = name;
        this.life       = life;
        this.maxLife    = life;
        this.damage     = damage;
        this.dropCoin   = dropCoin;
        this.dropXp     = dropXp;
        this.speed      = speed;
        this.resistance = resistance;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName()         { return name; }
    public int getLife()            { return life; }
    public int getMaxLife()         { return maxLife; }
    public int getDamage()          { return damage; }
    public int getDropCoin()        { return dropCoin; }
    public int getDropXp()          { return dropXp; }
    public int getSpeed()           { return speed; }
    public int getResistance()      { return resistance; }

    public int getImageResId()       { return imageResId; }
    public int getBattleImageResId() { return battleImageResId; }

    // =========================================================================
    // SETTERS
    // =========================================================================

    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    // =========================================================================
    // COMBAT
    // =========================================================================

    public void attack(Character target) {
        int finalDamage = Math.max(0, this.damage - target.getResistance());
        target.setLife(target.getLife() - finalDamage);
    }

    // =========================================================================
    // FACTORY METHODS
    // =========================================================================

    public static List<Monsters> createGoblins(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new Goblin());
        }
        return list;
    }

    public static List<Monsters> createGoblinsExp(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new GoblinExp());
        }
        return list;
    }

    public static List<Monsters> createGoblinBosses(int amount) {
        List<Monsters> list = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            list.add(new BossGoblin());
        }
        return list;
    }
}