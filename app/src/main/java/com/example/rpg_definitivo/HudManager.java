package com.example.rpg_definitivo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class HudManager {
    public static final int FRAME_WIDTH = 385;
    public static final int FRAME_HEIGHT = 188;

    /**
     * Retorna o frame da HUD baseado na vida e no XP.
     * Escolhe a imagem correta (100hp, 75hp, etc) e recorta o frame de XP (0%, 25%, 50%, 75%, 100%).
     */
    public static Bitmap getHudFrame(Context context, int hp, int maxHp, int xp, int maxXp) {
        float hpPercent = (float) hp / maxHp;
        float xpPercent = (maxXp > 0) ? (float) xp / maxXp : 0;

        // 1. Escolhe qual recurso de imagem carregar baseado no HP
        int resId;
        if (hpPercent >= 0.875f) resId = R.drawable.hud_100hp;
        else if (hpPercent >= 0.625f) resId = R.drawable.hud_75hp;
        else if (hpPercent >= 0.375f) resId = R.drawable.hud_50hp;
        else if (hpPercent >= 0.125f) resId = R.drawable.hud_25hp;
        else resId = R.drawable.hud_0hp;

        Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), resId);
        if (sheet == null) return null;

        // 2. Escolhe o índice do sprite baseado no XP (0 a 4)
        int xpIndex;
        if (xpPercent < 0.125f) xpIndex = 0;      // 0%
        else if (xpPercent < 0.375f) xpIndex = 1; // 25%
        else if (xpPercent < 0.625f) xpIndex = 2; // 50%
        else if (xpPercent < 0.875f) xpIndex = 3; // 75%
        else xpIndex = 4;                         // 100%

        // 3. Calcula o recorte (cada sheet tem 5 sprites horizontalmente)
        int actualFrameWidth = sheet.getWidth() / 5;
        int actualFrameHeight = sheet.getHeight();
        
        int x = xpIndex * actualFrameWidth;
        
        // Garante que o recorte não saia dos limites
        if (x + actualFrameWidth > sheet.getWidth()) x = sheet.getWidth() - actualFrameWidth;

        return Bitmap.createBitmap(sheet, x, 0, actualFrameWidth, actualFrameHeight);
    }
}
