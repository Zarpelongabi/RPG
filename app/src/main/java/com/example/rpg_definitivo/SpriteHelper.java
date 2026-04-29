package com.example.rpg_definitivo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SpriteHelper {

    /**
     * Recorta uma parte específica de um sprite sheet.
     * @param context Contexto da aplicação
     * @param resourceId ID do drawable (sprite sheet)
     * @param col Coluna do frame (0 indexado)
     * @param row Linha do frame (0 indexado)
     * @param totalCols Total de colunas no sprite sheet
     * @param totalRows Total de linhas no sprite sheet
     * @param cropFactorY O quanto da altura do frame deve ser pego (0.0 a 1.0). 
     *                    Ex: 0.5 pega apenas a metade superior (rosto).
     * @return Bitmap recortado
     */
    public static Bitmap getFacePortrait(Context context, int resourceId, int totalCols, int totalRows, float cropFactorY) {
        try {
            Bitmap spriteSheet = BitmapFactory.decodeResource(context.getResources(), resourceId);
            if (spriteSheet == null) return null;

            int frameWidth = spriteSheet.getWidth() / totalCols;
            int frameHeight = spriteSheet.getHeight() / totalRows;

            // Recorta o primeiro frame (coluna 0, linha 0) focado na parte superior (rosto)
            int cropHeight = (int) (frameHeight * cropFactorY);
            
            return Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, cropHeight);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
