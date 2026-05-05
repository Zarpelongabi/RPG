package com.example.rpg_definitivo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {

    private ImageView ivEnemy, ivPlayer, ivHpFrame;
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
    private boolean testDanoZero = false;
    private Bitmap enemySheet;
    private Bitmap playerSheet;
    private Bitmap currentHudSheet;
    private int lastHudResId = -1;
    private int lastXpFrame = -1;

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
        ivHpFrame = findViewById(R.id.iv_hp_frame);
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

        // Configuração inicial do Jogador (Spritesheet)
        playerSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite_batalhapersonagem);
        setPlayerAnimationFrame(0);

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
            if (isPlayerTurn) {
                PopupMenu popup = new PopupMenu(this, v);
                popup.getMenu().add("Ataque Normal");
                popup.getMenu().add("Ataque Teste (Dano 0)");
                popup.getMenu().add("Tomar Dano (Teste HUD)");
                
                popup.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if (title.equals("Tomar Dano (Teste HUD)")) {
                        playerHp = Math.max(0, playerHp - 25);
                        shakeView(ivPlayer);
                        atualizarHUD(true);
                        if (playerHp <= 0) gameOver();
                        return true;
                    }
                    testDanoZero = title.contains("Dano 0");
                    realizarAtaqueJogador();
                    return true;
                });
                popup.show();
            }
        });

        // Botões de Skill e Item (Atualmente apenas visuais)
        findViewById(R.id.btn_skill).setOnClickListener(v -> Toast.makeText(this, "Você ainda não possui habilidades!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_item).setOnClickListener(v -> Toast.makeText(this, "Sua bolsa está vazia!", Toast.LENGTH_SHORT).show());

        btnRun.setOnClickListener(v -> {
            tvMessage.setText("Você fugiu com segurança!");
            encerrarBatalha(RESULT_CANCELED);
        });
    }

    private void realizarAtaqueJogador() {
        isPlayerTurn = false;
        setPlayerAnimationFrame(1); // Frame de Preparação/Salto
        
        // Pulo de ataque fluido (Arco parabólico de movimento)
        ivPlayer.animate()
                .translationXBy(100)
                .translationYBy(-50)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    setPlayerAnimationFrame(2); // Frame de Impacto
                    ivPlayer.animate()
                            .translationXBy(-100)
                            .translationYBy(50)
                            .setDuration(150)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .withEndAction(() -> {
                                setPlayerAnimationFrame(0); // Volta ao Idle
                                aplicarDanoAoInimigo();
                            }).start();
                }).start();
    }

    private void aplicarDanoAoInimigo() {
        int dano = testDanoZero ? 0 : 6 + (playerLevel * 2) + (int)(Math.random() * 4);
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
                    // Aguarda a animação da HUD e a leitura da mensagem antes de fechar
                    btnAttack.postDelayed(() -> encerrarBatalha(RESULT_OK), 2000);
                }, 1200);
            } else {
                atualizarHUD(true);
                btnAttack.postDelayed(() -> encerrarBatalha(RESULT_OK), 1000);
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
                setPlayerAnimationFrame(2); // Frame de Dano
                shakeView(ivPlayer);
                atualizarHUD(true);

                tvMessage.setText("O inimigo atacou e causou " + dano + " de dano!");

                if (playerHp <= 0) {
                    gameOver();
                } else {
                    isPlayerTurn = true;
                    btnAttack.postDelayed(() -> {
                        setPlayerAnimationFrame(0); // Volta ao Idle
                        tvMessage.setText("O que você fará?");
                    }, 800);
                }
            }).start();
        }).start();
    }

    /**
     * Sincroniza o Level e as Barras de status (Sombras) com animação suave.
     * Também alterna entre os frames da Spritesheet da HUD conforme o HP.
     */
    private void atualizarHUD(boolean animar) {
        tvPlayerLevel.setText(String.valueOf(playerLevel));
        
        final int targetSombraHp = playerMaxHp - Math.max(0, playerHp);
        final int targetSombraXp = xpToNextLevel - playerXp;

        pbPlayerHp.setMax(playerMaxHp);
        pbPlayerXp.setMax(xpToNextLevel);

        if (animar) {
            // Animação sincronizada para a barra e o sprite da HUD
            ValueAnimator animHp = ValueAnimator.ofInt(pbPlayerHp.getProgress(), targetSombraHp);
            animHp.setDuration(600);
            animHp.setInterpolator(new AccelerateDecelerateInterpolator());
            animHp.addUpdateListener(animation -> {
                pbPlayerHp.setProgress((int) animation.getAnimatedValue());
                updateHudFromBars();
            });
            animHp.start();

            // Animação suave da barra de XP
            ValueAnimator animXp = ValueAnimator.ofInt(pbPlayerXp.getProgress(), targetSombraXp);
            animXp.setDuration(800);
            animXp.setInterpolator(new DecelerateInterpolator());
            animXp.addUpdateListener(animation -> {
                pbPlayerXp.setProgress((int) animation.getAnimatedValue());
                updateHudFromBars();
            });
            animXp.start();
        } else {
            pbPlayerHp.setProgress(targetSombraHp);
            pbPlayerXp.setProgress(targetSombraXp);
            updateHudSprite(playerHp, playerXp);
        }
    }

    /**
     * Lê o progresso atual das barras para atualizar o sprite da HUD de forma sincronizada.
     */
    private void updateHudFromBars() {
        int currentHp = pbPlayerHp.getMax() - pbPlayerHp.getProgress();
        int currentXp = pbPlayerXp.getMax() - pbPlayerXp.getProgress();
        updateHudSprite(currentHp, currentXp);
    }

    /**
     * Atualiza o frame da HUD baseado no HP e XP atuais usando as spritesheets individuais.
     * Cada folha (ex: hud_100hp) contém 5 frames horizontais para 0%, 25%, 50%, 75% e 100% de XP.
     */
    private void updateHudSprite(int currentHp, int currentXp) {
        int resId;
        if (currentHp <= 0) {
            resId = R.drawable.hud_0hp;
        } else {
            float percentHp = (float) currentHp / playerMaxHp;
            if (percentHp > 0.75f) resId = R.drawable.hud_100hp;
            else if (percentHp > 0.50f) resId = R.drawable.hud_75hp;
            else if (percentHp > 0.25f) resId = R.drawable.hud_50hp;
            else resId = R.drawable.hud_25hp;
        }

        int xpFrame = 0;
        if (xpToNextLevel > 0) {
            float percentXp = (float) currentXp / xpToNextLevel;
            // Mapeia 0.0-1.0 para os frames 0, 1, 2, 3, 4 (0%, 25%, 50%, 75%, 100% de XP)
            if (percentXp < 0.20f) xpFrame = 0;
            else if (percentXp < 0.40f) xpFrame = 1;
            else if (percentXp < 0.60f) xpFrame = 2;
            else if (percentXp < 0.80f) xpFrame = 3;
            else xpFrame = 4;
        }

        if (resId != lastHudResId || xpFrame != lastXpFrame) {
            if (resId != lastHudResId) {
                lastHudResId = resId;
                if (currentHudSheet != null) currentHudSheet.recycle();
                currentHudSheet = BitmapFactory.decodeResource(getResources(), resId);
            }
            lastXpFrame = xpFrame;

            if (currentHudSheet != null) {
                try {
                    int sheetWidth = currentHudSheet.getWidth();
                    int sheetHeight = currentHudSheet.getHeight();
                    
                    // Se a imagem for uma spritesheet (5 frames horizontais)
                    if (sheetWidth > sheetHeight * 1.5) {
                        int frameWidth = sheetWidth / 5;
                        int frameHeight = sheetHeight;
                        int xStart = Math.min(xpFrame, 4) * frameWidth;
                        
                        // Garante que o recorte está dentro dos limites da bitmap
                        if (xStart + frameWidth > sheetWidth) {
                            frameWidth = sheetWidth - xStart;
                        }
                        
                        Bitmap frame = Bitmap.createBitmap(currentHudSheet, xStart, 0, frameWidth, frameHeight);
                        ivHpFrame.setImageBitmap(frame);
                    } else {
                        // Caso não seja spritesheet, usa a imagem inteira
                        ivHpFrame.setImageBitmap(currentHudSheet);
                    }
                } catch (Exception e) {
                    ivHpFrame.setImageResource(resId);
                }
            }
        }
    }

    /**
     * Mecânica de Game Over: mostra o HUD de morte e retorna à tela inicial.
     */
    private void gameOver() {
        isPlayerTurn = false;
        playerHp = 0;
        updateHudSprite(0, playerXp);
        
        tvMessage.setText("GAME OVER! Você foi derrotado...");
        
        // Faz o jogador desaparecer lentamente
        ivPlayer.animate().alpha(0).setDuration(1500).start();
        
        btnAttack.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Limpa a pilha de atividades para voltar ao menu principal
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }

    /**
     * Efeito de tremer (vibrar) um elemento na tela e piscar.
     */
    private void shakeView(View view) {
        view.animate()
            .translationXBy(20)
            .setDuration(50)
            .setInterpolator(new CycleInterpolator(3))
            .withEndAction(() -> {
                view.setTranslationX(0);
                view.setAlpha(1.0f);
            })
            .start();
            
        // Flash vermelho ou transparência ao receber dano
        ValueAnimator flash = ValueAnimator.ofFloat(1f, 0.4f, 1f);
        flash.setDuration(150);
        flash.addUpdateListener(animation -> view.setAlpha((float) animation.getAnimatedValue()));
        flash.start();
    }

    private void encerrarBatalha(int resultCode) {
        Intent resultData = new Intent();
        resultData.putExtra("p_hp", playerHp);
        resultData.putExtra("p_max_hp", playerMaxHp);
        resultData.putExtra("p_level", playerLevel);
        resultData.putExtra("p_xp", playerXp);
        setResult(resultCode, resultData);
        finish();
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

    private void setPlayerAnimationFrame(int frameIndex) {
        if (playerSheet == null) return;
        try {
            int frameWidth = playerSheet.getWidth() / 3;
            int frameHeight = playerSheet.getHeight();
            Bitmap frame = Bitmap.createBitmap(playerSheet, Math.max(0, Math.min(frameIndex, 2)) * frameWidth, 0, frameWidth, frameHeight);
            ivPlayer.setImageBitmap(frame);
        } catch (Exception e) {}
    }
}
