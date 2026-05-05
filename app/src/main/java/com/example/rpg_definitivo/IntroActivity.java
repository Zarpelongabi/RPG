package com.example.rpg_definitivo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends Activity {

    private TextView tvIntroText;
    private View btnSkip;
    private List<String> phrases = new ArrayList<>();
    private int currentPhraseIndex = 0;
    private Handler handler = new Handler();

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

        // Definindo as frases conforme solicitado
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
        
        // Animação de Fade In
        tvIntroText.animate().alpha(1f).setDuration(1500).withEndAction(() -> {
            // Espera um pouco e faz Fade Out
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
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Como você se chama?");
        
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Digite seu nome...");
        builder.setView(input);

        builder.setPositiveButton("Começar Jornada", (dialog, which) -> {
            String nome = input.getText().toString().trim();
            if (nome.isEmpty()) nome = "Herói";
            
            Intent intent = new Intent(this, NovoJogoActivity.class);
            intent.putExtra("player_name", nome);
            startActivity(intent);
            finish();
        });

        builder.setCancelable(false);
        builder.show();
    }
}
