package com.example.rpg_definitivo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.rpg_definitivo.backend.managers.EnemyManager;

public class NovoJogoActivity extends Activity {

    // =========================================================================
    // FIELDS — Componentes da UI (XML)
    // =========================================================================
    private PlayerView playerView;
    private ImageView ivHpFrame;
    private ImageView mapView;
    private FrameLayout mainLayout;

    // Os de andar voltam a ser Button normal:
    private Button btnUp, btnDown, btnLeft, btnRight;

    // Ação e Pause:
    private View btnAction;
    private View btnPause;
    private View pauseMenuContainer;
    private TextView tvContinuar, tvSalvar, tvSairJogo;
    private TextView tvLevelUpToast;

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

    // Status do Jogador (para persistência)
    private int playerHp = 100;
    private int playerMaxHp = 100;
    private int playerLevel = 1;
    private int playerXp = 0;
    private int playerCoins = 0;

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
    private String currentSlotName = "HERÓI"; // Alterado para padrão retro
    private String tempSaveName = ""; // Para o dialog de save
    private final int MAX_NAME_LENGTH = 10;
    private boolean isUpperCase = true;
    private boolean cursorVisible = true;
    private Handler cursorHandler = new Handler();
    private TextView tvSaveDisplayName;
    private GridLayout gridSaveLetters;

    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            cursorVisible = !cursorVisible;
            updateSaveDisplayName();
            cursorHandler.postDelayed(this, 500);
        }
    };

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

        // 4. Se veio de um novo jogo ou carregar save:
        if (getIntent().hasExtra("player_name")) {
            currentSlotName = getIntent().getStringExtra("player_name");
        }
        
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
            // O nome que aparece no HUD ou diálogos é o do PERSONAGEM (playerName)
            currentSlotName = slot.playerName;
            rotaAtual = slot.rota;
            playerHp = slot.hp;
            playerMaxHp = slot.maxHp;
            playerLevel = slot.level;
            playerXp = slot.xp;
            playerCoins = slot.coins;
            if (slot.defeatedEnemies != null) {
                defeatedEnemies = slot.defeatedEnemies;
            }

            // Usamos post para garantir que a UI já foi desenhada
            playerView.post(() -> {
                playerView.setX(slot.playerX);
                playerView.setY(slot.playerY);
                // Atualizar o HUD visual (pode ser necessário criar um método para isso)
                updateHUD();
            });
        }
    }

    private void updateHUD() {
        if (ivHpFrame == null) return;

        int xpToNext = (int)(20 * Math.pow(1.5, playerLevel - 1));
        Bitmap hudFrame = HudManager.getHudFrame(this, playerHp, playerMaxHp, playerXp, xpToNext);
        
        if (hudFrame != null) {
            ivHpFrame.setImageBitmap(hudFrame);
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
        tvLevelUpToast = findViewById(R.id.tv_level_up_toast);
        ivHpFrame = findViewById(R.id.iv_map_hp_frame);

        // Ação do botão de Pause/Menu
        btnPause.setOnClickListener(v -> togglePause());

        tvContinuar.setOnClickListener(v -> togglePause());

        tvSalvar.setOnClickListener(v -> {
            showSaveDialog();
        });

        tvSairJogo.setOnClickListener(v -> {
            finish(); // Volta para a tela inicial (MainActivity)
        });

        // Inicializa a HUD
        updateHUD();
    }

    private void showSaveDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_game, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        tvSaveDisplayName = dialogView.findViewById(R.id.tv_save_display_name);
        gridSaveLetters = dialogView.findViewById(R.id.grid_save_letters);
        View btnBackspace = dialogView.findViewById(R.id.btn_save_backspace);
        View btnCase = dialogView.findViewById(R.id.btn_save_case);
        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        View btnSave = dialogView.findViewById(R.id.btn_dialog_save);

        // Começamos com um campo limpo para o usuário nomear o arquivo de save
        tempSaveName = "";
        updateSaveDisplayName();
        updateSaveKeyboard((Button) btnCase);
        cursorHandler.post(cursorRunnable);

        btnBackspace.setOnClickListener(v -> {
            animarBotao(v);
            if (tempSaveName.length() > 0) {
                tempSaveName = tempSaveName.substring(0, tempSaveName.length() - 1);
                updateSaveDisplayName();
            }
        });

        ((Button) btnCase).setOnClickListener(v -> {
            animarBotao(v);
            isUpperCase = !isUpperCase;
            updateSaveKeyboard((Button) v);
        });

        btnCancel.setOnClickListener(v -> {
            cursorHandler.removeCallbacks(cursorRunnable);
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            animarBotao(v);
            String saveName = tempSaveName.trim();
            if (saveName.isEmpty()) saveName = "SLOT JOGO"; // Nome padrão se estiver vazio
            
            if (currentSlotId == null) {
                currentSlotId = String.valueOf(System.currentTimeMillis());
            }
            
            // Agora o currentSlotName (que é o nome do personagem) é passado separadamente do nome do Save
            SaveSystem.salvarJogo(this, currentSlotId, saveName, currentSlotName, playerView.getX(), playerView.getY(), rotaAtual, 
                                 playerHp, playerMaxHp, playerLevel, playerXp, playerCoins, defeatedEnemies);
            
            Toast.makeText(this, "Jornada Salva: " + saveName, Toast.LENGTH_SHORT).show();
            cursorHandler.removeCallbacks(cursorRunnable);
            dialog.dismiss();
            togglePause();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();

        // Animação de entrada do dialog content
        dialogView.setAlpha(0f);
        dialogView.setScaleX(0.9f);
        dialogView.setScaleY(0.9f);
        dialogView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
    }

    private void updateSaveKeyboard(Button btnCase) {
        gridSaveLetters.removeAllViews();
        String letters = isUpperCase ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ" : "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allChars = letters + numbers;
        
        if (btnCase != null) {
            btnCase.setText(isUpperCase ? "ABC / abc" : "abc / ABC");
        }

        for (char c : allChars.toCharArray()) {
            String display = String.valueOf(c);
            adicionarBotaoSaveTeclado(display, v -> {
                if (tempSaveName.length() < MAX_NAME_LENGTH) {
                    tempSaveName += display;
                    updateSaveDisplayName();
                }
            });
        }
    }

    private void adicionarBotaoSaveTeclado(String texto, View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(texto);
        btn.setMinWidth(0);
        btn.setMinHeight(0);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        // 12 colunas: botões super compactos para garantir que caibam na tela
        params.width = (int) (24 * getResources().getDisplayMetrics().density);
        params.height = (int) (30 * getResources().getDisplayMetrics().density);
        params.setMargins(1, 1, 1, 1);
        btn.setLayoutParams(params);
        
        btn.setBackgroundResource(R.drawable.box_message);
        btn.setPadding(0, 0, 0, 0); // Zerar padding após o background para ganhar espaço

        btn.setTextColor(android.graphics.Color.WHITE);
        btn.setTextSize(14); 
        btn.setAllCaps(false);
        
        // CORREÇÃO PARA CARACTERES CORTADOS:
        btn.setIncludeFontPadding(false); 
        btn.setGravity(android.view.Gravity.CENTER);
        
        btn.setOnClickListener(v -> {
            animarBotao(v);
            listener.onClick(v);
        });
        gridSaveLetters.addView(btn);
    }

    private void updateSaveDisplayName() {
        if (tvSaveDisplayName == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_NAME_LENGTH; i++) {
            if (i < tempSaveName.length()) {
                sb.append(tempSaveName.charAt(i)).append(" ");
            } else if (i == tempSaveName.length()) {
                sb.append(cursorVisible ? "_ " : "  ");
            } else {
                sb.append("_ ");
            }
        }
        tvSaveDisplayName.setText(sb.toString().trim());
    }

    private void animarBotao(View v) {
        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(50)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(50).start()).start();
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

                // --- EFEITO DE MOVIMENTO NO MAPA (CÂMERA REMOVIDA) ---
                // O mapa agora fica estático conforme solicitado
                mapView.setTranslationX(0);
                mapView.setTranslationY(0);

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

    private int lastEnemyIndex = -1;

    private void iniciarBatalha(int enemyIndex) {
        if (isPaused || isTransitioning) return;
        
        // Pausa o jogo antes de ir para a batalha
        isPaused = true;
        resetMovement();
        lastEnemyIndex = enemyIndex;

        com.example.rpg_definitivo.backend.models.Monsters monstro = enemyManager.getMonstro(enemyIndex);
        
        Intent intent = new Intent(this, BattleActivity.class);
        intent.putExtra("enemy_name", monstro.getName());
        intent.putExtra("enemy_res", monstro.getBattleImageResId());
        intent.putExtra("player_name", currentSlotName);
        
        // Passar status atuais do jogador para a batalha
        intent.putExtra("p_hp", playerHp);
        intent.putExtra("p_max_hp", playerMaxHp);
        intent.putExtra("p_level", playerLevel);
        intent.putExtra("p_xp", playerXp);

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Se a BattleActivity devolver os status atualizados:
            if (data != null) {
                playerHp = data.getIntExtra("p_hp", playerHp);
                playerMaxHp = data.getIntExtra("p_max_hp", playerMaxHp);
                playerLevel = data.getIntExtra("p_level", playerLevel);
                playerXp = data.getIntExtra("p_xp", playerXp);
                // Moedas podem ser adicionadas aqui também se BattleActivity as gerenciar
                updateHUD();
            }

            if (resultCode == RESULT_OK && lastEnemyIndex != -1) {
                // Remove o inimigo apenas em caso de vitória
                enemyManager.removeEnemy(lastEnemyIndex);
                showLevelUpEffect();
            } else {
                // Se fugiu ou perdeu, o inimigo continua no mapa. 
                // Teletransportamos o jogador um pouco para trás para evitar colisão imediata.
                afastarJogador();
                Toast.makeText(this, "A batalha terminou.", Toast.LENGTH_SHORT).show();
            }
            lastEnemyIndex = -1;
            isPaused = false;
        }
    }

    private void showLevelUpEffect() {
        if (tvLevelUpToast == null) return;
        
        tvLevelUpToast.setVisibility(View.VISIBLE);
        tvLevelUpToast.setAlpha(0f);
        tvLevelUpToast.setScaleX(0.5f);
        tvLevelUpToast.setScaleY(0.5f);

        tvLevelUpToast.animate()
                .alpha(1f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> {
                    tvLevelUpToast.animate()
                            .alpha(0f)
                            .setStartDelay(1000)
                            .setDuration(500)
                            .withEndAction(() -> tvLevelUpToast.setVisibility(View.GONE))
                            .start();
                })
                .start();
    }

    private void afastarJogador() {
        if (keyUp) playerView.setY(playerView.getY() + 40);
        else if (keyDown) playerView.setY(playerView.getY() - 40);
        else if (keyLeft) playerView.setX(playerView.getX() + 40);
        else if (keyRight) playerView.setX(playerView.getX() - 40);
        else {
            // Se não estava se movendo (colisão por movimento do inimigo), afasta para baixo por padrão
            playerView.setY(playerView.getY() + 40);
        }
    }

    /**
     * Lógica para "pular" de mapa.
     * Reseta a posição do jogador para baixo e pode trocar a imagem do mapa.
     */
    private void mudarDeMapa() {
        if (isTransitioning) return;
        isTransitioning = true;
        
        final View transition = findViewById(R.id.view_transition);
        if (transition != null) {
            transition.setVisibility(View.VISIBLE);
            transition.animate().alpha(1f).setDuration(400).withEndAction(() -> {
                rotaAtual++;
                int alturaTela = mainLayout.getHeight();
                playerView.setY(alturaTela - playerView.getHeight() - 150);
                
                if (enemyManager != null) {
                    enemyManager.configureForMap(rotaAtual - 1);
                }
                
                Toast.makeText(this, "Rota " + rotaAtual, Toast.LENGTH_SHORT).show();
                
                transition.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    transition.setVisibility(View.GONE);
                    isTransitioning = false;
                }).start();
            }).start();
        } else {
            rotaAtual++;
            isTransitioning = false;
        }
    }

    private void voltarMapa() {
        if (isTransitioning) return;
        if (rotaAtual <= 1) {
            float alturaTela = mainLayout.getHeight();
            playerView.setY(alturaTela - playerView.getHeight() - 20);
            return;
        }

        isTransitioning = true;
        
        final View transition = findViewById(R.id.view_transition);
        if (transition != null) {
            transition.setVisibility(View.VISIBLE);
            transition.animate().alpha(1f).setDuration(400).withEndAction(() -> {
                rotaAtual--;
                playerView.setY(250);
                
                if (enemyManager != null) {
                    enemyManager.configureForMap(rotaAtual - 1);
                }

                Toast.makeText(this, "Rota " + rotaAtual, Toast.LENGTH_SHORT).show();
                
                transition.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                    transition.setVisibility(View.GONE);
                    isTransitioning = false;
                }).start();
            }).start();
        } else {
            rotaAtual--;
            isTransitioning = false;
        }
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