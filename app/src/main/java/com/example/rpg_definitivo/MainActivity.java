package com.example.rpg_definitivo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout; // AQUI FOI CORRIGIDO!
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private FrameLayout rootLayout;
    private LinearLayout menuContainer;
    private TextView tvClickAnywhere;
    private TextView tvNewSave, tvLoadSave, tvExit;

    private boolean isMenuOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TELA CHEIA
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Ligando as variáveis com o XML
        rootLayout = findViewById(R.id.root_layout);
        menuContainer = findViewById(R.id.menu_container);
        tvClickAnywhere = findViewById(R.id.tv_click_anywhere);

        tvNewSave = findViewById(R.id.tv_new_save);
        tvLoadSave = findViewById(R.id.tv_load_save);
        tvExit = findViewById(R.id.tv_exit);

        // Evento: Clicar em qualquer lugar da tela para abrir o menu
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMenuOpen) {
                    abrirMenu();
                }
            }
        });

        // Evento: Clicar em "Novo Save"
        tvNewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen) {
                    Toast.makeText(MainActivity.this, "Iniciando Jornada...", Toast.LENGTH_SHORT).show();
                    Intent aba = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(aba);
                }
            }
        });

        tvLoadSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen) {
                    mostrarSlotsLoad();
                }
            }
        });

        // Evento: Clicar em "Sair"
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpen) {
                    finish(); // Fecha o app
                }
            }
        });
    }

    private void abrirMenu() {
        isMenuOpen = true;
        tvClickAnywhere.setVisibility(View.GONE); // Esconde o texto inicial
        menuContainer.setVisibility(View.VISIBLE); // Mostra o menu

        // Animação de Fade-In para ficar elegante
        menuContainer.setAlpha(0f);
        menuContainer.animate().alpha(1f).setDuration(500).start();
    }

    private void mostrarSlotsLoad() {
        java.util.List<SaveSystem.SaveSlot> slots = SaveSystem.carregarTodosSaves(this);
        if (slots.isEmpty()) {
            Toast.makeText(this, "Nenhum save encontrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Escolha um Save");

        String[] nomes = new String[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            nomes[i] = slots.get(i).name;
        }

        builder.setItems(nomes, (dialog, which) -> {
            SaveSystem.SaveSlot selecionado = slots.get(which);
            carregarPartida(selecionado.id);
        });

        builder.setNeutralButton("Excluir", (dialog, which) -> {
            mostrarDialogExcluir();
        });

        builder.show();
    }

    private void mostrarDialogExcluir() {
        java.util.List<SaveSystem.SaveSlot> slots = SaveSystem.carregarTodosSaves(this);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Excluir Save");

        String[] nomes = new String[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            nomes[i] = slots.get(i).name;
        }

        builder.setItems(nomes, (dialog, which) -> {
            SaveSystem.excluirSave(this, slots.get(which).id);
            Toast.makeText(this, "Save excluído!", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void carregarPartida(String slotId) {
        Toast.makeText(this, "Carregando Jogo...", Toast.LENGTH_SHORT).show();
        Intent aba = new Intent(MainActivity.this, NovoJogoActivity.class);
        aba.putExtra("slot_id", slotId);
        startActivity(aba);
    }
}
