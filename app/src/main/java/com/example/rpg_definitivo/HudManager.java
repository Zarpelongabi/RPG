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
        float hpPct = (maxHp > 0) ? (float) hp / maxHp : 0;
        float xpPct = (maxXp > 0) ? (float) xp / maxXp : 0;

        // Seleção de Spritesheet baseada no HP (0, 10, 20... 100)
        int hpIndex = Math.round(hpPct * 10) * 10;
        if (hpIndex < 0) hpIndex = 0;
        if (hpIndex > 100) hpIndex = 100;
        
        // Se HP > 0 mas o arredondamento deu 0, forçamos o HUD de 10hp para não parecer morto
        if (hp > 0 && hpIndex == 0) hpIndex = 10;

        String resName = "hud_" + hpIndex + "hp";
        int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        
        // Fallback caso algum recurso não exista
        if (resId == 0) resId = R.drawable.hud_100hp;

        // Seleção de Frame baseado no progresso de XP (0-10)
        int xpFrame = Math.round(xpPct * 10);
        if (xpFrame < 0) xpFrame = 0;
        if (xpFrame > 10) xpFrame = 10;

        return getFromCache(context, resId, xpFrame);
    }

    private static Bitmap getFromCache(Context context, int resId, int index) {
        Bitmap[] frames = frameCache.get(resId);
        if (frames == null) {
            Bitmap sheet = BitmapFactory.decodeResource(context.getResources(), resId);
            if (sheet == null) return null;
            
            // Agora são 11 sprites por folha
            frames = new Bitmap[11];
            int w = sheet.getWidth() / 11;
            int h = sheet.getHeight();
            for (int i = 0; i < 11; i++) {
                frames[i] = Bitmap.createBitmap(sheet, i * w, 0, w, h);
            }
            frameCache.put(resId, frames);
            sheet.recycle();
        }
        return frames[Math.min(index, 10)];
    }

    public static void clearCache() {
        for (int i = 0; i < frameCache.size(); i++) {
            Bitmap[] frames = frameCache.valueAt(i);
            for (Bitmap b : frames) if (b != null) b.recycle();
        }
        frameCache.clear();
    }
}
