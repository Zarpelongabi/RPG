package com.example.rpg_definitivo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SaveSystem {

    private static final String PREF_NAME = "RPG_SAVE_SYSTEM";
    private static final String SLOTS_KEY = "save_slots";

    public static class SaveSlot {
        public String id;
        public String name;
        public float playerX;
        public float playerY;
        public int rota;
        public int hp;
        public int maxHp;
        public int level;
        public int xp;
        public int coins;
        public boolean[][] defeatedEnemies;

        public SaveSlot(String id, String name, float playerX, float playerY, int rota, int hp, int maxHp, int level, int xp, int coins, boolean[][] defeatedEnemies) {
            this.id = id;
            this.name = name;
            this.playerX = playerX;
            this.playerY = playerY;
            this.rota = rota;
            this.hp = hp;
            this.maxHp = maxHp;
            this.level = level;
            this.xp = xp;
            this.coins = coins;
            this.defeatedEnemies = defeatedEnemies;
        }

        public JSONObject toInterface() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("playerX", playerX);
            json.put("playerY", playerY);
            json.put("rota", rota);
            json.put("hp", hp);
            json.put("maxHp", maxHp);
            json.put("level", level);
            json.put("xp", xp);
            json.put("coins", coins);
            
            JSONArray defeatedArray = new JSONArray();
            if (defeatedEnemies != null) {
                for (boolean[] row : defeatedEnemies) {
                    JSONArray rowArray = new JSONArray();
                    for (boolean val : row) {
                        rowArray.put(val);
                    }
                    defeatedArray.put(rowArray);
                }
            }
            json.put("defeatedEnemies", defeatedArray);
            
            return json;
        }

        public static SaveSlot fromJSON(JSONObject json) throws JSONException {
            boolean[][] defeated = new boolean[10][10];
            if (json.has("defeatedEnemies")) {
                JSONArray defeatedArray = json.getJSONArray("defeatedEnemies");
                for (int i = 0; i < Math.min(defeatedArray.length(), 10); i++) {
                    JSONArray rowArray = defeatedArray.getJSONArray(i);
                    for (int j = 0; j < Math.min(rowArray.length(), 10); j++) {
                        defeated[i][j] = rowArray.getBoolean(j);
                    }
                }
            }

            return new SaveSlot(
                json.getString("id"),
                json.getString("name"),
                (float) json.getDouble("playerX"),
                (float) json.getDouble("playerY"),
                json.optInt("rota", 1),
                json.optInt("hp", 100),
                json.optInt("maxHp", 100),
                json.optInt("level", 1),
                json.optInt("xp", 0),
                json.optInt("coins", 0),
                defeated
            );
        }
    }

    public static void salvarJogo(Context context, String slotId, String slotName, float x, float y, int rota, int hp, int maxHp, int level, int xp, int coins, boolean[][] defeatedEnemies) {
        List<SaveSlot> slots = carregarTodosSaves(context);
        boolean found = false;

        SaveSlot newSlot = new SaveSlot(slotId, slotName, x, y, rota, hp, maxHp, level, xp, coins, defeatedEnemies);

        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).id.equals(slotId)) {
                slots.set(i, newSlot);
                found = true;
                break;
            }
        }

        if (!found) {
            slots.add(newSlot);
        }

        salvarLista(context, slots);
    }

    public static List<SaveSlot> carregarTodosSaves(Context context) {
        List<SaveSlot> lista = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(SLOTS_KEY, "[]");

        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                lista.add(SaveSlot.fromJSON(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void excluirSave(Context context, String slotId) {
        List<SaveSlot> slots = carregarTodosSaves(context);
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).id.equals(slotId)) {
                slots.remove(i);
                break;
            }
        }
        salvarLista(context, slots);
    }

    private static void salvarLista(Context context, List<SaveSlot> slots) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray array = new JSONArray();
        try {
            for (SaveSlot s : slots) {
                array.put(s.toInterface());
            }
            editor.putString(SLOTS_KEY, array.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SaveSlot carregarSlot(Context context, String slotId) {
        for (SaveSlot s : carregarTodosSaves(context)) {
            if (s.id.equals(slotId)) return s;
        }
        return null;
    }
}
