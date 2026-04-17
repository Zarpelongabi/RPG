package com.example.rpg_definitivo.backend.managers; // Pacote corrigido

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import com.example.rpg_definitivo.backend.models.Character; // Import corrigido

/**
 * ============================================================
 * SaveManager.java — Sistema de save/load do jogo (Versão Android)
 * ============================================================
 * * NOTA: No Android, usamos 'context.getFilesDir()' para garantir
 * que o arquivo seja salvo na memória interna segura do app.
 */
public class SaveManager {

    private static final String KEY_MAP      = "mapaAtual";
    private static final String KEY_POS_X    = "posicaoX";
    private static final String KEY_POS_Y    = "posicaoY";
    private static final String KEY_LIFE     = "vidaPlayer";
    private static final String KEY_LEVEL    = "levelPlayer";
    private static final String KEY_GOLD     = "ouroPlayer";
    private static final String KEY_ENEMIES  = "inimigosMortos";

    public static class SaveData {
        public int mapa;
        public double posX;
        public double posY;
        public int vida;
        public int level;
        public int ouro;
        public boolean[][] inimigosDerrotados;
    }

    /**
     * Salva o jogo.
     * No Android, o 'fileName' deve ser apenas o nome (ex: "save1.json")
     * e o File objeto cuida do caminho completo.
     */
    public static void salvar(File filesDir, String fileName, int currentMap, double playerX, double playerY, Character player, boolean[][] defeatedEnemies) {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_MAP,     String.valueOf(currentMap));
            props.setProperty(KEY_POS_X,   String.valueOf(playerX));
            props.setProperty(KEY_POS_Y,   String.valueOf(playerY));
            props.setProperty(KEY_LIFE,    String.valueOf(player.getLife()));
            props.setProperty(KEY_LEVEL,   String.valueOf(player.getNivel()));
            props.setProperty(KEY_GOLD,    String.valueOf(player.getCoin()));
            props.setProperty(KEY_ENEMIES, serializeDefeatedEnemies(defeatedEnemies));

            File file = new File(filesDir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            props.store(fos, "Save Data - The Last Roar");
            fos.close();

            System.out.println("[SaveManager] Jogo salvo em: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("[SaveManager] Erro ao salvar: " + e.getMessage());
        }
    }

    public static SaveData carregar(File filesDir, String fileName, int totalMaps) {
        File file = new File(filesDir, fileName);
        if (!file.exists()) return null;

        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(file);
            props.load(fis);
            fis.close();

            SaveData data = new SaveData();
            data.mapa  = Integer.parseInt(props.getProperty(KEY_MAP,    "0"));
            data.posX  = Double.parseDouble(props.getProperty(KEY_POS_X,  "0.0"));
            data.posY  = Double.parseDouble(props.getProperty(KEY_POS_Y,  "0.0"));
            data.vida  = Integer.parseInt(props.getProperty(KEY_LIFE,   "20"));
            data.level = Integer.parseInt(props.getProperty(KEY_LEVEL,  "1"));
            data.ouro  = Integer.parseInt(props.getProperty(KEY_GOLD,   "0"));

            data.inimigosDerrotados = deserializeDefeatedEnemies(
                    props.getProperty(KEY_ENEMIES, ""), totalMaps
            );

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean existe(File filesDir, String fileName) {
        return new File(filesDir, fileName).exists();
    }

    private static String serializeDefeatedEnemies(boolean[][] defeatedEnemies) {
        if (defeatedEnemies == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < defeatedEnemies.length; i++) {
            for (int j = 0; j < defeatedEnemies[i].length; j++) {
                sb.append(defeatedEnemies[i][j]);
                if (j < defeatedEnemies[i].length - 1) sb.append(",");
            }
            if (i < defeatedEnemies.length - 1) sb.append(";");
        }
        return sb.toString();
    }

    private static boolean[][] deserializeDefeatedEnemies(String serialized, int totalMaps) {
        boolean[][] result = new boolean[totalMaps][10];
        if (serialized == null || serialized.isBlank()) return result;

        String[] mapsData = serialized.split(";");
        int mapsLimit = Math.min(mapsData.length, totalMaps);

        for (int i = 0; i < mapsLimit; i++) {
            String[] enemies = mapsData[i].split(",");
            int enemiesLimit = Math.min(enemies.length, result[i].length);
            for (int j = 0; j < enemiesLimit; j++) {
                result[i][j] = Boolean.parseBoolean(enemies[j]);
            }
        }
        return result;
    }
}