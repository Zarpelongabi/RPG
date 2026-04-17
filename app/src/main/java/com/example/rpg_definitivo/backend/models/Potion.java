package com.example.rpg_definitivo.backend.models; // Pacote atualizado para o Android

/**
 * ============================================================
 * Potion.java — Item consumível de cura
 * ============================================================
 */
public class Potion extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private int healedLife;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Potion(String name, int value, int size, int healedLife) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.healedLife = healedLife;
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    public int getHealedLife() {
        return healedLife;
    }

    // =========================================================================
    // SETTER (com validação)
    // =========================================================================

    public void setHealedLife(int healedLife) {
        if (healedLife > 0) {
            this.healedLife = healedLife;
        }
    }
}