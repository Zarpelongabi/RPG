package com.example.rpg_definitivo;

import android.graphics.Bitmap;

public class FaceCropper {
    /**
     * Recorta o rosto de uma spritesheet baseado na vida atual.
     * @param spriteSheet A Bitmap da spritesheet.
     * @param hp Vida atual.
     * @param maxHp Vida máxima.
     * @param columns Número de colunas na spritesheet (ex: 4 para o jogador, 3 para o inimigo).
     * @return O frame correspondente ao estado de saúde.
     */
    public static Bitmap getFace(Bitmap spriteSheet, int hp, int maxHp, int columns) {
        if (spriteSheet == null || maxHp <= 0 || columns <= 0) return null;

        int widthFrame = spriteSheet.getWidth() / columns;
        // Heurística para altura: se for 4 colunas, assumimos 4x4 (jogador). Se for 3, assumimos 3x1 (inimigo).
        int heightFrame = (columns == 4) ? spriteSheet.getHeight() / 4 : spriteSheet.getHeight();

        float percent = (float) hp / maxHp;
        int frameIndex;

        // Se a vida estiver cheia (100%), usa sempre o primeiro frame (0)
        if (percent >= 1.0f) {
            frameIndex = 0;
        } else if (columns > 1) {
            // Mapeia o restante dos frames conforme a vida cai
            frameIndex = 1 + (int)((1.0f - percent) * (columns - 1));
            if (frameIndex >= columns) frameIndex = columns - 1;
        } else {
            frameIndex = 0;
        }

        return Bitmap.createBitmap(spriteSheet, frameIndex * widthFrame, 0, widthFrame, heightFrame);
    }

    /**
     * Sobrecarga para manter compatibilidade e detecção automática de colunas.
     */
    public static Bitmap getFace(Bitmap spriteSheet, int hp, int maxHp) {
        // Se a largura for muito maior que a altura, provavelmente é uma sheet horizontal de 3 frames (inimigo)
        int cols = (spriteSheet.getWidth() > spriteSheet.getHeight() * 1.5) ? 3 : 4;
        return getFace(spriteSheet, hp, maxHp, cols);
    }
}
