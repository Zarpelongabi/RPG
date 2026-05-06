package com.example.rpg_definitivo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

    private boolean isPlayerTurn = true;
    private boolean isTyping = false;
    private String fullMessage = "";
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
        iniciarAnimacoesIdle();
        
        escreverMensagem("Um " + tvEnemyName.getText() + " bloqueia seu caminho!");
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

        enemyHp = enemyMaxHp;
        pbEnemyHp.setMax(enemyMaxHp);
        pbEnemyHp.setProgress(enemyHp);
        atualizarHUD(false);
    }

    private void abrirMenuAtaque(View v) {
        PopupMenu p = new PopupMenu(this, v);
        p.getMenu().add("Golpe Cortante");
        p.getMenu().add("Ataque Furtivo");
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
                escreverMensagem(crit ? "GOLPE CRÍTICO! " + dano + " de dano!" : "Você causou " + dano + " de dano.");
                handler.postDelayed(() -> { if (enemyHp <= 0) vitoria(); else turnoInimigo(); }, 1500);
            }).start();
        }).start();
    }

    private void turnoInimigo() {
        ivEnemy.animate().translationXBy(-100).setDuration(250).withEndAction(() -> {
            int dano = enemyDamage + (int)(Math.random() * 4);
            playerHp = Math.max(0, playerHp - dano);
            
            aplicarEfeitosImpacto(ivPlayer, false);
            shakeView(ivHpFrame, 10);
            exibirDanoFlutuante(dano, ivPlayer, false, true);
            vibrar(60);
            atualizarHUD(true);

            ivEnemy.animate().translationX(0).setDuration(300).withEndAction(() -> {
                escreverMensagem("O inimigo atacou! Você perdeu " + dano + " de vida.");
                if (playerHp <= 0) gameOver();
                else handler.postDelayed(() -> { isPlayerTurn = true; setButtonsEnabled(true); escreverMensagem("O que você fará?"); }, 1200);
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
        int th = playerMaxHp - playerHp; int tx = xpToNextLevel - playerXp;
        pbPlayerHp.setMax(playerMaxHp); pbPlayerXp.setMax(xpToNextLevel);
        if (animar) {
            ValueAnimator ha = ValueAnimator.ofInt(pbPlayerHp.getProgress(), th).setDuration(600);
            ha.addUpdateListener(a -> { pbPlayerHp.setProgress((int)a.getAnimatedValue()); syncHud(); }); ha.start();
            ValueAnimator xa = ValueAnimator.ofInt(pbPlayerXp.getProgress(), tx).setDuration(800);
            xa.addUpdateListener(a -> { pbPlayerXp.setProgress((int)a.getAnimatedValue()); syncHud(); }); xa.start();
        } else { pbPlayerHp.setProgress(th); pbPlayerXp.setProgress(tx); syncHud(); }
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

    private void iniciarAnimacoesIdle() {
        ObjectAnimator playerIdle = ObjectAnimator.ofFloat(ivPlayer, "translationY", 0f, -8f);
        playerIdle.setDuration(1200);
        playerIdle.setRepeatMode(ValueAnimator.REVERSE);
        playerIdle.setRepeatCount(ValueAnimator.INFINITE);
        playerIdle.start();

        ObjectAnimator enemyIdle = ObjectAnimator.ofFloat(ivEnemy, "translationY", 0f, 6f);
        enemyIdle.setDuration(1800);
        enemyIdle.setRepeatMode(ValueAnimator.REVERSE);
        enemyIdle.setRepeatCount(ValueAnimator.INFINITE);
        enemyIdle.start();
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
    private void gameOver() { escreverMensagem("Você tombou..."); ivPlayer.animate().alpha(0).setDuration(2000).start(); handler.postDelayed(() -> { startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)); finish(); }, 3000); }
    private void encerrarBatalha(int r) { HudManager.clearCache(); Intent d = new Intent(); d.putExtra("p_hp", playerHp); d.putExtra("p_max_hp", playerMaxHp); d.putExtra("p_level", playerLevel); d.putExtra("p_xp", playerXp); setResult(r, d); finish(); }
}
