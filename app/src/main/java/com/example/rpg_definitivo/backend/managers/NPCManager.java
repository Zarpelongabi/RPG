package com.example.rpg_definitivo.backend.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.example.rpg_definitivo.R;

public class NPCManager {
    private Context context;
    private ViewGroup container;
    private ImageView merchantView;
    private Bitmap[] merchantFrames;
    private boolean isSetup = false;

    public NPCManager(Context context, ViewGroup container) {
        this.context = context;
        this.container = container;
        setupMerchant();
    }

    private void setupMerchant() {
        merchantView = new ImageView(context);
        // Tamanho maior para facilitar visualização e clique
        int size = (int) (180 * context.getResources().getDisplayMetrics().density);
        merchantView.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        merchantView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.npc_vendedor);
        if (sheet != null) {
            merchantFrames = new Bitmap[4];
            int fw = sheet.getWidth() / 4;
            int fh = sheet.getHeight();
            for (int i = 0; i < 4; i++) {
                // Slicing preciso
                merchantFrames[i] = Bitmap.createBitmap(sheet, i * fw, 0, fw, fh);
            }
            merchantView.setImageBitmap(merchantFrames[0]);
        }
        
        merchantView.setVisibility(View.GONE);
        merchantView.setClickable(true);
        merchantView.setFocusable(true);
        
        container.addView(merchantView);
        isSetup = true;
    }

    public void update(int rota, float playerX, float playerY, int screenW, int screenH, boolean lojaAberta) {
        if (rota == 4) {
            if (merchantView.getVisibility() == View.GONE) {
                merchantView.setVisibility(View.VISIBLE);
                if (merchantFrames != null) merchantView.setImageBitmap(merchantFrames[0]);
            }
            
            if (screenW > 0 && screenH > 0) {
                int size = (int) (180 * context.getResources().getDisplayMetrics().density);
                // Centralizado horizontalmente na estrada
                float targetX = (screenW * 0.5f) - (size / 2f);
                // Posicionado na parte superior da estrada, mas alcançável
                float targetY = (screenH * 0.25f); 
                
                if (Math.abs(merchantView.getX() - targetX) > 5) {
                    merchantView.setX(targetX);
                    merchantView.setY(targetY);
                }
            }

            // Se a loja NÃO está aberta, garante que ele está no frame inicial (conversando)
            // Isso evita que ele fique "travado" em outros frames se a loja fechou abruptamente
            if (!lojaAberta && merchantFrames != null) {
                merchantView.setImageBitmap(merchantFrames[0]);
            }
            
            merchantView.bringToFront();
        } else {
            merchantView.setVisibility(View.GONE);
        }
    }

    public Bitmap getFrame(int index) {
        if (merchantFrames != null && index >= 0 && index < merchantFrames.length) {
            return merchantFrames[index];
        }
        return (merchantFrames != null) ? merchantFrames[0] : null;
    }

    public boolean isNearMerchant(float playerX, float playerY, float playerW, float playerH) {
        if (merchantView == null || merchantView.getVisibility() != View.VISIBLE) return false;
        
        int mSize = (int) (180 * context.getResources().getDisplayMetrics().density);
        // Centro do mercador
        float mx = merchantView.getX() + mSize / 2f;
        float my = merchantView.getY() + mSize / 2f;
        
        // Centro do jogador
        float px = playerX + playerW / 2f;
        float py = playerY + playerH / 2f;
        
        double dist = Math.hypot(px - mx, py - my);
        // Raio de interação aumentado para ser mais amigável
        return dist < 300;
    }

    public void setMerchantClickListener(View.OnClickListener listener) {
        merchantView.setOnClickListener(listener);
    }
}
