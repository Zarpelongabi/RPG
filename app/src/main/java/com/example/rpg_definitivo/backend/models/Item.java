package com.example.rpg_definitivo.backend.models;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private int value;
    private int size;

    public Item(String name, int value, int size) {
        this.name = name;
        this.value = value;
        this.size = size;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", getClass().getSimpleName());
        json.put("name", name);
        json.put("value", value);
        json.put("size", size);
        return json;
    }

    public static Item fromJSON(JSONObject json) throws JSONException {
        String type = json.getString("type");
        String name = json.getString("name");
        int value = json.getInt("value");
        int size = json.getInt("size");

        if (type.equals("Potion")) {
            int healedLife = json.getInt("healedLife");
            return new Potion(name, value, size, healedLife);
        } else if (type.equals("Sword")) {
            int damage = json.getInt("damage");
            String typeAttr = json.getString("typeAttr");
            return new Sword(name, value, damage, typeAttr, size);
        }
        return new Item(name, value, size);
    }

    public String getName() { return name; }
    public int getValue() { return value; }
    public int getSize() { return size; }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) this.name = name;
    }

    public void setValue(int value) {
        if (value >= 0) this.value = value;
    }

    public void setSize(int size) {
        if (size > 0) this.size = size;
    }
}
