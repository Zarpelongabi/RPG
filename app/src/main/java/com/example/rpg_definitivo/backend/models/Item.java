package com.example.rpg_definitivo.backend.models; // Pacote atualizado para o Android

/**
 * ============================================================
 * Item.java — Classe base para TODOS os itens do jogo
 * ============================================================
 */
public class Item {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private String name;
    private int value;
    private int size;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public Item(String name, int value, int size) {
        this.name  = name;
        this.value = value;
        this.size  = size;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getName()  { return name; }
    public int getValue()    { return value; }
    public int getSize()     { return size; }

    // =========================================================================
    // SETTERS (com validação)
    // =========================================================================

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void setValue(int value) {
        if (value >= 0) {
            this.value = value;
        }
    }

    public void setSize(int size) {
        if (size > 0) {
            this.size = size;
        }
    }
}