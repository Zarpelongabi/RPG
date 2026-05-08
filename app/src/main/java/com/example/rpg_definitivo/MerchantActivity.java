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
import com.example.rpg_definitivo.backend.models.Armor;
import com.example.rpg_definitivo.backend.models.Item;
import com.example.rpg_definitivo.backend.models.Potion;
import com.example.rpg_definitivo.backend.models.Sword;

import java.util.ArrayList;
import java.util.List;

public class MerchantActivity extends AppCompatActivity {

    private ImageView ivMerchantSprite, ivShopShelfBg;
    private TextView tvMerchantMessage, tvShopCoins, tvCartTotal;
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
    private final String[] categories = {"ARMAS", "ARMADURAS", "ITENS"};
    private final int[] shopBackgrounds = {R.drawable.loja_armas, R.drawable.loja_armaduras, R.drawable.loja_itens};
    
    private final List<Item> cart = new ArrayList<>();
    private int cartTotal = 0;
    
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
        tvCartTotal = findViewById(R.id.tv_cart_total);
        ivShopShelfBg = findViewById(R.id.iv_shop_shelf_bg);
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

        // Botões Invisíveis (Sobre as setas da imagem)
        findViewById(R.id.btn_prev_category).setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex - 1 + categories.length) % categories.length;
            atualizarPrateleira();
        });

        findViewById(R.id.btn_next_category).setOnClickListener(v -> {
            currentCategoryIndex = (currentCategoryIndex + 1) % categories.length;
            atualizarPrateleira();
        });

        findViewById(R.id.btn_close_merchant).setOnClickListener(v -> despedida());
        findViewById(R.id.btn_close_detail).setOnClickListener(v -> itemDetailOverlay.setVisibility(View.GONE));
        findViewById(R.id.btn_go_to_checkout).setOnClickListener(v -> abrirCheckout());
        
        btnBuyItem.setOnClickListener(v -> adicionarAoCarrinho());
        layoutMerchantDialog.setOnClickListener(v -> avancarDialogo());

        // Inicia com o personagem visível e diálogo
        avancarDialogo();
    }

    private String fullMessage = "";

    private boolean isDespedida = false;

    private void avancarDialogo() {
        if (isTyping) {
            isTyping = false;
            if (typingRunnable != null) handler.removeCallbacks(typingRunnable);
            tvMerchantMessage.setText(fullMessage);
            return;
        }

        if (isDespedida) {
            finalizar();
            return;
        }

        currentDialogueStep++;
        switch (currentDialogueStep) {
            case 1:
                mostrarFrame(0); 
                ivMerchantSprite.setVisibility(View.VISIBLE);
                layoutMerchantDialog.setVisibility(View.VISIBLE);
                escreverMensagem("Saudações! Veja as mercadorias de hoje.");
                break;
            case 2:
                mostrarFrame(2); // Gesto de apontar
                escreverMensagem("Use as setas no balcão para navegar entre as seções.");
                break;
            case 3:
                // MODO LOJA: Personagem e Balão DESAPARECEM
                layoutMerchantDialog.setVisibility(View.GONE);
                ivMerchantSprite.setVisibility(View.GONE); 
                mostrarLojaComAnimacao();
                atualizarPrateleira();
                break;
        }
    }

    private void atualizarPrateleira() {
        if (ivShopShelfBg != null) {
            ivShopShelfBg.setImageResource(shopBackgrounds[currentCategoryIndex]);
            ivShopShelfBg.setAlpha(0.6f);
            ivShopShelfBg.animate().alpha(1.0f).setDuration(400).start();
        }
        
        gridShopItems.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            gridShopItems.removeAllViews();
            List<Item> items = getItemsPorCategoria(categories[currentCategoryIndex]);
            
            // Slots: 0, 2, 4 para a primeira prateleira (ajustado para os 3 nichos visíveis)
            int[] shelfSlots = {0, 2, 4, 10, 12, 14};
            
            for (int i = 0; i < items.size() && i < shelfSlots.length; i++) {
                adicionarItemAoSlot(items.get(i), shelfSlots[i]);
            }
            gridShopItems.animate().alpha(1f).setDuration(250).start();
        }).start();
    }

    private void adicionarItemAoSlot(Item item, int slotIndex) {
        LinearLayout slot = new LinearLayout(this);
        slot.setOrientation(LinearLayout.VERTICAL);
        slot.setGravity(Gravity.CENTER);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(slotIndex % 5, 1f);
        params.rowSpec = GridLayout.spec(slotIndex / 5, 1f);
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.setMargins(5, 5, 5, 5);
        slot.setLayoutParams(params);
        
        ImageView icon = new ImageView(this);
        icon.setImageResource(getIconResourceForItem(item));
        int iconSize = (int) (65 * getResources().getDisplayMetrics().density);
        icon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        TextView name = new TextView(this);
        name.setText(item.getName());
        name.setTextColor(Color.WHITE);
        name.setTextSize(11);
        name.setGravity(Gravity.CENTER);
        name.setShadowLayer(4, 2, 2, Color.BLACK);
        name.setPadding(0, 4, 0, 0);

        TextView price = new TextView(this);
        price.setText(item.getValue() + "G");
        price.setTextColor(Color.parseColor("#FFD700")); // Dourado
        price.setTextSize(12);
        price.setGravity(Gravity.CENTER);
        price.setShadowLayer(3, 1, 1, Color.BLACK);

        slot.addView(icon);
        slot.addView(name);
        slot.addView(price);
        
        slot.setClickable(true);
        slot.setFocusable(true);
        slot.setBackgroundResource(android.R.drawable.list_selector_background);
        
        slot.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> 
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            ).start();
            mostrarDetalhes(item);
        });
        gridShopItems.addView(slot);
    }

    private void abrirCheckout() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "O carrinho está vazio!", Toast.LENGTH_SHORT).show();
            return;
        }
        layoutShopArea.setVisibility(View.GONE);
        ivMerchantSprite.setVisibility(View.VISIBLE); // Volta para confirmar
        layoutMerchantDialog.setVisibility(View.VISIBLE);
        mostrarFrame(1); 
        escreverMensagem("Total: " + cartTotal + " moedas. Confirmar?", () -> {
            layoutMerchantDialog.setOnClickListener(v -> confirmarCompra());
        });
    }

    private void confirmarCompra() {
        if (playerCoins >= cartTotal) {
            int totalRequiredSpace = 0;
            for (Item item : cart) totalRequiredSpace += item.getSize();

            if (inventory.getFreeSpace() >= totalRequiredSpace) {
                for (Item item : cart) inventory.addItem(item);
                playerCoins -= cartTotal;
                updateShopCoins();
                cart.clear();
                cartTotal = 0;
                tvCartTotal.setText("0");
                despedida();
            } else {
                escreverMensagem("Sua mochila está cheia!", () -> {
                    layoutMerchantDialog.setOnClickListener(v -> reabrirLoja());
                });
            }
        } else {
            escreverMensagem("Moedas insuficientes!", () -> {
                layoutMerchantDialog.setOnClickListener(v -> reabrirLoja());
            });
        }
    }

    private void reabrirLoja() {
        layoutMerchantDialog.setVisibility(View.GONE);
        ivMerchantSprite.setVisibility(View.GONE);
        layoutShopArea.setVisibility(View.VISIBLE);
        layoutMerchantDialog.setOnClickListener(v -> avancarDialogo());
    }

    private void despedida() {
        isDespedida = true;
        layoutShopArea.setVisibility(View.GONE);
        ivMerchantSprite.setVisibility(View.VISIBLE);
        layoutMerchantDialog.setVisibility(View.VISIBLE);
        layoutMerchantDialog.setOnClickListener(v -> avancarDialogo());
        mostrarFrame(3);
        escreverMensagem("Volte sempre! Que os deuses o guiem.");
    }

    private void updateShopCoins() {
        if (tvShopCoins != null) tvShopCoins.setText(String.valueOf(playerCoins));
    }

    private void carregarSprites() {
        Bitmap sheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite_comerciante);
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
        layoutShopArea.animate().alpha(1f).setDuration(400).start();
    }

    private List<Item> getItemsPorCategoria(String categoria) {
        List<Item> list = new ArrayList<>();
        switch (categoria) {
            case "ARMAS":
                list.add(new Sword("Adaga", 25, 3, "Comum", 1));
                list.add(new Sword("Katana", 50, 5, "Comum", 2));
                list.add(new Sword("Espada", 80, 7, "Comum", 3));
                break;
            case "ARMADURAS":
                list.add(new Armor("P. Leve", 30, 2, 2));
                list.add(new Armor("P. Médio", 60, 4, 5));
                list.add(new Armor("P. Pesado", 120, 6, 10));
                break;
            case "ITENS":
                list.add(new Potion("P. Pequena", 10, 1, 20));
                list.add(new Potion("P. Média", 25, 1, 50));
                list.add(new Potion("Elixir", 60, 1, 100));
                break;
        }
        return list;
    }

    private int getIconResourceForItem(Item item) {
        String name = item.getName();
        if (name.contains("Adaga")) return R.drawable.adaga;
        if (name.contains("Katana")) return R.drawable.katana;
        if (name.contains("Espada")) return R.drawable.adaga;
        if (name.contains("Leve")) return R.drawable.peitoral_leve;
        if (name.contains("Médio")) return R.drawable.peitoral_medio;
        if (name.contains("Pesado")) return R.drawable.peitoral_pesado;
        return R.drawable.moedas;
    }

    private void mostrarDetalhes(Item item) {
        selectedItem = item;
        tvDetailName.setText(item.getName());
        tvDetailPrice.setText("Preço: " + item.getValue());
        ivDetailIcon.setImageResource(getIconResourceForItem(item));
        
        StringBuilder info = new StringBuilder();
        if (item instanceof Sword) info.append("Dano: ").append(((Sword) item).getDamage());
        else if (item instanceof Potion) info.append("Cura: ").append(((Potion) item).getHealedLife());
        else if (item instanceof Armor) info.append("Defesa: ").append(((Armor) item).getResistance());
        
        tvDetailInfo.setText(info.toString());
        itemDetailOverlay.setVisibility(View.VISIBLE);
    }

    private void adicionarAoCarrinho() {
        if (selectedItem == null) return;
        cart.add(selectedItem);
        cartTotal += selectedItem.getValue();
        tvCartTotal.setText(String.valueOf(cartTotal));
        
        // Feedback visual no carrinho
        View cartBtn = findViewById(R.id.btn_go_to_checkout);
        if (cartBtn != null) {
            cartBtn.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).withEndAction(() -> 
                cartBtn.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
            ).start();
        }

        itemDetailOverlay.setVisibility(View.GONE);
    }

    private void escreverMensagem(String msg) { escreverMensagem(msg, null); }
    private void escreverMensagem(String msg, Runnable onComplete) {
        isTyping = true;
        fullMessage = msg;
        tvMerchantMessage.setText("");
        if (typingRunnable != null) handler.removeCallbacks(typingRunnable);
        typingRunnable = new Runnable() {
            int i = 0;
            @Override
            public void run() {
                if (i < msg.length() && isTyping) {
                    tvMerchantMessage.append(String.valueOf(msg.charAt(i++)));
                    handler.postDelayed(this, 30);
                } else {
                    isTyping = false;
                    tvMerchantMessage.setText(msg);
                    if (onComplete != null) onComplete.run();
                }
            }
        };
        handler.post(typingRunnable);
    }

    private void finalizar() {
        Intent result = new Intent();
        result.putExtra("p_coins", playerCoins);
        try { result.putExtra("inventory_json", inventory.toJSON().toString()); } catch (Exception e) {}
        setResult(RESULT_OK, result);
        finish();
    }
}
