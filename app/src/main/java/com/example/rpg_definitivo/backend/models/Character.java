package com.example.rpg_definitivo.backend.models; // Pacote de modelos do Android

import java.util.List;
import java.util.Random;

import com.example.rpg_definitivo.backend.managers.Inventory; // Import corrigido

/**
 * ============================================================
 * Character.java — Personagem principal do jogador (Versão Android)
 * ============================================================
 * NOTA DE MIGRAÇÃO:
 * As variáveis 'Image' do JavaFX foram trocadas por 'int'.
 * No Android, referenciamos imagens por seus IDs de recurso (ex: R.drawable.hero_sprite).
 */
public class Character {

    // =========================================================================
    // FIELDS — Informações básicas
    // =========================================================================

    private String name;
    private int life;
    private int maxLife;
    private int coin;

    /** Resistência BASE do personagem (sem contar a armadura equipada). */
    private int baseResistance;

    private Inventory inventory;

    // =========================================================================
    // FIELDS — Sistema de level
    // =========================================================================

    private int level         = 1;
    private int xp            = 0;
    private int xpNecessary   = 10;

    // =========================================================================
    // FIELDS — Equipamentos
    // =========================================================================

    private Sword sword;
    private Armor equippedArmor;

    // =========================================================================
    // FIELDS — Sprites (Adaptado para Android)
    // =========================================================================

    /** ID do Drawable usado no mapa (ex: R.drawable.hero_map) */
    private int sprite;

    /** ID do Drawable exibido na tela de batalha (ex: R.drawable.hero_battle) */
    private int battleSprite;

    private static final Random random = new Random();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Character(String name, int life, int baseResistance,
                     Sword sword, int sprite, int battleSprite) {
        this.name           = name;
        this.life           = life;
        this.maxLife        = life;
        this.baseResistance = baseResistance;
        this.coin           = 0;
        this.inventory      = new Inventory();
        this.sword          = sword;
        this.sprite         = sprite;
        this.battleSprite   = battleSprite;
        this.equippedArmor  = null;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName()         { return name; }
    public int getLife()            { return life; }
    public int getMaxLife()         { return maxLife; }
    public int getCoin()            { return coin; }
    public Inventory getInventory() { return inventory; }
    public int getNivel()           { return level; }
    public int getXp()              { return xp; }
    public int getXpNecessary()     { return xpNecessary; }
    public int getMaxXp()           { return xpNecessary; }
    public Sword getSword()         { return sword; }

    // Retornam int agora (IDs do Android)
    public int getSprite()          { return sprite; }
    public int getBattleSprite()    { return battleSprite; }

    public Armor getEquippedArmor() { return equippedArmor; }

    public int getResistance() {
        int total = baseResistance;
        if (equippedArmor != null) {
            total += equippedArmor.getResistance();
        }
        return total;
    }

    // =========================================================================
    // SETTERS
    // =========================================================================

    public void setNivel(int level)      { this.level = level; }
    public void setCoin(int coin)        { this.coin  = coin;  }
    public void setXp(int xp)            { this.xp    = xp;    }
    public void setSword(Sword sword)    { this.sword = sword; }
    public void setEquippedArmor(Armor armor) { this.equippedArmor = armor; }

    // =========================================================================
    // HEALTH MANAGEMENT
    // =========================================================================

    public void setLife(int life) {
        this.life = Math.max(0, Math.min(life, maxLife));
    }

    public boolean isAlive() {
        return this.life > 0;
    }

    public void heal(int amount) {
        if (amount > 0) {
            setLife(this.life + amount);
        }
    }

    // =========================================================================
    // ECONOMY
    // =========================================================================

    public void addCoin(int amount) {
        if (amount > 0) {
            this.coin += amount;
        }
    }

    public boolean removeCoin(int amount) {
        if (amount > 0 && this.coin >= amount) {
            this.coin -= amount;
            return true;
        }
        return false;
    }

    // =========================================================================
    // XP & LEVELING
    // =========================================================================

    public boolean earnXp(int amount) {
        if (amount <= 0) return false;
        xp += amount;
        return calculateLevel();
    }

    private boolean calculateLevel() {
        boolean leveledUp = false;

        while (xp >= xpNecessary && level < 10) {
            xp          -= xpNecessary;
            level++;
            xpNecessary  = (int)(xpNecessary * 1.5);
            life         = maxLife;

            if (level == 5 || level == 10) {
                inventory.increaseSpace(5);
            }
            leveledUp = true;
        }

        if (level == 10 && xp >= xpNecessary) {
            xp = xpNecessary;
        }

        return leveledUp;
    }

    // =========================================================================
    // COMBAT
    // =========================================================================

    public int attack(Monsters target) {
        if (target == null || target.getLife() <= 0 || sword == null) return 0;

        int rawDamage   = sword.calculateDamage();
        int finalDamage = Math.max(0, rawDamage - target.getResistance());
        target.setLife(target.getLife() - finalDamage);
        return finalDamage;
    }

    public int attackArea(List<Monsters> targets) {
        if (targets == null || targets.isEmpty() || sword == null) return 0;

        int baseDamage       = sword.calculateDamage() / 2;
        int totalDamageDealt = 0;

        for (int i = 0; i < targets.size(); i++) {
            Monsters target     = targets.get(i);
            int finalDamage     = Math.max(0, baseDamage - target.getResistance());
            target.setLife(target.getLife() - finalDamage);
            totalDamageDealt   += finalDamage;

            if (target.getLife() <= 0) {
                targets.remove(i);
                i--;
            }
        }
        return totalDamageDealt;
    }

    public boolean leave() {
        return random.nextInt(20) + 1 > 10;
    }
}