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
        // Tamanho maior para o mercador/balcão no mapa
        int sizeW = (int) (280 * context.getResources().getDisplayMetrics().density);
        int sizeH = (int) (200 * context.getResources().getDisplayMetrics().density);
        merchantView.setLayoutParams(new FrameLayout.LayoutParams(sizeW, sizeH));
        merchantView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), R.drawable.npc_vendedor);
        if (sheet != null) {
            // Para o mapa, usamos a imagem completa do vendedor/balcão 
            // sem fatiar, para evitar que o stand apareça cortado.
            merchantFrames = new Bitmap[1];
            merchantFrames[0] = sheet;
            merchantView.setImageBitmap(sheet);
        }
        
        merchantView.setVisibility(View.GONE);
        merchantView.setClickable(true);
        merchantView.setFocusable(true);
        
        container.addView(merchantView);
        isSetup = true;
    }

    public void update(int rota, float playerX, float playerY, int screenW, int screenH) {
        if (rota == 4) {
            if (merchantView.getVisibility() == View.GONE) {
                merchantView.setVisibility(View.VISIBLE);
                if (merchantFrames != null) merchantView.setImageBitmap(merchantFrames[0]);
            }
            
            if (screenW > 0 && screenH > 0) {
                int sizeW = (int) (280 * context.getResources().getDisplayMetrics().density);
                int sizeH = (int) (200 * context.getResources().getDisplayMetrics().density);
                // Posicionado levemente para a direita na estrada
                float targetX = (screenW * 0.65f) - (sizeW / 2f);
                // Posicionado na parte superior da estrada
                float targetY = (screenH * 0.22f); 
                
                if (Math.abs(merchantView.getX() - targetX) > 5) {
                    merchantView.setX(targetX);
                    merchantView.setY(targetY);
                }
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
        return (merchantFrames != null && merchantFrames.length > 0) ? merchantFrames[0] : null;
    }

    public boolean isNearMerchant(float playerX, float playerY, float playerW, float playerH) {
        if (merchantView == null || merchantView.getVisibility() != View.VISIBLE) return false;
        
        int mSizeW = (int) (280 * context.getResources().getDisplayMetrics().density);
        int mSizeH = (int) (200 * context.getResources().getDisplayMetrics().density);
        // Centro do mercador
        float mx = merchantView.getX() + mSizeW / 2f;
        float my = merchantView.getY() + mSizeH / 2f;
        
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

    public void removeMerchantView() {
        if (container != null && merchantView != null) {
            container.removeView(merchantView);
        }
    }
}
