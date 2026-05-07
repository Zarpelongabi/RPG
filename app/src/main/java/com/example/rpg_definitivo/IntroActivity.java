package com.example.rpg_definitivo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends Activity {

    private TextView tvIntroText;
    private View btnSkip;
    private List<String> phrases = new ArrayList<>();
    private int currentPhraseIndex = 0;
    private Handler handler = new Handler();

    // Custom Name Input Fields
    private View containerNameInput;
    private TextView tvDisplayName;
    private android.widget.ImageView ivNameSprite;
    private String playerName = "";
    private final int MAX_NAME_LENGTH = 10;
    private boolean isUpperCase = true;
    private GridLayout gridLetters;

    private android.graphics.Bitmap[] spriteFrames = new android.graphics.Bitmap[4];
    private int currentSpriteFrame = 0;
    private Handler spriteHandler = new Handler();
    private Runnable spriteRunnable = new Runnable() {
        @Override
        public void run() {
            if (spriteFrames[currentSpriteFrame] != null) {
                ivNameSprite.setImageBitmap(spriteFrames[currentSpriteFrame]);
            }
            // Sequência simples 0,1,2,3 para os 4 primeiros frames
            currentSpriteFrame = (currentSpriteFrame + 1) % 4;
            spriteHandler.postDelayed(this, 200);
        }
    };

    private boolean cursorVisible = true;
    private final Handler cursorHandler = new Handler();
    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            cursorVisible = !cursorVisible;
            updateDisplayName();
            cursorHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modo Imersivo
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_intro);

        tvIntroText = findViewById(R.id.tv_intro_text);
        btnSkip = findViewById(R.id.btn_skip_intro);

        setupNameInput();

        phrases.add("Há muito tempo, as terras de Eldrath viviam em paz...");
        phrases.add("Até que as criaturas das trevas romperam o equilíbrio...");
        phrases.add("Um único guerreiro se levantou para responder ao chamado.");
        phrases.add("Esta é a história do Último Rugido.");

        btnSkip.setOnClickListener(v -> finalizarIntro());

        mostrarProximaFrase();
    }

    private void mostrarProximaFrase() {
        if (currentPhraseIndex >= phrases.size()) {
            finalizarIntro();
            return;
        }

        String frase = phrases.get(currentPhraseIndex);
        tvIntroText.setText(frase);
        
        tvIntroText.animate().alpha(1f).setDuration(1500).withEndAction(() -> {
            handler.postDelayed(() -> {
                tvIntroText.animate().alpha(0f).setDuration(1500).withEndAction(() -> {
                    currentPhraseIndex++;
                    mostrarProximaFrase();
                }).start();
            }, 2000);
        }).start();
    }

    private void finalizarIntro() {
        handler.removeCallbacksAndMessages(null);
        containerNameInput.setVisibility(View.VISIBLE);
        containerNameInput.setAlpha(0f);
        containerNameInput.setScaleX(0.8f);
        containerNameInput.setScaleY(0.8f);
        
        containerNameInput.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
        
        cursorHandler.post(cursorRunnable);
    }

    private void setupNameInput() {
        containerNameInput = findViewById(R.id.container_name_input);
        tvDisplayName = findViewById(R.id.tv_display_name);
        ivNameSprite = findViewById(R.id.iv_name_sprite);
        gridLetters = findViewById(R.id.grid_letters);
        Button btnBackspace = findViewById(R.id.btn_name_backspace);
        Button btnConfirm = findViewById(R.id.btn_name_confirm);
        Button btnCase = findViewById(R.id.btn_name_case);

        carregarSprites();

        updateKeyboard(btnCase);

        btnBackspace.setOnClickListener(v -> {
            animarBotao(v);
            if (playerName.length() > 0) {
                playerName = playerName.substring(0, playerName.length() - 1);
                updateDisplayName();
            }
        });
        
        btnBackspace.setOnLongClickListener(v -> {
            animarBotao(v);
            playerName = "";
            updateDisplayName();
            return true;
        });

        btnCase.setOnClickListener(v -> {
            animarBotao(v);
            isUpperCase = !isUpperCase;
            updateKeyboard(btnCase);
        });

        btnConfirm.setOnClickListener(v -> {
            animarBotao(v);
            String nome = playerName.trim();
            if (nome.isEmpty()) nome = "HERÓI";
            
            final String finalNome = nome;
            
            // Diálogo de confirmação customizado e elegante
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_name, null);
            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            TextView tvMessage = dialogView.findViewById(R.id.tv_confirm_message);
            tvMessage.setText("Então seu nome é " + finalNome + "?");

            Button btnNo = dialogView.findViewById(R.id.btn_confirm_no);
            Button btnYes = dialogView.findViewById(R.id.btn_confirm_yes);

            btnNo.setOnClickListener(v1 -> {
                animarBotao(v1);
                dialog.dismiss();
            });

            btnYes.setOnClickListener(v1 -> {
                animarBotao(v1);
                dialog.dismiss();
                Intent intent = new Intent(this, NovoJogoActivity.class);
                intent.putExtra("player_name", finalNome);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });

            dialog.show();
            
            // Animação de entrada do diálogo
            dialogView.setAlpha(0f);
            dialogView.setScaleX(0.9f);
            dialogView.setScaleY(0.9f);
            dialogView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
        });

        spriteHandler.post(spriteRunnable);
    }

    private void carregarSprites() {
        try {
            android.graphics.Bitmap fullSheet = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.sprite_personagem);
            int frameWidth = fullSheet.getWidth() / 4;
            int frameHeight = fullSheet.getHeight() / 4;
            for (int i = 0; i < 4; i++) {
                spriteFrames[i] = android.graphics.Bitmap.createBitmap(fullSheet, i * frameWidth, 0, frameWidth, frameHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateKeyboard(Button btnCase) {
        gridLetters.removeAllViews();
        String letters = isUpperCase ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ" : "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String allChars = letters + numbers;
        
        if (btnCase != null) {
            btnCase.setText(isUpperCase ? "abc" : "ABC");
            btnCase.setAllCaps(false);
        }

        for (char c : allChars.toCharArray()) {
            String display = String.valueOf(c);
            adicionarBotaoTeclado(display, v -> {
                if (playerName.length() < MAX_NAME_LENGTH) {
                    playerName += display;
                    updateDisplayName();
                }
            });
        }
    }

    private void adicionarBotaoTeclado(String texto, View.OnClickListener listener) {
        Button btn = new Button(this);
        btn.setText(texto);
        btn.setMinWidth(0);
        btn.setPadding(0, 0, 0, 0);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = (int) (48 * getResources().getDisplayMetrics().density);
        params.height = (int) (52 * getResources().getDisplayMetrics().density);
        params.setMargins(4, 4, 4, 4);
        btn.setLayoutParams(params);
        
        btn.setBackgroundResource(R.drawable.box_message);
        btn.setTextColor(android.graphics.Color.WHITE);
        btn.setTextSize(22);
        btn.setAllCaps(false);
        btn.setIncludeFontPadding(false);
        btn.setGravity(android.view.Gravity.CENTER);
        
        btn.setOnClickListener(v -> {
            animarBotao(v);
            listener.onClick(v);
        });
        gridLetters.addView(btn);
    }

    private void updateDisplayName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_NAME_LENGTH; i++) {
            if (i < playerName.length()) {
                sb.append(playerName.charAt(i)).append(" ");
            } else if (i == playerName.length()) {
                // Cursor piscando na posição atual (alterna entre _ e vazio)
                sb.append(cursorVisible ? "_ " : "  ");
            } else {
                // Espaços futuros
                sb.append("_ ");
            }
        }
        tvDisplayName.setText(sb.toString().trim());
    }

    private void animarBotao(View v) {
        v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(60)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(60).start()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursorHandler.removeCallbacks(cursorRunnable);
        spriteHandler.removeCallbacks(spriteRunnable);
    }
}
