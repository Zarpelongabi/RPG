package com.example.rpg_definitivo;

import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BattleActivity extends Activity {

    private ImageView ivEnemy, ivPlayer;
    private TextView tvEnemyName, tvPlayerLevel, tvMessage;
    private ProgressBar pbEnemyHp, pbPlayerHp, pbPlayerXp;
    private Button btnAttack, btnRun;

    // Atributos do Jogador
    private int playerMaxHp = 100;
    private int playerHp = 100;
    private int playerLevel = 1;
    private int playerXp = 0;
    private int xpToNextLevel = 20;

    // Atributos do Inimigo
    private int enemyMaxHp = 20;
    private int enemyHp = 20;
    private int enemyDamage = 5;
    private int enemyXpReward = 5;
    
    private boolean isPlayerTurn = true;
    private Bitmap enemySheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Modo Imersivo para esconder barras do sistema
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_battle);

        // Vínculos com o XML
        ivEnemy = findViewById(R.id.iv_enemy_battle);
        ivPlayer = findViewById(R.id.iv_player_battle);
        tvEnemyName = findViewById(R.id.tv_enemy_name);
        tvPlayerLevel = findViewById(R.id.tv_player_level);
        tvMessage = findViewById(R.id.tv_battle_message);
        pbEnemyHp = findViewById(R.id.pb_enemy_hp);
        
        // As ProgressBars agora funcionam como Máscaras de Escurecimento sobre a Sprite HUD
        pbPlayerHp = findViewById(R.id.pb_player_hp);
        pbPlayerXp = findViewById(R.id.pb_player_xp);
        
        btnAttack = findViewById(R.id.btn_attack);
        btnRun = findViewById(R.id.btn_run);

        // Recuperar status vindos do mapa
        playerHp = getIntent().getIntExtra("p_hp", 100);
        playerMaxHp = getIntent().getIntExtra("p_max_hp", 100);
        playerLevel = getIntent().getIntExtra("p_level", 1);
        playerXp = getIntent().getIntExtra("p_xp", 0);
        xpToNextLevel = (int)(20 * Math.pow(1.5, playerLevel - 1));

        // Configuração inicial do Inimigo
        int enemyResId = getIntent().getIntExtra("enemy_res", R.drawable.sprite_batalhagoblin);
        enemySheet = BitmapFactory.decodeResource(getResources(), enemyResId);
        setEnemyAnimationFrame(0);

        String enemyName = getIntent().getStringExtra("enemy_name");
        if (enemyName != null) {
            tvEnemyName.setText(enemyName.toUpperCase());
            if (enemyName.contains("Boss")) {
                enemyMaxHp = 80; enemyDamage = 15; enemyXpReward = 50;
            } else {
                enemyMaxHp = 20; enemyDamage = 5; enemyXpReward = 5;
            }
        }

        enemyHp = enemyMaxHp;
        pbEnemyHp.setMax(enemyMaxHp);
        pbEnemyHp.setProgress(enemyHp);

        tvPlayerLevel.setText(String.valueOf(playerLevel));
        
        // Sincroniza a HUD inicial sem animação
        atualizarHUD(false);

        tvMessage.setText("Um " + tvEnemyName.getText() + " selvagem apareceu!");

        btnAttack.setOnClickListener(v -> {
            if (isPlayerTurn) realizarAtaqueJogador();
        });

        btnRun.setOnClickListener(v -> {
            tvMessage.setText("Você fugiu com segurança!");
            encerrarBatalha(RESULT_CANCELED);
        });
    }

    private void realizarAtaqueJogador() {
        isPlayerTurn = false;
        
        // Pulo de ataque fluido (Arco parabólico de movimento)
        ivPlayer.animate()
                .translationXBy(100)
                .translationYBy(-50)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    ivPlayer.animate()
                            .translationXBy(-100)
                            .translationYBy(50)
                            .setDuration(150)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .withEndAction(this::aplicarDanoAoInimigo).start();
                }).start();
    }

    private void aplicarDanoAoInimigo() {
        int dano = 6 + (playerLevel * 2) + (int)(Math.random() * 4);
        enemyHp -= dano;
        
        // Efeito de tremer o inimigo ao receber impacto
        shakeView(ivEnemy);
        
        // Barra de vida do inimigo desce suavemente
        ObjectAnimator anim = ObjectAnimator.ofInt(pbEnemyHp, "progress", Math.max(0, enemyHp));
        anim.setDuration(400);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
        
        tvMessage.setText("Você causou " + dano + " de dano!");

        if (enemyHp <= 0) {
            btnAttack.postDelayed(this::vitoria, 600);
        } else {
            btnAttack.postDelayed(this::turnoInimigo, 1200);
        }
    }

    private void vitoria() {
        tvMessage.setText("Vitória! O " + tvEnemyName.getText() + " foi derrotado!");
        // Inimigo desaparece com escala e transparência
        ivEnemy.animate().alpha(0).scaleX(0).scaleY(0).setDuration(600).start();
        
        btnAttack.postDelayed(() -> {
            tvMessage.setText("Você ganhou " + enemyXpReward + " pontos de experiência!");
            
            playerXp += enemyXpReward;
            boolean subiuNivel = false;
            
            while (playerXp >= xpToNextLevel) {
                playerXp -= xpToNextLevel;
                playerLevel++;
                playerMaxHp += 20; // Aumento de vida máxima ao subir de nível
                playerHp = playerMaxHp; // Cura total ao subir de nível
                xpToNextLevel = (int)(20 * Math.pow(1.5, playerLevel - 1));
                subiuNivel = true;
            }

            if (subiuNivel) {
                btnAttack.postDelayed(() -> {
                    tvMessage.setText("LEVEL UP! Você subiu para o nível " + playerLevel + "!");
                    atualizarHUD(true);
                    encerrarBatalha(RESULT_OK);
                }, 1200);
            } else {
                atualizarHUD(true);
                encerrarBatalha(RESULT_OK);
            }
        }, 1500);
    }

    private void turnoInimigo() {
        setEnemyAnimationFrame(1);
        // Investida física do inimigo para a esquerda
        ivEnemy.animate().translationXBy(-40).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
            setEnemyAnimationFrame(2);
            ivEnemy.animate().translationXBy(40).setDuration(150).withEndAction(() -> {
                setEnemyAnimationFrame(0);
                
                int dano = enemyDamage + (int)(Math.random() * 3);
                playerHp -= dano;
                
                // Jogador treme ao levar dano
                shakeView(ivPlayer);
                atualizarHUD(true);

                tvMessage.setText("O inimigo atacou e causou " + dano + " de dano!");

                if (playerHp <= 0) {
                    tvMessage.setText("Você foi derrotado...");
                    encerrarBatalha(RESULT_CANCELED);
                } else {
                    isPlayerTurn = true;
                    btnAttack.postDelayed(() -> tvMessage.setText("O que você fará?"), 800);
                }
            }).start();
        }).start();
    }

    /**
     * Sincroniza o Level e as Barras de status (Sombras) com animação suave.
     */
    private void atualizarHUD(boolean animar) {
        tvPlayerLevel.setText(String.valueOf(playerLevel));
        
        int sombraHp = playerMaxHp - Math.max(0, playerHp);
        int sombraXp = xpToNextLevel - playerXp;

        pbPlayerHp.setMax(playerMaxHp);
        pbPlayerXp.setMax(xpToNextLevel);
        
        if (animar) {
            // Animação orgânica da barra de vida
            ObjectAnimator animHp = ObjectAnimator.ofInt(pbPlayerHp, "progress", sombraHp);
            animHp.setDuration(600);
            animHp.setInterpolator(new AccelerateDecelerateInterpolator());
            animHp.start();

            // Animação suave da barra de XP
            ObjectAnimator animXp = ObjectAnimator.ofInt(pbPlayerXp, "progress", sombraXp);
            animXp.setDuration(800);
            animXp.setInterpolator(new DecelerateInterpolator());
            animXp.start();
        } else {
            pbPlayerHp.setProgress(sombraHp);
            pbPlayerXp.setProgress(sombraXp);
        }
    }

    /**
     * Efeito de tremer (vibrar) um elemento na tela.
     */
    private void shakeView(View view) {
        view.animate()
            .translationXBy(20)
            .setDuration(50)
            .setInterpolator(new CycleInterpolator(3))
            .withEndAction(() -> view.setTranslationX(0))
            .start();
    }

    private void encerrarBatalha(int resultCode) {
        Intent resultData = new Intent();
        resultData.putExtra("p_hp", playerHp);
        resultData.putExtra("p_max_hp", playerMaxHp);
        resultData.putExtra("p_level", playerLevel);
        resultData.putExtra("p_xp", playerXp);
        setResult(resultCode, resultData);
        btnAttack.postDelayed(this::finish, 1800);
    }

    private void setEnemyAnimationFrame(int frameIndex) {
        if (enemySheet == null) return;
        try {
            int frameWidth = enemySheet.getWidth() / 3;
            int frameHeight = enemySheet.getHeight();
            Bitmap frame = Bitmap.createBitmap(enemySheet, Math.max(0, Math.min(frameIndex, 2)) * frameWidth, 0, frameWidth, frameHeight);
            ivEnemy.setImageBitmap(frame);
        } catch (Exception e) {}
    }
}
