package com.example.rpg_definitivo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BattleActivity extends Activity {

    private ImageView ivEnemy, ivPlayer;
    private TextView tvEnemyName, tvPlayerName, tvPlayerLevel, tvMessage, tvPlayerHpValues, tvPlayerXpValues;
    private ProgressBar pbEnemyHp, pbPlayerHp, pbPlayerXp;
    private Button btnAttack, btnSkill, btnItem, btnRun;

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
        tvPlayerName = findViewById(R.id.tv_player_name);
        tvPlayerLevel = findViewById(R.id.tv_player_level);
        tvPlayerHpValues = findViewById(R.id.tv_player_hp_values);
        tvPlayerXpValues = findViewById(R.id.tv_player_xp_values);
        tvMessage = findViewById(R.id.tv_battle_message);
        pbEnemyHp = findViewById(R.id.pb_enemy_hp);
        
        // As ProgressBars agora funcionam como Máscaras de Escurecimento
        pbPlayerHp = findViewById(R.id.pb_player_hp);
        pbPlayerXp = findViewById(R.id.pb_player_xp);
        
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
        btnItem = findViewById(R.id.btn_item);
        btnRun = findViewById(R.id.btn_run);

        // Configuração inicial do Inimigo
        int enemyResId = getIntent().getIntExtra("enemy_res", R.drawable.sprite_batalhagoblin);
        enemySheet = BitmapFactory.decodeResource(getResources(), enemyResId);
        setEnemyAnimationFrame(0);

        String enemyName = getIntent().getStringExtra("enemy_name");
        if (enemyName != null) {
            tvEnemyName.setText(enemyName.toUpperCase());
            if (enemyName.contains("Boss")) {
                enemyMaxHp = 80; enemyDamage = 15; enemyXpReward = 50;
            } else if (enemyName.contains("Exp")) {
                enemyMaxHp = 30; enemyDamage = 8; enemyXpReward = 15;
            } else {
                enemyMaxHp = 20; enemyDamage = 5; enemyXpReward = 5;
            }
        }

        // Setup inicial
        enemyHp = enemyMaxHp;
        pbEnemyHp.setMax(enemyMaxHp);
        pbEnemyHp.setProgress(enemyHp);

        tvPlayerLevel.setText("" + playerLevel);
        tvPlayerHpValues.setText(playerHp + "/" + playerMaxHp);
        tvPlayerXpValues.setText(playerXp + " / " + xpToNextLevel);
        
        // No HP "Sombra", progress=100 significa 0 sombra (vida cheia mostra o vermelho original)
        pbPlayerHp.setMax(playerMaxHp);
        pbPlayerHp.setProgress(playerHp);
        
        // No XP "Sombra", progress=0 significa sombra total (barra de XP começa vazia/escura)
        pbPlayerXp.setMax(xpToNextLevel);
        pbPlayerXp.setProgress(playerXp);

        tvMessage.setText("Um " + tvEnemyName.getText() + " selvagem apareceu!");

        btnAttack.setOnClickListener(v -> {
            if (isPlayerTurn) realizarAtaqueJogador();
        });

        btnRun.setOnClickListener(v -> {
            tvMessage.setText("Você fugiu com segurança!");
            v.postDelayed(this::finish, 1000);
        });
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

    private void realizarAtaqueJogador() {
        isPlayerTurn = false;
        // Pulo do Herói
        ivPlayer.animate().translationXBy(80).translationYBy(-40).setDuration(120).withEndAction(() -> {
            ivPlayer.animate().translationXBy(-80).translationYBy(40).setDuration(120).withEndAction(() -> {
                int dano = 6 + (int)(Math.random() * 4);
                enemyHp -= dano;
                pbEnemyHp.setProgress(Math.max(0, enemyHp));
                tvMessage.setText("Você causou " + dano + " de dano!");

                if (enemyHp <= 0) {
                    vitoria();
                } else {
                    btnAttack.postDelayed(this::turnoInimigo, 1000);
                }
            }).start();
        }).start();
    }

    private void vitoria() {
        tvMessage.setText("O " + tvEnemyName.getText() + " foi derrotado!");
        ivEnemy.animate().alpha(0).setDuration(500).start();
        
        btnAttack.postDelayed(() -> {
            tvMessage.setText("Você ganhou " + enemyXpReward + " pontos de XP!");
            
            playerXp += enemyXpReward;
            if (playerXp >= xpToNextLevel) {
                playerXp -= xpToNextLevel;
                playerLevel++;
                xpToNextLevel = (int)(xpToNextLevel * 1.5);
                pbPlayerXp.setMax(xpToNextLevel);
                tvPlayerLevel.setText("" + playerLevel);
                tvMessage.setText("Subiu para o nível " + playerLevel + "!");
                playerHp = playerMaxHp; 
                pbPlayerHp.setProgress(playerHp);
                tvPlayerHpValues.setText(playerHp + "/" + playerMaxHp);
            }
            pbPlayerXp.setProgress(playerXp);
            tvPlayerXpValues.setText(playerXp + " / " + xpToNextLevel);
            
            btnAttack.postDelayed(this::finish, 2000);
        }, 1000);
    }

    private void turnoInimigo() {
        setEnemyAnimationFrame(1);
        ivEnemy.postDelayed(() -> {
            setEnemyAnimationFrame(2);
            ivEnemy.postDelayed(() -> {
                setEnemyAnimationFrame(0);
                int dano = enemyDamage + (int)(Math.random() * 3);
                playerHp -= dano;
                
                // Atualiza o HP. Conforme playerHp diminui, a parte transparente encurta 
                // e a parte preta (Background) cobre a barra vermelha da imagem original.
                pbPlayerHp.setProgress(Math.max(0, playerHp));
                tvPlayerHpValues.setText(Math.max(0, playerHp) + "/" + playerMaxHp);

                tvMessage.setText("O inimigo atacou e causou " + dano + " de dano!");

                if (playerHp <= 0) {
                    tvMessage.setText("Você foi derrotado...");
                    btnAttack.postDelayed(this::finish, 2000);
                } else {
                    isPlayerTurn = true;
                    tvMessage.setText("O que você fará?");
                }
            }, 100);
        }, 100);
    }
}
