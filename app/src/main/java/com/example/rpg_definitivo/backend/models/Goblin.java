package com.example.rpg_definitivo.backend.models;

import com.example.rpg_definitivo.R;

public class Goblin extends Monsters {

    public Goblin() {
        super(
                "Goblin", // name
                8,        // life
                2,        // damage
                3,        // dropCoin
                2,        // dropXp
                20,       // speed
                0         // resistance (sem resistência)
        );

        // No Android, referenciamos imagens por IDs numéricos.
        // Zero (0) é usado temporariamente até colocarmos as imagens na pasta res/drawable.
        this.imageResId       = R.drawable.sprite_goblin;
        this.battleImageResId = R.drawable.sprite_goblin;
    }
}