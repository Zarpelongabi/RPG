package com.example.rpg_definitivo.backend.models; // Pacote do Android

import java.util.Random;

/**
 * ============================================================
 * Sword.java — Arma equipável do personagem
 * ============================================================
 */
public class Sword extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private int damage;
    private String type;
    private static final Random random = new Random();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Sword(String name, int value, int damage, String type, int size) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.damage = damage;
        this.type   = type;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public int getDamage() { return damage; }
    public String getType() { return type; }

    // =========================================================================
    // SETTERS (com validação)
    // =========================================================================

    public void setDamage(int damage) {
        if (damage > 0) {
            this.damage = damage;
        }
    }

    public void setType(String type) {
        if (type != null && !type.isBlank()) {
            this.type = type;
        }
    }

    // =========================================================================
    // COMBAT — DAMAGE CALCULATION
    // =========================================================================

    public int calculateDamage() {

        int totalDamage = 0;
        int roll        = random.nextInt(20) + 1;
        String weapon   = getName();

        // Substituído o 'switch' com '->' pelo clássico para compatibilidade no Android
        switch (weapon) {

            // ── ADAGA ─────────────────────────────────────────────────────
            case "Adaga":
                for (int i = 0; i < 2; i++) {
                    int attackRoll = random.nextInt(20) + 1;
                    int hit = damage;
                    if (attackRoll == 20) {
                        hit *= 2;
                    }
                    totalDamage += hit;
                }
                break;

            // ── KATANA ────────────────────────────────────────────────────
            case "Katana":
                int critMultiplier = 2; // Padrão: Comum
                if (type != null) {
                    switch (type) {
                        case "Rara":
                            critMultiplier = 3;
                            break;
                        case "Lendaria":
                            critMultiplier = 4;
                            break;
                    }
                }
                totalDamage = (roll == 20) ? damage * critMultiplier : damage;
                break;

            // ── ESPADA LONGA ──────────────────────────────────────────────
            case "Espada Longa":
                int critThreshold = 20; // Padrão: Comum
                if (type != null) {
                    switch (type) {
                        case "Rara":
                            critThreshold = 18;
                            break;
                        case "Lendaria":
                            critThreshold = 15;
                            break;
                    }
                }
                totalDamage = (roll >= critThreshold) ? damage * 2 : damage;
                break;

            // ── ARMAS PADRÃO ──────────────────────────────────────────────
            default:
                totalDamage = (roll == 20) ? damage * 2 : damage;
                break;
        }

        return totalDamage;
    }
}