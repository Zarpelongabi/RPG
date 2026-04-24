package com.example.rpg_definitivo.backend.managers;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

// Importe seus modelos quando portá-los para o Android
import com.example.rpg_definitivo.backend.models.BossGoblin;
import com.example.rpg_definitivo.backend.models.Goblin;
import com.example.rpg_definitivo
        .backend.models.GoblinExp;
import com.example.rpg_definitivo.backend.models.Monsters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public class EnemyManager {

    // =========================================================================
    // FIELDS
    // =========================================================================

    private final Context context;          // Necessário no Android para criar Views
    private final FrameLayout gameRoot;     // O equivalente ao Pane do JavaFX

    private final double screenW;
    private final double screenH;

    private final List<Monsters> monsters = new ArrayList<>();
    private final List<ImageView> views   = new ArrayList<>();

    private final boolean[][] defeatedEnemies;
    private int currentMapIndex = 0;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public EnemyManager(Context context, FrameLayout gameRoot, double screenW, double screenH,
                        boolean[][] defeatedEnemies) {
        this.context = context;
        this.gameRoot = gameRoot;
        this.screenW = screenW;
        this.screenH = screenH;
        this.defeatedEnemies = defeatedEnemies;
    }

    // =========================================================================
    // MAP CONFIGURATION
    // =========================================================================

    public void configureForMap(int mapIndex) {
        this.currentMapIndex = mapIndex;

        // Remove todos os inimigos da tela (do layout)
        for (ImageView view : views) {
            gameRoot.removeView(view);
        }
        monsters.clear();
        views.clear();

        spawnEnemiesForMap(mapIndex);
    }

    private void spawnEnemiesForMap(int mapIndex) {
        switch (mapIndex) {
            case 0:
                spawnMap0Enemies();
                break;
            case 1:
                spawnMap1Enemies();
                break;
            case 2:
                spawnMap2Enemies();
                break;
        }
    }

    private void spawnMap0Enemies() {
        addEnemy(new Goblin(), 0, screenW * 0.5, screenH * 0.3, 128, 80);
    }

    private void spawnMap1Enemies() {
        // Agora apenas 1 inimigo por rota, do tamanho do personagem (80dp)
        addEnemy(new GoblinExp(), 0, screenW * 0.5, screenH * 0.3, 128, 80);
    }

    private void spawnMap2Enemies() {
        // Boss continua maior, ou você pode mudar para 80 se quiser padrão
        addEnemy(new BossGoblin(), 0, screenW * 0.5, screenH * 0.3, 256, 150);
    }

    private void addEnemy(Monsters monster, int uniqueId,
                          double x, double y, int spriteSize, int displaySize) {

        if (defeatedEnemies[currentMapIndex][uniqueId]) {
            return;
        }

        ImageView enemyView = createEnemyImageView(monster, spriteSize, displaySize, x, y);
        if (enemyView == null) return;

        storeEnemyProperties(enemyView, uniqueId, spriteSize, displaySize, monster);

        monsters.add(monster);
        views.add(enemyView);
        gameRoot.addView(enemyView); // Adiciona na tela do celular
    }

    private ImageView createEnemyImageView(Monsters monster, int spriteSize,
                                           int displaySize, double x, double y) {

        ImageView view = new ImageView(context);

        // Configura dimensões e posição
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(displaySize, displaySize);
        view.setLayoutParams(params);
        view.setX((float) x);
        view.setY((float) y);

        return view;
    }

    private void storeEnemyProperties(ImageView view, int uniqueId,
                                      int spriteSize, int displaySize, Monsters monster) {
        EnemyData data = new EnemyData();
        data.mapId = uniqueId;
        data.spriteSize = spriteSize;
        data.displaySize = displaySize;
        
        // Carrega a Spritesheet do monstro
        Bitmap fullSheet = BitmapFactory.decodeResource(context.getResources(), monster.getImageResId());
        data.spriteSheet = fullSheet;

        view.setTag(data);
    }

    // Classe auxiliar para guardar as propriedades
    private static class EnemyData {
        int mapId;
        double dirX = 1.0;
        double dirY = 0.0;
        long lastAiChange = 0;
        int spriteSize;
        int displaySize;
        Bitmap spriteSheet;
    }

    // =========================================================================
    // FRAME UPDATE — Movimento, Animação e Colisão
    // =========================================================================

    public int update(double playerX, double playerY, int enemyFrame) {
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < views.size(); i++) {
            ImageView view = views.get(i);
            EnemyData data = (EnemyData) view.getTag();

            int displaySize = data.displaySize;

            // ── IA: Movimento Aleatório ──────────────────────────────────
            if (System.currentTimeMillis() - data.lastAiChange > 2000) { // Muda a cada 2 segundos
                int action = random.nextInt(5); // 0: Parado, 1: Esquerda, 2: Direita, 3: Cima, 4: Baixo
                switch (action) {
                    case 0: data.dirX = 0; data.dirY = 0; break;
                    case 1: data.dirX = -1.5; data.dirY = 0; break;
                    case 2: data.dirX = 1.5; data.dirY = 0; break;
                    case 3: data.dirX = 0; data.dirY = -1.5; break;
                    case 4: data.dirX = 0; data.dirY = 1.5; break;
                }
                data.lastAiChange = System.currentTimeMillis();
            }

            // ── Movimento ────────────────────────────────────────────────
            double newX = view.getX() + data.dirX;
            double newY = view.getY() + data.dirY;

            // ── Colisão com Paredes Invisíveis (Igual ao Jogador - Ajustado para o Caminho) ────────
            float limiteEsquerdo = (float) (screenW * 0.20f);
            float limiteDireito = (float) (screenW * 0.80f - displaySize);
            float limiteSuperior = 0;
            float limiteInferior = (float) (screenH - displaySize - 20);

            if (newX < limiteEsquerdo) { newX = limiteEsquerdo; data.dirX *= -1; }
            if (newX > limiteDireito) { newX = limiteDireito; data.dirX *= -1; }
            if (newY < limiteSuperior) { newY = limiteSuperior; data.dirY *= -1; }
            if (newY > limiteInferior) { newY = limiteInferior; data.dirY *= -1; }

            view.setX((float) newX);
            view.setY((float) newY);

            // ── Animação: Recorte da Spritesheet ───────
            if (data.spriteSheet != null) {
                int directionRow = 0; // Baixo
                if (data.dirY < 0) directionRow = 3;      // Cima
                else if (data.dirX < 0) directionRow = 1; // Esquerda
                else if (data.dirX > 0) directionRow = 2; // Direita
                
                int frameW = data.spriteSheet.getWidth() / 4;
                int frameH = data.spriteSheet.getHeight() / 4;

                int srcX = enemyFrame * frameW;
                int srcY = directionRow * frameH;

                Bitmap frameBitmap = Bitmap.createBitmap(data.spriteSheet, srcX, srcY, frameW, frameH);
                view.setImageBitmap(frameBitmap);
            }

            // ── Verificação de colisão ──
            double playerCenterX = playerX + (displaySize / 2.0);
            double playerCenterY = playerY + (displaySize / 2.0);
            double enemyCenterX = newX + (displaySize / 2.0);
            double enemyCenterY = newY + (displaySize / 2.0);

            double dx = playerCenterX - enemyCenterX;
            double dy = playerCenterY - enemyCenterY;
            double distanceSquared = dx * dx + dy * dy;
            
            // Raio de colisão (ajustado para ser mais sensível quando se "encostam")
            double collisionDistance = displaySize * 0.7; 
            double collisionRadiusSquared = collisionDistance * collisionDistance;

            if (distanceSquared < collisionRadiusSquared) {
                return i;
            }
        }

        return -1;
    }

    // =========================================================================
    // ENEMY REMOVAL E GETTERS
    // =========================================================================

    public void removeEnemy(int index) {
        if (index < 0 || index >= views.size()) return;

        ImageView view = views.get(index);
        EnemyData data = (EnemyData) view.getTag();

        if (data != null) {
            defeatedEnemies[currentMapIndex][data.mapId] = true;
        }

        gameRoot.removeView(view);
        views.remove(index);
        monsters.remove(index);
    }

    public Monsters getMonstro(int index) { return monsters.get(index); }
    public ImageView getView(int index) { return views.get(index); }
    public boolean isEmpty() { return views.isEmpty(); }
}