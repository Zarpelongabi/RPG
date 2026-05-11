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

    private View layoutShopArea, layoutMerchantDialog;
    private FrameLayout itemDetailOverlay;
    private GridLayout gridShopItems;
    
    private TextView tvDetailName, tvDetailInfo, tvDetailPrice;
    private ImageView ivDetailIcon;
    private Button btnBuyItem;
    private Item selectedItem;

    private Bitmap[] merchantFrames;
    private int playerCoins;
    private Inventory inventory;
    
    private int currentCategoryIndex = 0;
    
    private boolean isTyping = false;
    private final Handler handler = new Handler();
    private Runnable typingRunnable;

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

        ivMerchantSprite = findViewById(R.id.iv_merchant_sprite);
        tvMerchantMessage = findViewById(R.id.tv_merchant_message);
        tvShopCoins = findViewById(R.id.tv_shop_coins_internal);
        gridShopItems = findViewById(R.id.grid_shop_items);
        layoutShopArea = findViewById(R.id.layout_shop_area);
        layoutMerchantDialog = findViewById(R.id.layout_merchant_dialog);

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

            currentCategoryIndex = (currentCategoryIndex - 1 + categories.length) % categories.length;
            atualizarPrateleira();
        });

            currentCategoryIndex = (currentCategoryIndex + 1) % categories.length;
            atualizarPrateleira();
        });

        findViewById(R.id.btn_close_merchant).setOnClickListener(v -> despedida());
        findViewById(R.id.btn_close_detail).setOnClickListener(v -> itemDetailOverlay.setVisibility(View.GONE));
        
        layoutMerchantDialog.setOnClickListener(v -> avancarDialogo());

        avancarDialogo();
    }

    private void avancarDialogo() {

        currentDialogueStep++;
        switch (currentDialogueStep) {
            case 1:
                mostrarFrame(0); 
                break;
            case 2:
                break;
            case 3:
                layoutMerchantDialog.setVisibility(View.GONE);
                mostrarLojaComAnimacao();
                atualizarPrateleira();
                break;
        }
    }

    private void despedida() {
        layoutShopArea.setVisibility(View.GONE);
        layoutMerchantDialog.setVisibility(View.VISIBLE);
        mostrarFrame(3);
    }

    private void updateShopCoins() {
        if (tvShopCoins != null) tvShopCoins.setText(String.valueOf(playerCoins));
    }

    private void carregarSprites() {
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
    }

    private List<Item> getItemsPorCategoria(String categoria) {
        List<Item> list = new ArrayList<>();
        switch (categoria) {
                list.add(new Sword("Adaga", 25, 3, "Comum", 1));
                break;
            case "ARMADURAS":
                break;
                list.add(new Potion("Elixir", 60, 1, 100));
                break;
        }
        return list;
    }

    }

        selectedItem = item;
        tvDetailName.setText(item.getName());
        
        
        itemDetailOverlay.setVisibility(View.VISIBLE);
    }

        if (selectedItem == null) return;
                itemDetailOverlay.setVisibility(View.GONE);
    }

    private void escreverMensagem(String msg, Runnable onComplete) {
        isTyping = true;
        tvMerchantMessage.setText("");
        if (typingRunnable != null) handler.removeCallbacks(typingRunnable);
        typingRunnable = new Runnable() {
            int i = 0;
            @Override
            public void run() {
                    tvMerchantMessage.append(String.valueOf(msg.charAt(i++)));
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
        setResult(RESULT_OK, result);
        finish();
    }
}
