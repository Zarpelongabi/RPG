package com.example.rpg_definitivo.backend.models;

import com.example.rpg_definitivo.R;

// O nome da classe TEM que ser igual ao nome do arquivo (BossGoblin)
public class BossGoblin extends Monsters {

    // O nome do construtor TEM que ser igual ao da classe
    public BossGoblin() {
        super(
                "Goblin Boss",
                50,   // life
                15,   // damage
                50,   // dropCoin
                100,  // dropXp
                15,   // speed
                5     // resistance
        );

        this.imageResId       = R.drawable.sprite_baginga;
        this.battleImageResId = R.drawable.sprite_goblin;
    }
}