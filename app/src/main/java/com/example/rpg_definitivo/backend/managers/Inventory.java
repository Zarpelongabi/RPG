package com.example.rpg_definitivo.backend.managers;

import com.example.rpg_definitivo.backend.models.Item;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Inventory implements Serializable {
    private int maxSpace = 20;
    private int usedSpace = 0;
    private List<Item> items = new ArrayList<>();

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("maxSpace", maxSpace);
        JSONArray itemsArray = new JSONArray();
        for (Item item : items) {
            itemsArray.put(item.toJSON());
        }
        json.put("items", itemsArray);
        return json;
    }

    public static Inventory fromJSON(String jsonStr) {
        Inventory inv = new Inventory();
        if (jsonStr == null || jsonStr.isEmpty() || jsonStr.equals("{}")) return inv;
        try {
            JSONObject json = new JSONObject(jsonStr);
            inv.maxSpace = json.optInt("maxSpace", 20);
            JSONArray itemsArray = json.optJSONArray("items");
            if (itemsArray != null) {
                for (int i = 0; i < itemsArray.length(); i++) {
                    inv.addItem(Item.fromJSON(itemsArray.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return inv;
    }

    public boolean addItem(Item item) {
        if (item == null) return false;
        if (usedSpace + item.getSize() <= maxSpace) {
            items.add(item);
            usedSpace += item.getSize();
            return true;
        }
        return false;
    }

    public boolean removeItem(Item item) {
        if (items.remove(item)) {
            usedSpace -= item.getSize();
            return true;
        }
        return false;
    }

    public void increaseSpace(int amount) {
        if (amount > 0) {
            maxSpace += amount;
        }
    }

    public int getMaxSpace() { return maxSpace; }
    public int getUsedSpace() { return usedSpace; }
    public int getFreeSpace() { return maxSpace - usedSpace; }
    public List<Item> getItems() { return items; }
}
