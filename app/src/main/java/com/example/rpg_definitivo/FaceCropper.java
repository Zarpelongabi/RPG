package com.example.rpg_definitivo;

import android.graphics.Bitmap;

public class FaceCropper {
    public static Bitmap getFace(Bitmap spriteSheet) {
        if (spriteSheet == null) return null;
        try {
            // Assume sprite sheet 4x4
            int frameWidth = spriteSheet.getWidth() / 4;
            int frameHeight = spriteSheet.getHeight() / 4;

            // Ajuste fino para o seu sprite:
            // x: pula 22% da largura para centralizar bem
            // y: pula 15% da altura para ignorar o vazio/topo do cabelo
            // width: pega 55% da largura
            // height: pega 45% da altura (pega o rosto completo)
            int startX = (int) (frameWidth * 0.22); 
            int startY = (int) (frameHeight * 0.15); 
            int width = (int) (frameWidth * 0.55);
            int height = (int) (frameHeight * 0.45);

            return Bitmap.createBitmap(spriteSheet, startX, startY, width, height);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
