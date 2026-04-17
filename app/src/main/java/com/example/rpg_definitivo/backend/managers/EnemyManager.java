package com.example.rpg_definitivo.backend.managers;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

// Importe seus modelos quando portá-los para o Android
import com.example.rpg_definitivo.backend.models.BossGoblin;
import com.example.rpg_definitivo.backend.models.Goblin;
import com.example.rpg_definitivo.backend.models.BossGoblin;
import com.example.rpg_definitivo.backend.models.GoblinExp;
import com.example.rpg_definitivo.backend.models.Monsters;

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
        addEnemy(new Goblin(), 6, screenW * 0.3, screenH * 0.1, 128, 80);
        addEnemy(new Goblin(), 1, screenW * 0.7, screenH * 0.2, 128, 80);
    }

    private void spawnMap1Enemies() {
        addEnemy(new GoblinExp(), 0, screenW * 0.5, screenH * 0.1, 128, 80);
        addEnemy(new GoblinExp(), 1, screenW * 0.2, screenH * 0.15, 128, 80);
    }

    private void spawnMap2Enemies() {
        addEnemy(new BossGoblin(), 0, screenW * 0.5, screenH * 0.1, 256, 256);
    }

    private void addEnemy(Monsters monster, int uniqueId,
                          double x, double y, int spriteSize, int displaySize) {

        if (defeatedEnemies[currentMapIndex][uniqueId]) {
            return;
        }

        ImageView enemyView = createEnemyImageView(monster, spriteSize, displaySize, x, y);
        if (enemyView == null) return;

        storeEnemyProperties(enemyView, uniqueId, spriteSize, displaySize);

        monsters.add(monster);
        views.add(enemyView);
        gameRoot.addView(enemyView); // Adiciona na tela do celular
    }

    private ImageView createEnemyImageView(Monsters monster, int spriteSize,
                                           int displaySize, double x, double y) {

        ImageView view = new ImageView(context);

        // No Android, em vez de passar um "caminho de String", usamos o ID do R.drawable
        // ATENÇÃO: Aqui você precisará pegar a imagem baseada no seu R.drawable
        // Exemplo fixo provisório (você adaptará para o getIdDaImagem do seu modelo):
        // int imageResId = R.drawable.sprite_goblin;

        // view.setImageResource(imageResId);

        // Configura dimensões e posição
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(displaySize, displaySize);
        view.setLayoutParams(params);
        view.setX((float) x);
        view.setY((float) y);

        return view;
    }

    private void storeEnemyProperties(ImageView view, int uniqueId,
                                      int spriteSize, int displaySize) {
        // ImageView do Android não tem um getProperties() nativo genérico.
        // A melhor forma de atrelar dados é usando setTag()
        EnemyData data = new EnemyData();
        data.mapId = uniqueId;
        data.dirMove = 1.0;
        data.spriteSize = spriteSize;
        data.displaySize = displaySize;

        // Opcional: guardar a Bitmap original aqui para não recarregar toda hora

        view.setTag(data);
    }

    // Classe auxiliar para guardar as propriedades (substitui o view.getProperties() do JavaFX)
    private static class EnemyData {
        int mapId;
        double dirMove;
        int spriteSize;
        int displaySize;
    }

    // =========================================================================
    // FRAME UPDATE — Movimento, Animação e Colisão
    // =========================================================================

    public int update(double playerX, double playerY, int enemyFrame) {
        for (int i = 0; i < views.size(); i++) {
            ImageView view = views.get(i);
            EnemyData data = (EnemyData) view.getTag();

            double dirMove = data.dirMove;
            int spriteSize = data.spriteSize;
            double displaySize = data.displaySize;

            // ── Movimento horizontal ─────────────────────────────────────
            double newX = view.getX() + dirMove;
            view.setX((float) newX);

            // ── Bounce nas bordas da tela ───────────────────────────────
            boolean changedDirection = false;
            if (newX > screenW - displaySize - 50) {
                dirMove = -1.0;
                changedDirection = true;
            } else if (newX < 50) {
                dirMove = 1.0;
                changedDirection = true;
            }
            if (changedDirection) {
                data.dirMove = dirMove;
            }

            // ── Animação: seleciona frame e direção na sprite sheet ───────
            // No Android, para recortar uma Sprite Sheet, nós faríamos isso:
            // int spriteRow = (dirMove > 0) ? 2 : 1;
            // Bitmap original = ... (sua sprite sheet inteira)
            // Bitmap frameRecortado = Bitmap.createBitmap(original, enemyFrame * spriteSize, spriteRow * spriteSize, spriteSize, spriteSize);
            // view.setImageBitmap(frameRecortado);
            // *NOTA: Recortar imagens 60x por segundo pesa no celular. O ideal será pré-recortar depois.

            // ── Verificação de colisão (A MATEMÁTICA CONTINUA IGUAL!) ──
            double dx = (playerX + 32) - (newX + displaySize / 2);
            double dy = (playerY + 32) - (view.getY() + displaySize / 2);
            double distanceSquared = dx * dx + dy * dy;
            double collisionRadiusSquared = (displaySize * 0.4) * (displaySize * 0.4);

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