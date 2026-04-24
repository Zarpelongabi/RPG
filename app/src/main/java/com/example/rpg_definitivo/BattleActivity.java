
package com.example.rpg_definitivo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {

    private ImageView ivEnemy, ivPlayer;
    private TextView tvEnemyName, tvMessage;
    private Button btnAttack, btnSkill, btnItem, btnRun;

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

        setContentView(R.layout.activity_battle);

        ivEnemy = findViewById(R.id.iv_enemy_battle);
        ivPlayer = findViewById(R.id.iv_player_battle);
        tvEnemyName = findViewById(R.id.tv_enemy_name);
        tvMessage = findViewById(R.id.tv_battle_message);
        
        btnAttack = findViewById(R.id.btn_attack);
        btnSkill = findViewById(R.id.btn_skill);
        btnItem = findViewById(R.id.btn_item);
        btnRun = findViewById(R.id.btn_run);

        // Receber dados do inimigo (opcional por enquanto)
        String enemyName = getIntent().getStringExtra("enemy_name");
        int enemyResId = getIntent().getIntExtra("enemy_res", R.drawable.sprite_goblin);
        
        if (enemyName != null) tvEnemyName.setText(enemyName);
        ivEnemy.setImageResource(enemyResId);

        tvMessage.setText("Um " + tvEnemyName.getText() + " selvagem apareceu!");

        btnAttack.setOnClickListener(v -> {
            tvMessage.setText("Você atacou o " + tvEnemyName.getText() + "!");
            // Lógica de dano aqui...
        });

        btnRun.setOnClickListener(v -> {
            Toast.makeText(this, "Você fugiu da batalha!", Toast.LENGTH_SHORT).show();
            finish(); // Volta para o mapa
        });
        
        btnSkill.setOnClickListener(v -> tvMessage.setText("Você não tem skills ainda!"));
        btnItem.setOnClickListener(v -> tvMessage.setText("Bolsa vazia!"));
    }
}
