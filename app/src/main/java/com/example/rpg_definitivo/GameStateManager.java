package com.example.rpg_definitivo;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
public class GameStateManager {

    private Activity activity;
    private View mainLayout;
    private ImageView mapView;
    private ImageView playerView;

    private boolean isTransitioning = false;
    private int currentMapIndex = 0;
    private final float PLAYER_DISPLAY_SIZE = 150f;
    private int screenW, screenH;

    // Esse construtor recebe as informações da NovoJogoActivity
    public GameStateManager(Activity activity, View mainLayout, ImageView mapView, ImageView playerView) {
        this.activity = activity;
        this.mainLayout = mainLayout;
        this.mapView = mapView;
        this.playerView = playerView;

        this.screenW = activity.getResources().getDisplayMetrics().widthPixels;
        this.screenH = activity.getResources().getDisplayMetrics().heightPixels;
    }

    public void changeMap(final String direction) {
    }
}