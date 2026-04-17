package com.example.rpg_definitivo.backend.managers; // Pacote corrigido para o Android

import java.util.ArrayList;
import java.util.List;

import com.example.rpg_definitivo.backend.models.Item; // Import corrigido para o Android

/**
 * ============================================================
 * Inventory.java — Mochila do personagem (Versão Android)
 * ============================================================
 *
 * RESPONSABILIDADE:
 * Gerencia a coleção de itens carregados pelo personagem.
 * Controla capacidade máxima de slots e evita overflow.
 */
public class Inventory {

    // =========================================================================
    // FIELDS
    // =========================================================================

    /** Número máximo de slots disponíveis. Começa em 20 e pode aumentar. */
    private int maxSpace = 20;

    /** Slots atualmente ocupados pelos itens na mochila. */
    private int usedSpace = 0;

    /** Lista de itens armazenados na mochila. */
    private List<Item> items = new ArrayList<>();

    // =========================================================================
    // ITEM MANAGEMENT
    // =========================================================================

    /**
     * Tenta adicionar um item à mochila.
     *
     * @param item Item a adicionar (não pode ser null).
     * @return true se adicionado com sucesso; false se o item é null ou não há espaço.
     */
    public boolean addItem(Item item) {
        if (item == null) return false;

        if (usedSpace + item.getSize() <= maxSpace) {
            items.add(item);
            usedSpace += item.getSize();
            return true;
        }

        return false; // Inventário cheio
    }

    /**
     * Remove um item da mochila e libera os slots ocupados.
     *
     * @param item Item a remover (deve estar presente na mochila).
     * @return true se removido com sucesso; false se o item não estava na mochila.
     */
    public boolean removeItem(Item item) {
        if (items.remove(item)) {
            usedSpace -= item.getSize();
            return true;
        }
        return false;
    }

    // =========================================================================
    // CAPACITY MANAGEMENT
    // =========================================================================

    /**
     * Aumenta o número máximo de slots do inventário.
     * Chamado ao subir de nível (levels 5 e 10).
     *
     * @param amount Quantidade de slots a adicionar (deve ser > 0).
     */
    public void increaseSpace(int amount) {
        if (amount > 0) {
            maxSpace += amount;
        }
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public int getMaxSpace() { return maxSpace; }
    public int getUsedSpace() { return usedSpace; }
    public int getFreeSpace() { return maxSpace - usedSpace; }
    public List<Item> getItems() { return items; }
}