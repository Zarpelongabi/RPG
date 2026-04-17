package com.example.rpg_definitivo.backend.models;

import com.example.rpg_definitivo.R;

public class GoblinExp extends Monsters {
    public GoblinExp() {
        super(
                "GoblinExp", // name
                13,          // life
                99,          // damage (alto de propósito - cuidado!)
                5,           // dropCoin
                4,           // dropXp
                20,          // speed
                1            // resistance
        );

        // Aqui está a mágica do Android! Usamos o R.drawable.nome_do_arquivo (sem o .png)
        this.imageResId       = R.drawable.sprite_goblinexperiente;
        this.battleImageResId = R.drawable.sprite_goblin;
    }
}