package com.example.rpg_definitivo.backend.models; // Pacote atualizado para Android

/**
 * ============================================================
 * Armor.java — Armadura equipável do personagem
 * ============================================================
 */
public class Armor extends Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private int resistance;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Armor(String name, int value, int size, int resistance) {
        super(name, value, size);   // Repassa nome, valor e tamanho para Item
        this.resistance = resistance;
    }

    // =========================================================================
    // GETTER
    // =========================================================================

    public int getResistance() {
        return resistance;
    }

    // =========================================================================
    // SETTER (com validação)
    // =========================================================================

    public void setResistance(int resistance) {
        if (resistance >= 0) {
            this.resistance = resistance;
        }
    }
}