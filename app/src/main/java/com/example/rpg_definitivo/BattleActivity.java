package com.example.rpg_definitivo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BattleActivity extends Activity {

    private ImageView ivEnemy, ivPlayer, ivHpFrame, ivFlash;
    private TextView tvEnemyName, tvMessage;
    private ProgressBar pbEnemyHp, pbPlayerHp, pbPlayerXp;
    private Button btnAttack, btnRun, btnSkill, btnItem;
    private FrameLayout rootLayout;

    private int playerMaxHp, playerHp, playerLevel, playerXp, xpToNextLevel;
    private int enemyMaxHp = 25, enemyHp = 25, enemyDamage = 6, enemyXpReward = 8;
    private int enemyResId;

    private int playerFrame = 0, enemyFrame = 0;
    private Bitmap playerSheet, enemySheet;
    private boolean isPlayerAnimatingFrames = false;
    private boolean isEnemyAnimatingFrames = false;

    private boolean isPlayerTurn = true;
    private boolean isTyping = false;
    private String fullMessage = "";
    private String playerName = "HERÓI";
    private final Handler handler = new Handler();
    private Runnable typingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarModoImersivo();
        setContentView(R.layout.activity_battle);

        vincularViews();
        configurarBotoes();
        carregarStatus();
        
        playerName = getIntent().getStringExtra("player_name");
        if (playerName == null || playerName.isEmpty()) playerName = "HERÓI";

        // Fade in inicial
        rootLayout.setAlpha(0f);
        rootLayout.animate().alpha(1f).setDuration(1000).start();

        escreverMensagem("Um " + tvEnemyName.getText() + " bloqueia o caminho de " + playerName + "!");
    }

    private void vincularViews() {
        rootLayout = findViewById(android.R.id.content);
        ivEnemy = findViewById(R.id.iv_enemy_battle);
        ivPlayer = findViewById(R.id.iv_player_battle);
        ivHpFrame = findViewById(R.id.iv_hp_frame);
        tvEnemyName = findViewById(R.id.tv_enemy_name);
        tvMessage = findViewById(R.id.tv_battle_message);
        pbEnemyHp = findViewById(R.id.pb_enemy_hp);
        pbPlayerHp = findViewById(R.id.pb_player_hp);
        pbPlayerXp = findViewById(R.id.pb_player_xp);
        btnAttack = findViewById(R.id.btn_attack);
        btnRun = findViewById(R.id.btn_run);
        btnSkill = findViewById(R.id.btn_skill);
        btnItem = findViewById(R.id.btn_item);

        // Flash Layer para impactos visuais
        ivFlash = new ImageView(this);
        ivFlash.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        ivFlash.setBackgroundColor(Color.WHITE);
        ivFlash.setAlpha(0f);
        ((ViewGroup)rootLayout).addView(ivFlash);

        tvMessage.setOnClickListener(v -> pularTexto());
    }

    private void configurarBotoes() {
        btnAttack.setOnClickListener(v -> { animarBotao(v); if (isPlayerTurn && !isTyping) abrirMenuAtaque(v); });
        btnRun.setOnClickListener(v -> { animarBotao(v); fugir(); });
        btnSkill.setOnClickListener(v -> animarBotao(v));
        btnItem.setOnClickListener(v -> animarBotao(v));
    }

    private void carregarStatus() {
        playerHp = getIntent().getIntExtra("p_hp", 100);
        playerMaxHp = getIntent().getIntExtra("p_max_hp", 100);
        playerLevel = getIntent().getIntExtra("p_level", 1);
        playerXp = getIntent().getIntExtra("p_xp", 0);
        xpToNextLevel = (int)(20 * Math.pow(1.5, playerLevel - 1));

        String name = getIntent().getStringExtra("enemy_name");
        if (name != null) tvEnemyName.setText(name.toUpperCase());
        
        enemyResId = getIntent().getIntExtra("enemy_res", 0);
        
        // Carrega as Sheets
        playerSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite_batalhapersonagem);
        
        // Correção do Goblin e outros inimigos
        if (enemyResId != 0) {
            enemySheet = BitmapFactory.decodeResource(getResources(), enemyResId);
        }
        
        // Se ainda for nulo (ou se o ID veio errado), tenta o goblin padrão
        if (enemySheet == null) {
            enemySheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite_batalhagoblin);
        }

        enemyHp = enemyMaxHp;
        pbEnemyHp.setMax(enemyMaxHp);
        pbEnemyHp.setProgress(enemyHp);
        
        // Sincroniza HUD inicial sem animação
        atualizarHUD(false);

        // Aplica escala baseada no tipo de inimigo (Pokémon style)
        // Jogador base é 220dp no XML
        float playerBaseSize = 220;
        String eName = tvEnemyName.getText().toString().toLowerCase();
        
        android.view.ViewGroup.LayoutParams params = ivEnemy.getLayoutParams();
        if (eName.contains("boss")) {
            // Boss 10% maior que o herói
            params.width = (int) (playerBaseSize * 1.1 * getResources().getDisplayMetrics().density);
            params.height = (int) (playerBaseSize * 1.1 * getResources().getDisplayMetrics().density);
        } else if (eName.contains("experiente")) {
            // Experiente 10% menor que o herói
            params.width = (int) (playerBaseSize * 0.9 * getResources().getDisplayMetrics().density);
            params.height = (int) (playerBaseSize * 0.9 * getResources().getDisplayMetrics().density);
        } else {
            // Normal 20% menor que o herói
            params.width = (int) (playerBaseSize * 0.8 * getResources().getDisplayMetrics().density);
            params.height = (int) (playerBaseSize * 0.8 * getResources().getDisplayMetrics().density);
        }
        ivEnemy.setLayoutParams(params);
        
        // Inicia ciclo de animação de frames (recortando a sheet)
        iniciarAnimacaoFrames();
    }

    private void iniciarAnimacaoFrames() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Jogador
                if (playerSheet != null) {
                    ivPlayer.setImageBitmap(getFrame(playerSheet, isPlayerAnimatingFrames ? playerFrame : 0, 3));
                    if (isPlayerAnimatingFrames) {
                        playerFrame = (playerFrame + 1) % 3;
                    } else {
                        playerFrame = 0;
                    }
                }
                // Inimigo
                if (enemySheet != null) {
                    ivEnemy.setImageBitmap(getFrame(enemySheet, isEnemyAnimatingFrames ? enemyFrame : 0, 3));
                    if (isEnemyAnimatingFrames) {
                        enemyFrame = (enemyFrame + 1) % 3;
                    } else {
                        enemyFrame = 0;
                    }
                }
                
                // Velocidade adaptativa: 150ms em combate, mas pode ser menor se precisar
                handler.postDelayed(this, 150);
            }
        });
    }

    private Bitmap getFrame(Bitmap sheet, int frame, int totalFrames) {
        if (sheet == null) return null;
        
        // Garante que o recorte seja baseado na largura REAL da imagem carregada
        // O Android às vezes faz scale automático no BitmapFactory dependendo da densidade
        int sheetWidth = sheet.getWidth();
        int sheetHeight = sheet.getHeight();
        int frameWidth = sheetWidth / totalFrames;
        
        // Segurança para não estourar o limite da imagem
        int startX = Math.min(frame * frameWidth, sheetWidth - frameWidth);
        
        return Bitmap.createBitmap(sheet, startX, 0, frameWidth, sheetHeight);
    }

    private void abrirMenuAtaque(View v) {
        PopupMenu p = new PopupMenu(this, v);
        p.getMenu().add("Golpe Cortante");
        p.getMenu().add("Ataque Furtivo");
        
        // Remove modo teste em produção ou mantém se o usuário quiser
        p.getMenu().add("Modo Teste (0 Dano)");

        p.setOnMenuItemClickListener(item -> {
            executarAtaqueJogador(item.getTitle().toString().contains("0 Dano"));
            return true;
        });
        p.show();
    }

    private void executarAtaqueJogador(boolean isTest) {
        isPlayerTurn = false;
        setButtonsEnabled(false);
        isPlayerAnimatingFrames = true; // Começa a animar o spritesheet

        // Animação Squash & Stretch (Preparação)
        ivPlayer.animate().translationXBy(140).scaleX(1.2f).scaleY(0.8f).setDuration(150)
                .setInterpolator(new AccelerateInterpolator()).withEndAction(() -> {
            
            // Impacto
            boolean crit = Math.random() < 0.15;
            int danoBase = 8 + (playerLevel * 3) + (int)(Math.random() * 5);
            int dano = isTest ? 0 : (crit ? danoBase * 2 : danoBase);
            
            aplicarEfeitosImpacto(ivEnemy, crit);
            exibirDanoFlutuante(dano, ivEnemy, crit, false);
            
            enemyHp = Math.max(0, enemyHp - dano);
            ObjectAnimator.ofInt(pbEnemyHp, "progress", enemyHp).setDuration(400).start();

            // Retorno
            ivPlayer.animate().translationX(0).scaleX(1f).scaleY(1f).setDuration(300)
                    .setInterpolator(new DecelerateInterpolator()).withEndAction(() -> {
                isPlayerAnimatingFrames = false; // Para de animar o spritesheet
                escreverMensagem(crit ? "GOLPE CRÍTICO! " + dano + " de dano!" : "Você causou " + dano + " de dano.");
                handler.postDelayed(() -> { if (enemyHp <= 0) vitoria(); else turnoInimigo(); }, 1500);
            }).start();
        }).start();
    }

    private void turnoInimigo() {
        isEnemyAnimatingFrames = true; // Começa a animar o goblin
        ivEnemy.animate().translationXBy(-100).setDuration(250).withEndAction(() -> {
            // Lógica de Dano solicitada: 10 fixo, Crítico (1/20) = 20
            boolean criticoInimigo = Math.random() < 0.05; // 1/20 = 5%
            int dano = criticoInimigo ? 20 : 10;
            
            playerHp = Math.max(0, playerHp - dano);
            
            aplicarEfeitosImpacto(ivPlayer, criticoInimigo);
            shakeView(ivHpFrame, 10);
            exibirDanoFlutuante(dano, ivPlayer, criticoInimigo, true);
            vibrar(60);
            atualizarHUD(true);

            ivEnemy.animate().translationX(0).setDuration(300).withEndAction(() -> {
                isEnemyAnimatingFrames = false; // Para de animar o goblin
                String msg = criticoInimigo ? "GOLPE LETAL! " + playerName + " perdeu " + dano + " de vida!" : "O inimigo atacou! " + playerName + " perdeu " + dano + " de vida.";
                escreverMensagem(msg);
                if (playerHp <= 0) gameOver();
                else handler.postDelayed(() -> { isPlayerTurn = true; setButtonsEnabled(true); escreverMensagem("O que " + playerName + " fará?"); }, 1200);
            }).start();
        }).start();
    }

    private void aplicarEfeitosImpacto(View alvo, boolean critico) {
        ivFlash.setAlpha(critico ? 0.7f : 0.4f);
        ivFlash.animate().alpha(0f).setDuration(150).start();
        shakeView(alvo, critico ? 30 : 15);
        shakeView(rootLayout, critico ? 15 : 6);
    }

    private void exibirDanoFlutuante(int valor, View alvo, boolean crit, boolean isPlayer) {
        TextView tv = new TextView(this);
        tv.setText(valor == 0 ? "MISS" : (crit ? "CRIT\n" + valor : String.valueOf(valor)));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, crit ? 34 : 26);
        tv.setTextColor(crit ? Color.parseColor("#FFD700") : (isPlayer ? Color.RED : Color.YELLOW));
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setShadowLayer(6, 2, 2, Color.BLACK);
        tv.setGravity(Gravity.CENTER);

        int[] p = new int[2]; alvo.getLocationInWindow(p);
        tv.setX(p[0] + alvo.getWidth()/3f); tv.setY(p[1] + alvo.getHeight()/4f);
        ((ViewGroup)rootLayout).addView(tv);

        tv.animate().translationYBy(-220).translationXBy((float)(Math.random()*120-60))
                .alpha(0).setDuration(1200).withEndAction(() -> ((ViewGroup)rootLayout).removeView(tv)).start();
    }

    private void vitoria() {
        escreverMensagem("O inimigo sucumbiu!");
        ivEnemy.animate().alpha(0).scaleX(0).scaleY(0).setDuration(800).start();
        handler.postDelayed(() -> {
            playerXp += enemyXpReward;
            boolean subiu = false;
            while (playerXp >= xpToNextLevel) {
                playerXp -= xpToNextLevel; playerLevel++; playerMaxHp += 20; playerHp = playerMaxHp;
                xpToNextLevel = (int)(20 * Math.pow(1.5, playerLevel - 1));
                subiu = true;
            }
            atualizarHUD(true);
            if (subiu) {
                escreverMensagem("LEVEL UP! Nível " + playerLevel);
                ivPlayer.animate().scaleX(1.4f).scaleY(1.4f).setDuration(400).withEndAction(() ->
                    ivPlayer.animate().scaleX(1f).scaleY(1f).setDuration(300).start()).start();
                handler.postDelayed(() -> encerrarBatalha(RESULT_OK), 2200);
            } else handler.postDelayed(() -> encerrarBatalha(RESULT_OK), 1800);
        }, 1000);
    }

    private void atualizarHUD(boolean animar) {
        int hpProgress = playerMaxHp - playerHp;
        int xpProgress = xpToNextLevel - playerXp;
        pbPlayerHp.setMax(playerMaxHp);
        pbPlayerXp.setMax(xpToNextLevel);
        
        if (animar) {
            ValueAnimator ha = ValueAnimator.ofInt(pbPlayerHp.getProgress(), hpProgress).setDuration(600);
            ha.addUpdateListener(a -> { pbPlayerHp.setProgress((int)a.getAnimatedValue()); syncHud(); }); 
            ha.start();
            
            ValueAnimator xa = ValueAnimator.ofInt(pbPlayerXp.getProgress(), xpProgress).setDuration(800);
            xa.addUpdateListener(a -> { pbPlayerXp.setProgress((int)a.getAnimatedValue()); syncHud(); }); 
            xa.start();
        } else { 
            pbPlayerHp.setProgress(hpProgress); 
            pbPlayerXp.setProgress(xpProgress); 
            syncHud(); 
        }
    }

    private void syncHud() {
        Bitmap f = HudManager.getHudFrame(this, pbPlayerHp.getMax()-pbPlayerHp.getProgress(), playerMaxHp, pbPlayerXp.getMax()-pbPlayerXp.getProgress(), xpToNextLevel);
        if (f != null) ivHpFrame.setImageBitmap(f);
    }

    private void escreverMensagem(String txt) {
        fullMessage = txt; isTyping = true; tvMessage.setText("");
        if (typingRunnable != null) handler.removeCallbacks(typingRunnable);
        typingRunnable = new Runnable() {
            int i = 0;
            @Override public void run() {
                if (i < txt.length()) { tvMessage.append(String.valueOf(txt.charAt(i++))); handler.postDelayed(this, 25); }
                else isTyping = false;
            }
        };
        handler.post(typingRunnable);
    }

    private void pularTexto() { if (isTyping) { handler.removeCallbacks(typingRunnable); tvMessage.setText(fullMessage); isTyping = false; } }

    private void shakeView(View v, int intensity) {
        v.animate().translationXBy(intensity).setDuration(50).setInterpolator(new CycleInterpolator(3))
                .withEndAction(() -> v.setTranslationX(0)).start();
    }

    private void animarBotao(View v) {
        v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
    }

    private void vibrar(int ms) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) v.vibrate(ms);
    }

    private void configurarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void setButtonsEnabled(boolean e) { btnAttack.setEnabled(e); btnRun.setEnabled(e); btnSkill.setEnabled(e); btnItem.setEnabled(e); }
    private void fugir() { escreverMensagem("Você escapou!"); handler.postDelayed(this::finish, 1000); }
    private void gameOver() {
        escreverMensagem("");
        
        View gameOverContainer = findViewById(R.id.container_game_over);
        if (gameOverContainer == null) return;

        gameOverContainer.setVisibility(View.VISIBLE);
        gameOverContainer.setAlpha(0f);
        
        Button btnRetry = findViewById(R.id.btn_game_over_retry);
        Button btnQuit = findViewById(R.id.btn_game_over_quit);

        btnRetry.setOnClickListener(v -> {
            animarBotao(v);
            // Reinicia a batalha ou volta para o último ponto? 
            // Por enquanto, volta para o menu principal limpo como solicitado anteriormente
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });

        btnQuit.setOnClickListener(v -> {
            animarBotao(v);
            finish();
        });

        gameOverContainer.animate().alpha(1f).setDuration(2000).start();
        ivPlayer.animate().alpha(0).setDuration(2000).start();
    }
    private void encerrarBatalha(int r) { HudManager.clearCache(); Intent d = new Intent(); d.putExtra("p_hp", playerHp); d.putExtra("p_max_hp", playerMaxHp); d.putExtra("p_level", playerLevel); d.putExtra("p_xp", playerXp); setResult(r, d); finish(); }
}
