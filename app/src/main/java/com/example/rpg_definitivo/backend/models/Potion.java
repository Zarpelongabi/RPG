package com.example.rpg_definitivo.backend.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Potion extends Item {
    private int healedLife;

    public Potion(String name, int value, int size, int healedLife) {
        super(name, value, size);
        this.healedLife = healedLife;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("healedLife", healedLife);
        return json;
    }

    public int getHealedLife() { return healedLife; }

    public void setHealedLife(int healedLife) {
        if (healedLife > 0) this.healedLife = healedLife;
    }
}
