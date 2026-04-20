package com.example.rpg_definitivo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PlayerView extends View {
    private Bitmap spriteSheet;
    private int widthFrame, heightFrame;
    private int currentFrame = 0;
    private int direction = 0; // 0: Baixo, 1: Esquerda, 2: Direita, 3: Cima (depende da sua sheet)
    
    private Rect srcRect = new Rect();
    private Rect dstRect = new Rect();

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Carrega a imagem da spritesheet
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sprite_personagem);
        
        // Assume que é 4x4
        widthFrame = spriteSheet.getWidth() / 4;
        heightFrame = spriteSheet.getHeight() / 4;
    }

    public void setDirection(int dir) {
        this.direction = dir;
        invalidate(); // Redesenha
    }

    public void nextFrame() {
        currentFrame = (currentFrame + 1) % 4;
        invalidate();
    }
    
    public void resetFrame() {
        currentFrame = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (spriteSheet == null) return;

        // Define qual pedaço da imagem original vamos pegar (Source)
        int srcX = currentFrame * widthFrame;
        int srcY = direction * heightFrame;
        srcRect.set(srcX, srcY, srcX + widthFrame, srcY + heightFrame);

        // Define onde vamos desenhar na tela (Destination)
        dstRect.set(0, 0, getWidth(), getHeight());

        canvas.drawBitmap(spriteSheet, srcRect, dstRect, null);
    }
}
