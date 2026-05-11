package com.example.rpg_definitivo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rpg_definitivo.backend.managers.Inventory;
import com.example.rpg_definitivo.backend.models.Item;
import com.example.rpg_definitivo.backend.models.Potion;
import com.example.rpg_definitivo.backend.models.Sword;

import java.util.ArrayList;
import java.util.List;

public class MerchantActivity extends AppCompatActivity {

    private ImageView ivMerchantSprite, ivShopCategoryBg;
    private TextView tvMerchantMessage, tvShopCoins, tvCategoryTitle;
    private View layoutShopArea, layoutMerchantDialog;
    private FrameLayout itemDetailOverlay;
    private GridLayout gridShopItems;
    private ImageButton btnPrev, btnNext;
    
    // Detalhes do item
    private TextView tvDetailName, tvDetailInfo, tvDetailPrice;
    private ImageView ivDetailIcon;
    private Button btnBuyItem;
    private Item selectedItem;

    private Bitmap[] merchantFrames;
    private int playerCoins;
    private Inventory inventory;
    
    private int currentCategoryIndex = 0;
    private final String[] categories = {"ESPADAS", "ARMADURAS", "POÇÕES"};
    
    private boolean isTyping = false;
    private final Handler handler = new Handler();
    private Runnable typingRunnable;

    // Controle de Diálogo
    private int currentDialogueStep = 0;

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

        setContentView(R.layout.activity_merchant);

        // UI principal
        ivMerchantSprite = findViewById(R.id.iv_merchant_sprite);
        tvMerchantMessage = findViewById(R.id.tv_merchant_message);
        tvShopCoins = findViewById(R.id.tv_shop_coins_internal);
        tvCategoryTitle = findViewById(R.id.tv_category_title);
        ivShopCategoryBg = findViewById(R.id.iv_shop_category_bg);
        gridShopItems = findViewById(R.id.grid_shop_items);
        btnPrev = findViewById(R.id.btn_prev_category);
        btnNext = findViewById(R.id.btn_next_category);
        layoutShopArea = findViewById(R.id.layout_shop_area);
        layoutMerchantDialog = findViewById(R.id.layout_merchant_dialog);

        // Overlay de detalhes
        itemDetailOverlay = findViewById(R.id.item_detail_overlay);
        tvDetailName = findViewById(R.id.tv_detail_name);
        tvDetailInfo = findViewById(R.id.tv_detail_info);
        tvDetailPrice = findViewById(R.id.tv_detail_price);
        ivDetailIcon = findViewById(R.id.iv_detail_icon);
        btnBuyItem = findViewById(R.id.btn_buy_item);

        playerCoins = getIntent().getIntExtra("p_coins", 0);
        String invJson = getIntent().getStringExtra("inventory_json");
        inventory = Inventory.fromJSON(invJson);

        carregarSprites();
        updateShopCoins();

        btnPrev.setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex - 1 + categories.length) % categories.length;
            atualizarPrateleira();
        });

        btnNext.setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex + 1) % categories.length;
            atualizarPrateleira();
        });

        findViewById(R.id.btn_close_merchant).setOnClickListener(v -> despedida());
        findViewById(R.id.btn_close_detail).setOnClickListener(v -> itemDetailOverlay.setVisibility(View.GONE));
        
        btnBuyItem.setOnClickListener(v -> comprarItemSelecionado());

        layoutMerchantDialog.setOnClickListener(v -> avancarDialogo());

        // Inicia a cena
        avancarDialogo();
    }

    private void avancarDialogo() {
        if (isTyping) return;

        currentDialogueStep++;
        switch (currentDialogueStep) {
            case 1:
                // Frame 1: Conversa
                mostrarFrame(0); 
                escreverMensagem("Saudações, nobre viajante! Que bom encontrar uma alma viva nestas estradas perigosas.");
                break;
            case 2:
                // Frame 3: Convite (Mostrando a carroça)
                mostrarFrame(2);
                escreverMensagem("Dê uma olhada nas raridades que recuperei de ruínas antigas. Tenho certeza que algo lhe servirá!");
                break;
            case 3:
                // Transição para a Loja (Interface PvZ)
                layoutMerchantDialog.setOnClickListener(null);
                layoutMerchantDialog.setVisibility(View.GONE);
                mostrarLojaComAnimacao();
                atualizarPrateleira();
                break;
        }
    }

    private void despedida() {
        layoutShopArea.setVisibility(View.GONE);
        layoutMerchantDialog.setVisibility(View.VISIBLE);
        
        // Frame 4: Despedida (Acenando)
        mostrarFrame(3);
        
        escreverMensagem("Tenha uma jornada segura, meu amigo! Que o aço da sua espada nunca perca o fio.", () -> {
            layoutMerchantDialog.setOnClickListener(v -> finalizar());
        });
    }

    private void updateShopCoins() {
        if (tvShopCoins != null) tvShopCoins.setText(String.valueOf(playerCoins));
    }

    @SuppressWarnings("DiscouragedApi")
    private void carregarSprites() {
        // Usando getIdentifier para contornar falhas de indexação do R.drawable
        int resId = getResources().getIdentifier("sprite_comerciante", "drawable", getPackageName());
        if (resId == 0) return;

        Bitmap sheet = BitmapFactory.decodeResource(getResources(), resId);
        if (sheet != null) {
            merchantFrames = new Bitmap[4];
            int fw = sheet.getWidth() / 4;
            int fh = sheet.getHeight();
            for (int i = 0; i < 4; i++) {
                merchantFrames[i] = Bitmap.createBitmap(sheet, i * fw, 0, fw, fh);
            }
        }
    }

    private void mostrarFrame(int index) {
        if (merchantFrames != null && index >= 0 && index < merchantFrames.length) {
            ivMerchantSprite.setImageBitmap(merchantFrames[index]);
        }
    }

    private void mostrarLojaComAnimacao() {
        if (layoutShopArea == null) return;
        layoutShopArea.setVisibility(View.VISIBLE);
        layoutShopArea.setAlpha(0f);
        layoutShopArea.setScaleX(0.8f);
        layoutShopArea.setScaleY(0.8f);
        layoutShopArea.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .start();
    }

    @SuppressWarnings("DiscouragedApi")
    private void atualizarPrateleira() {
        tvCategoryTitle.setText(categories[currentCategoryIndex]);
        gridShopItems.removeAllViews();

        // Atualiza fundo temático
        String bgName = "";
        switch (categories[currentCategoryIndex]) {
            case "ESPADAS": bgName = "loja_armas"; break;
            case "ARMADURAS": bgName = "loja_armaduras"; break;
            case "POÇÕES": bgName = "loja_itens"; break;
        }
        int bgResId = getResources().getIdentifier(bgName, "drawable", getPackageName());
        if (bgResId != 0 && ivShopCategoryBg != null) {
            ivShopCategoryBg.setImageResource(bgResId);
        }
        
        List<Item> itemsDaCategoria = getItemsPorCategoria(categories[currentCategoryIndex]);
        for (Item item : itemsDaCategoria) {
            adicionarItemAoGrid(item);
        }
    }

    private List<Item> getItemsPorCategoria(String categoria) {
        List<Item> list = new ArrayList<>();
        switch (categoria) {
            case "ESPADAS":
                list.add(new Sword("Adaga", 25, 3, "Comum", 1));
                list.add(new Sword("Katana", 50, 5, "Comum", 1));
                list.add(new Sword("Espada Longa", 80, 7, "Comum", 1));
                break;
            case "ARMADURAS":
                list.add(new Item("Peitoral Leve", 30, 1));
                list.add(new Item("Peitoral Medio", 60, 2));
                list.add(new Item("Peitoral Pesado", 120, 3));
                break;
            case "POÇÕES":
                list.add(new Potion("Pocao", 10, 1, 20));
                list.add(new Potion("Elixir", 60, 1, 100));
                break;
        }
        return list;
    }

    @SuppressWarnings("DiscouragedApi")
    private void adicionarItemAoGrid(Item item) {
        LinearLayout slot = new LinearLayout(this);
        slot.setOrientation(LinearLayout.VERTICAL);
        slot.setGravity(Gravity.CENTER);
        slot.setPadding(10, 10, 10, 10);
        
        ImageView icon = new ImageView(this);
        // Busca ícone dinamicamente baseado no nome do item (em lowercase)
        String iconName = item.getName().toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (resId == 0) resId = getResources().getIdentifier("moedas", "drawable", getPackageName());
        
        icon.setImageResource(resId);
        int iconSize = (int) (60 * getResources().getDisplayMetrics().density);
        icon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
        
        TextView name = new TextView(this);
        name.setText(item.getName());
        name.setTextColor(Color.WHITE);
        name.setTextSize(12);
        name.setGravity(Gravity.CENTER);

        slot.addView(icon);
        slot.addView(name);
        slot.setOnClickListener(v -> mostrarDetalhes(item, iconName));
        gridShopItems.addView(slot);
    }

    @SuppressWarnings("DiscouragedApi")
    private void mostrarDetalhes(Item item, String iconName) {
        selectedItem = item;
        tvDetailName.setText(item.getName());
        tvDetailPrice.setText(getString(R.string.price_label) + ": " + item.getValue());
        
        int resId = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (resId == 0) resId = getResources().getIdentifier("moedas", "drawable", getPackageName());
        ivDetailIcon.setImageResource(resId);
        
        // Frame 2: Falando preço
        mostrarFrame(1); 
        
        String typeStr = (item instanceof Sword ? "Espada" : (item instanceof Potion ? "Poção" : "Armadura"));
        tvDetailInfo.setText("Tipo: " + typeStr + "\n" + (item.getSize() > 1 ? "Ocupa " + item.getSize() + " slots" : "Item Leve"));
        itemDetailOverlay.setVisibility(View.VISIBLE);
    }

    private void comprarItemSelecionado() {
        if (selectedItem == null) return;
        if (playerCoins >= selectedItem.getValue()) {
            if (inventory.addItem(selectedItem)) {
                playerCoins -= selectedItem.getValue();
                updateShopCoins();
                itemDetailOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "Comprou " + selectedItem.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Mochila Cheia!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sem moedas suficientes!", Toast.LENGTH_SHORT).show();
        }
    }

    private void escreverMensagem(String msg) {
        escreverMensagem(msg, null);
    }

    private void escreverMensagem(String msg, Runnable onComplete) {
        isTyping = true;
        tvMerchantMessage.setText("");
        if (typingRunnable != null) handler.removeCallbacks(typingRunnable);
        
        typingRunnable = new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < msg.length()) {
                    tvMerchantMessage.append(String.valueOf(msg.charAt(i++)));
                    handler.postDelayed(this, 35);
                } else {
                    isTyping = false;
                    if (onComplete != null) onComplete.run();
                }
            }
        };
        handler.post(typingRunnable);
    }

    private void finalizar() {
        Intent result = new Intent();
        result.putExtra("p_coins", playerCoins);
        if (inventory != null) {
            try {
                result.putExtra("inventory_json", inventory.toJSON().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setResult(RESULT_OK, result);
        finish();
    }
}
