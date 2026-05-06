package com.example.rpg_definitivo.backend.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import com.example.rpg_definitivo.backend.models.BossGoblin;
import com.example.rpg_definitivo.backend.models.Goblin;
import com.example.rpg_definitivo.backend.models.GoblinExp;
import com.example.rpg_definitivo.backend.models.Monsters;

public class EnemyManager {
    private final Context context;
    private final FrameLayout gameRoot;
    private final double screenW, screenH;
    private final List<Monsters> monsters = new ArrayList<>();
    private final List<ImageView> views = new ArrayList<>();
    private final boolean[][] defeatedEnemies;
    private int currentMapIndex = 0;

    public EnemyManager(Context context, FrameLayout gameRoot, double screenW, double screenH, boolean[][] defeatedEnemies) {
        this.context = context; this.gameRoot = gameRoot; this.screenW = screenW; this.screenH = screenH; this.defeatedEnemies = defeatedEnemies;
    }

    public void configureForMap(int mapIndex) {
        this.currentMapIndex = mapIndex;
        for (ImageView view : views) gameRoot.removeView(view);
        monsters.clear(); views.clear();
        spawnEnemiesForMap(mapIndex);
    }

    private void spawnEnemiesForMap(int mapIndex) {
        int normalSize = (int) (80 * context.getResources().getDisplayMetrics().density);
        switch (mapIndex) {
            case 0: addEnemy(new Goblin(), 0, screenW * 0.5, screenH * 0.3, normalSize); break;
            case 1: addEnemy(new GoblinExp(), 0, screenW * 0.5, screenH * 0.3, normalSize); break;
            case 2: addEnemy(new BossGoblin(), 0, screenW * 0.5, screenH * 0.3, normalSize); break;
        }
    }

    private void addEnemy(Monsters monster, int uniqueId, double x, double y, int displaySize) {
        if (defeatedEnemies[currentMapIndex][uniqueId]) return;
        ImageView view = new ImageView(context);
        view.setLayoutParams(new FrameLayout.LayoutParams(displaySize, displaySize));
        view.setX((float) x); view.setY((float) y);

        EnemyData data = new EnemyData();
        data.mapId = uniqueId;
        data.displaySize = displaySize;
        
        // Pré-corta a spritesheet para evitar lag no update
        Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), monster.getImageResId());
        if (sheet != null) {
            data.frames = new Bitmap[4][4]; // 4 direções, 4 frames
            int fw = sheet.getWidth() / 4;
            int fh = sheet.getHeight() / 4;
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    data.frames[r][c] = Bitmap.createBitmap(sheet, c * fw, r * fh, fw, fh);
                }
            }
            sheet.recycle();
        }

        view.setTag(data);
        monsters.add(monster);
        views.add(view);
        gameRoot.addView(view);
    }

    private static class EnemyData {
        int mapId, displaySize;
        double dirX = 1.0, dirY = 0.0;
        long lastAiChange = 0;
        Bitmap[][] frames;
    }

    public int update(double playerX, double playerY, int frameIndex) {
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < views.size(); i++) {
            ImageView view = views.get(i);
            EnemyData data = (EnemyData) view.getTag();

            if (System.currentTimeMillis() - data.lastAiChange > 2000) {
                int act = rand.nextInt(5);
                data.dirX = (act == 1) ? -1.5 : (act == 2) ? 1.5 : 0;
                data.dirY = (act == 3) ? -1.5 : (act == 4) ? 1.5 : 0;
                data.lastAiChange = System.currentTimeMillis();
            }

            view.setX((float) (view.getX() + data.dirX));
            view.setY((float) (view.getY() + data.dirY));

            // Animação Ultra-Rápida usando cache
            if (data.frames != null) {
                int row = (data.dirY < 0) ? 3 : (data.dirX < 0) ? 1 : (data.dirX > 0) ? 2 : 0;
                view.setImageBitmap(data.frames[row][frameIndex % 4]);
            }

            // Colisão simplificada e eficiente
            if (Math.hypot(playerX - view.getX(), playerY - view.getY()) < data.displaySize * 0.7) return i;
        }
        return -1;
    }

    public void removeEnemy(int index) {
        if (index < 0 || index >= views.size()) return;
        defeatedEnemies[currentMapIndex][((EnemyData)views.get(index).getTag()).mapId] = true;
        gameRoot.removeView(views.get(index));
        views.remove(index); monsters.remove(index);
    }
    public Monsters getMonstro(int i) { return monsters.get(i); }
}
