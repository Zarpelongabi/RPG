package com.example.rpg_definitivo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

/**
 * Gerenciador de HUD de Alta Performance.
 * Utiliza cache de frames para evitar alocação de memória em tempo de execução.
 */
public class HudManager {
    private static final SparseArray<Bitmap[]> frameCache = new SparseArray<>();

    public static Bitmap getHudFrame(Context context, int hp, int maxHp, int xp, int maxXp) {
        float hpPct = (float) hp / maxHp;
        float xpPct = (maxXp > 0) ? (float) xp / maxXp : 0;

        // Seleção de Spritesheet baseada no estado de saúde
        int resId = R.drawable.hud_100hp;
        if (hpPct <= 0) resId = R.drawable.hud_0hp;
        else if (hpPct < 0.20f) resId = R.drawable.hud_25hp;
        else if (hpPct < 0.45f) resId = R.drawable.hud_50hp;
        else if (hpPct < 0.70f) resId = R.drawable.hud_75hp;

        // Seleção de Frame baseado no progresso de XP (0-4)
        int xpFrame = (xpPct < 0.15f) ? 0 : (xpPct < 0.35f) ? 1 : (xpPct < 0.60f) ? 2 : (xpPct < 0.85f) ? 3 : 4;

        return getFromCache(context, resId, xpFrame);
    }

    private static Bitmap getFromCache(Context context, int resId, int index) {
        Bitmap[] frames = frameCache.get(resId);
        if (frames == null) {
            Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), resId);
            if (sheet == null) return null;
            
            frames = new Bitmap[5];
            int w = sheet.getWidth() / 5;
            int h = sheet.getHeight();
            for (int i = 0; i < 5; i++) {
                frames[i] = Bitmap.createBitmap(sheet, i * w, 0, w, h);
            }
            frameCache.put(resId, frames);
            sheet.recycle();
        }
        return frames[Math.min(index, 4)];
    }

    public static void clearCache() {
        for (int i = 0; i < frameCache.size(); i++) {
            Bitmap[] frames = frameCache.valueAt(i);
            for (Bitmap b : frames) if (b != null) b.recycle();
        }
        frameCache.clear();
    }
}
