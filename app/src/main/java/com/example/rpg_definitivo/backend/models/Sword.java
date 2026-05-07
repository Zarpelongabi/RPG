package com.example.rpg_definitivo.backend.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class Sword extends Item {
    private int damage;
    private String type;
    private static final Random random = new Random();

    public Sword(String name, int value, int damage, String type, int size) {
        super(name, value, size);
        this.damage = damage;
        this.type = type;
    }

    public int getDamage() { return damage; }
    public String getType() { return type; }

    public void setDamage(int damage) {
        if (damage > 0) this.damage = damage;
    }

    public void setType(String type) {
        if (type != null && !type.isEmpty()) this.type = type;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("damage", damage);
        json.put("typeAttr", type); // Use typeAttr to avoid conflict with "type" field in Item.toJSON
        return json;
    }

    public int calculateDamage() {
        int totalDamage = 0;
        int roll = random.nextInt(20) + 1;
        String weapon = getName();

        if (weapon.equalsIgnoreCase("Adaga")) {
            for (int i = 0; i < 2; i++) {
                int attackRoll = random.nextInt(20) + 1;
                int hit = damage;
                if (attackRoll == 20) hit *= 2;
                totalDamage += hit;
            }
        } else if (weapon.equalsIgnoreCase("Katana")) {
            int critMultiplier;
            if ("Rara".equalsIgnoreCase(type)) critMultiplier = 3;
            else if ("Lendaria".equalsIgnoreCase(type)) critMultiplier = 4;
            else critMultiplier = 2;
            totalDamage = (roll == 20) ? damage * critMultiplier : damage;
        } else if (weapon.equalsIgnoreCase("Espada Longa")) {
            int critThreshold;
            if ("Rara".equalsIgnoreCase(type)) critThreshold = 18;
            else if ("Lendaria".equalsIgnoreCase(type)) critThreshold = 15;
            else critThreshold = 20;
            totalDamage = (roll >= critThreshold) ? damage * 2 : damage;
        } else {
            totalDamage = (roll == 20) ? damage * 2 : damage;
        }

        return totalDamage;
    }
}
