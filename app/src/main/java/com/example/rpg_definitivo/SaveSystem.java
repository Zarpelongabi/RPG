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
        public int rota; // Adicionado: Rota atual

        public SaveSlot(String id, String name, float playerX, float playerY, int rota) {
            this.id = id;
            this.name = name;
            this.playerX = playerX;
            this.playerY = playerY;
            this.rota = rota;
        }

        public JSONObject toInterface() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("playerX", playerX);
            json.put("playerY", playerY);
            json.put("rota", rota);
            return json;
        }

        public static SaveSlot fromJSON(JSONObject json) throws JSONException {
            return new SaveSlot(
                json.getString("id"),
                json.getString("name"),
                (float) json.getDouble("playerX"),
                (float) json.getDouble("playerY"),
                json.optInt("rota", 1) // Padrão rota 1 se não existir
            );
        }
    }

    public static void salvarJogo(Context context, String slotId, String slotName, float x, float y, int rota) {
        List<SaveSlot> slots = carregarTodosSaves(context);
        boolean found = false;

        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).id.equals(slotId)) {
                slots.set(i, new SaveSlot(slotId, slotName, x, y, rota));
                found = true;
                break;
            }
        }

        if (!found) {
            slots.add(new SaveSlot(slotId, slotName, x, y, rota));
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
