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
import android.widget.Toast;

public class NovoJogoActivity extends Activity {

    // =========================================================================
    // FIELDS — Componentes da UI (XML)
    // =========================================================================
    private ImageView playerView;
    private ImageView mapView;
    private FrameLayout mainLayout;

    // Os de andar voltam a ser Button normal:
    private Button btnUp, btnDown, btnLeft, btnRight;

    // Ação e Pause ficam como ImageButton:
    private ImageButton btnAction, btnPause;

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

    // =========================================================================
    // FIELDS — Game Loop (Substituto do AnimationTimer do JavaFX)
    // =========================================================================
    private Handler gameHandler = new Handler();
    private final int FPS = 60;
    private final int FRAME_TIME = 1000 / FPS;

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
        startGameLoop();
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

        // Ação do botão de Pause/Menu
        btnPause.setOnClickListener(v -> togglePause());

        // Ação do botão de Ação (ex: Usar poção, atacar)
        btnAction.setOnClickListener(v -> {
            if (!isPaused) {
                // Exemplo: usePotionOnMap();
                Toast.makeText(this, "AÇÃO!", Toast.LENGTH_SHORT).show();
            }
        });
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
     * Equivalente ao conteúdo do seu playerMovement.start() e enemyAI.start()
     */
    private void atualizarJogo() {
        // Exemplo simples de movimentação do ImageView na tela:
        int velocidade = 5;

        if (keyUp)    playerView.setY(playerView.getY() - velocidade);
        if (keyDown)  playerView.setY(playerView.getY() + velocidade);
        if (keyLeft)  playerView.setX(playerView.getX() - velocidade);
        if (keyRight) playerView.setX(playerView.getX() + velocidade);

        // Aqui também entraria a lógica de:
        // - Atualizar inimigos (enemyManager.update())
        // - Checar colisões (checkNpcCollision())
        // - Trocar animação do sprite do jogador
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
            // TODO: Mostrar tela de Pause (ex: abrir um Dialog ou tornar um layout visível)
            Toast.makeText(this, "Jogo Pausado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Jogo Retomado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Para o loop quando a tela for fechada para não vazar memória
        gameHandler.removeCallbacks(gameRunnable);
    }
}