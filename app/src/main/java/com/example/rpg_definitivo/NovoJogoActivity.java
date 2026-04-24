package com.example.rpg_definitivo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.example.rpg_definitivo.backend.managers.EnemyManager;

public class NovoJogoActivity extends Activity {

    // =========================================================================
    // FIELDS — Componentes da UI (XML)
    // =========================================================================
    private PlayerView playerView;
    private ImageView mapView;
    private FrameLayout mainLayout;

    // Os de andar voltam a ser Button normal:
    private Button btnUp, btnDown, btnLeft, btnRight;

    // Ação e Pause:
    private View btnAction;
    private View btnPause;
    private View pauseMenuContainer;
    private TextView tvContinuar, tvSalvar, tvSairJogo;

    // =========================================================================
    // FIELDS — Estado das "Teclas" (D-Pad Virtual)
    // =========================================================================
    public static boolean keyUp = false, keyDown = false, keyLeft = false, keyRight = false;

    // =========================================================================
    // FIELDS — Estado do Jogo
    // =========================================================================
    public boolean isPaused = false;
    public boolean lojaAberta = false;
    public boolean inventarioAberto = false;
    public boolean isTransitioning = false;
    private int rotaAtual = 1;
    private EnemyManager enemyManager;
    private int enemyFrame = 0;
    private long lastEnemyFrameTime = 0;
    private boolean[][] defeatedEnemies = new boolean[10][10];

    // =========================================================================
    // FIELDS — Game Loop (Substituto do AnimationTimer do JavaFX)
    // =========================================================================
    private Handler gameHandler = new Handler();
    private final int FPS = 60;
    private final int FRAME_TIME = 1000 / FPS;

    private String currentSlotId = null;
    private String currentSlotName = "Novo Jogo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CÓDIGO MÁGICO DO MODO IMERSIVO (TELA CHEIA)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Esconde a barra inferior
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);    // Esconde a barra superior (Wi-Fi, Bateria)

        // Conecta esta Activity com o XML
        setContentView(R.layout.activity_novojogo);

        // 1. Inicializa os componentes
        linkarInterface();

        // 2. Configura os controles de toque (D-Pad)
        configurarControles();

        // 3. Inicia o Loop do Jogo
        mainLayout.post(() -> {
            enemyManager = new EnemyManager(this, findViewById(R.id.enemy_container), mainLayout.getWidth(), mainLayout.getHeight(), defeatedEnemies);
            enemyManager.configureForMap(rotaAtual - 1);
        });
        
        startGameLoop();

        // 4. Se veio para carregar save:
        if (getIntent().hasExtra("slot_id")) {
            currentSlotId = getIntent().getStringExtra("slot_id");
            aplicarSave(currentSlotId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reativa o modo imersivo ao voltar da batalha
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        
        // Despausa o jogo ao voltar da batalha
        if (isPaused && pauseMenuContainer.getVisibility() != View.VISIBLE) {
            isPaused = false;
        }
    }

    private void aplicarSave(String slotId) {
        SaveSystem.SaveSlot slot = SaveSystem.carregarSlot(this, slotId);
        if (slot != null) {
            currentSlotName = slot.name;
            rotaAtual = slot.rota;
            // Usamos post para garantir que a UI já foi desenhada
            playerView.post(() -> {
                playerView.setX(slot.playerX);
                playerView.setY(slot.playerY);
            });
        }
    }

    private void linkarInterface() {
        playerView = findViewById(R.id.player_view);
        mapView = findViewById(R.id.map_view);
        mainLayout = findViewById(R.id.main_layout);

        btnUp = findViewById(R.id.btn_up);
        btnDown = findViewById(R.id.btn_down);
        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        btnAction = findViewById(R.id.btn_action);
        btnPause = findViewById(R.id.btn_pause);

        pauseMenuContainer = findViewById(R.id.pause_menu_container);
        tvContinuar = findViewById(R.id.tv_continuar);
        tvSalvar = findViewById(R.id.tv_salvar);
        tvSairJogo = findViewById(R.id.tv_sair_jogo);

        // Ação do botão de Pause/Menu
        btnPause.setOnClickListener(v -> togglePause());

        tvContinuar.setOnClickListener(v -> togglePause());

        tvSalvar.setOnClickListener(v -> {
            showSaveDialog();
        });

        tvSairJogo.setOnClickListener(v -> {
            finish(); // Volta para a tela inicial (MainActivity)
        });

        // Ação do botão de Ação (ex: Usar poção, atacar)
        btnAction.setOnClickListener(v -> {
            if (!isPaused) {
                // Exemplo: usePotionOnMap();
                Toast.makeText(this, "AÇÃO!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSaveDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Salvar Jogo");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(currentSlotName);
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String name = input.getText().toString();
            if (name.isEmpty()) name = "Sem Nome";
            
            if (currentSlotId == null) {
                currentSlotId = String.valueOf(System.currentTimeMillis());
            }
            currentSlotName = name;
            
            SaveSystem.salvarJogo(this, currentSlotId, currentSlotName, playerView.getX(), playerView.getY(), rotaAtual);
            Toast.makeText(this, "Jogo Salvo em: " + currentSlotName, Toast.LENGTH_SHORT).show();
            togglePause();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Configura o D-Pad virtual. No celular, precisamos saber quando
     * o dedo TOCA no botão (ACTION_DOWN) e quando SOLTA (ACTION_UP).
     */
    private void configurarControles() {
        btnUp.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) keyUp = true;
            else if (event.getAction() == MotionEvent.ACTION_UP) keyUp = false;
            return true;
        });

        btnDown.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) keyDown = true;
            else if (event.getAction() == MotionEvent.ACTION_UP) keyDown = false;
            return true;
        });

        btnLeft.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) keyLeft = true;
            else if (event.getAction() == MotionEvent.ACTION_UP) keyLeft = false;
            return true;
        });

        btnRight.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) keyRight = true;
            else if (event.getAction() == MotionEvent.ACTION_UP) keyRight = false;
            return true;
        });
    }

    // =========================================================================
    // GAME LOOP
    // =========================================================================

    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused && !inventarioAberto && !lojaAberta && !isTransitioning) {
                atualizarJogo();
            }
            // Agenda a próxima execução para manter os 60 FPS
            gameHandler.postDelayed(this, FRAME_TIME);
        }
    };

    private void startGameLoop() {
        gameHandler.post(gameRunnable);
    }

    /**
     * Atualiza a lógica de movimento, colisões e câmera.
     */
    private void atualizarJogo() {
        int velocidade = 7; // Aumentei um pouco para ficar mais fluido
        boolean movendo = false;

        float novaX = playerView.getX();
        float novaY = playerView.getY();

        if (keyUp) {
            novaY -= velocidade;
            playerView.setDirection(3); // Cima
            movendo = true;
        } else if (keyDown) {
            novaY += velocidade;
            playerView.setDirection(0); // Baixo
            movendo = true;
        } else if (keyLeft) {
            novaX -= velocidade;
            playerView.setDirection(1); // Esquerda
            movendo = true;
        } else if (keyRight) {
            novaX += velocidade;
            playerView.setDirection(2); // Direita
            movendo = true;
        }

        if (movendo) {
            // --- SISTEMA DE COLISÃO (BARREIRAS INVISÍVEIS) ---
            
            int larguraTela = mainLayout.getWidth();
            int alturaTela = mainLayout.getHeight();
            
            if (larguraTela > 0 && alturaTela > 0) {
                // 1. Limites Laterais (Ajustados para 20% e 80% para permitir chegar mais perto das plantas)
                float limiteEsquerdo = larguraTela * 0.20f; 
                float limiteDireito = larguraTela * 0.80f - playerView.getWidth();

                // 2. Limite Superior (SISTEMA DE PRÓXIMO MAPA)
                float limiteSuperior = -playerView.getHeight() / 2f;
                if (novaY < limiteSuperior) {
                    mudarDeMapa();
                    return;
                }

                // 3. Limite Inferior (SISTEMA DE VOLTAR MAPA)
                float limiteInferior = alturaTela - playerView.getHeight() - 10;
                if (novaY > limiteInferior) {
                    voltarMapa();
                    return;
                }

                // --- EFEITO DE MOVIMENTO NO MAPA (CÂMERA) ---
                // O mapa se move levemente na direção oposta ao jogador
                float scrollX = (larguraTela / 2f - novaX) * 0.1f;
                float scrollY = (alturaTela / 2f - novaY) * 0.1f;
                
                mapView.setTranslationX(scrollX);
                mapView.setTranslationY(scrollY);

                // Aplicar as travas laterais (Mato)
                if (novaX < limiteEsquerdo) novaX = limiteEsquerdo;
                if (novaX > limiteDireito) novaX = limiteDireito;

                // Aplica a posição final ao personagem
                playerView.setX(novaX);
                playerView.setY(novaY);
            }

            // Controle de animação do jogador
            if (System.currentTimeMillis() % 150 < 20) {
                playerView.nextFrame();
            }
        } else {
            playerView.resetFrame();
        }

        // --- ATUALIZAR INIMIGOS (Sempre, mesmo se o jogador estiver parado) ---
        if (enemyManager != null) {
            // Atualiza frame da animação do inimigo a cada 150ms
            if (System.currentTimeMillis() - lastEnemyFrameTime > 150) {
                enemyFrame = (enemyFrame + 1) % 4;
                lastEnemyFrameTime = System.currentTimeMillis();
            }

            int collisionIndex = enemyManager.update(playerView.getX(), playerView.getY(), enemyFrame);
            if (collisionIndex != -1) {
                // Colidiu com inimigo! Iniciar Batalha
                iniciarBatalha(collisionIndex);
            }
        }
    }

    private void iniciarBatalha(int enemyIndex) {
        if (isPaused || isTransitioning) return;
        
        // Pausa o jogo antes de ir para a batalha
        isPaused = true;
        resetMovement();

        com.example.rpg_definitivo.backend.models.Monsters monstro = enemyManager.getMonstro(enemyIndex);
        
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("enemy_name", monstro.getName());
        intent.putExtra("enemy_res", monstro.getImageResId());
        
        // Remove o inimigo do mapa (para não colidir de novo ao voltar)
        enemyManager.removeEnemy(enemyIndex);
        
        startActivity(intent);
    }

    /**
     * Lógica para "pular" de mapa.
     * Reseta a posição do jogador para baixo e pode trocar a imagem do mapa.
     */
    private void mudarDeMapa() {
        if (isTransitioning) return;
        isTransitioning = true;
        rotaAtual++;
        
        // Pequeno atraso para dar efeito de transição
        new Handler().postDelayed(() -> {
            int alturaTela = mainLayout.getHeight();
            
            // Coloca o personagem lá embaixo de novo
            playerView.setY(alturaTela - playerView.getHeight() - 150);
            
            if (enemyManager != null) {
                enemyManager.configureForMap(rotaAtual - 1);
            }
            
            isTransitioning = false;
            Toast.makeText(this, "Rota " + rotaAtual, Toast.LENGTH_SHORT).show();
        }, 300);
    }

    private void voltarMapa() {
        if (isTransitioning) return;
        if (rotaAtual <= 1) {
            float alturaTela = mainLayout.getHeight();
            playerView.setY(alturaTela - playerView.getHeight() - 20);
            return;
        }

        isTransitioning = true;
        rotaAtual--;

        new Handler().postDelayed(() -> {
            playerView.setY(250);
            
            if (enemyManager != null) {
                enemyManager.configureForMap(rotaAtual - 1);
            }

            isTransitioning = false;
            Toast.makeText(this, "Rota " + rotaAtual, Toast.LENGTH_SHORT).show();
        }, 300);
    }

    // =========================================================================
    // ESTADOS DO JOGO
    // =========================================================================

    public static void resetMovement() {
        keyUp = false; keyDown = false; keyLeft = false; keyRight = false;
    }

    public void togglePause() {
        if (lojaAberta || inventarioAberto || isTransitioning) return;

        isPaused = !isPaused;

        if (isPaused) {
            resetMovement();
            pauseMenuContainer.setVisibility(View.VISIBLE);
        } else {
            pauseMenuContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para o loop quando a tela for fechada para não vazar memória
        gameHandler.removeCallbacks(gameRunnable);
    }
}