package com.blome.applist;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class OverlayManager {

    private Context context;
    private WindowManager windowManager;
    private View overlayView;

    public OverlayManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void showOverlay(String packageName) {
        if (overlayView != null) {
            return;
        }
        Log.d("OverlayManager", "Mostrando overlay para: " + packageName);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layoutId = context.getResources().getIdentifier("overlay_layout", "layout", context.getPackageName());
        if (layoutId == 0) {
            Log.e("OverlayManager", "ERRO: 'overlay_layout.xml' não foi encontrado em res/layout/");
            return;
        }
        overlayView = inflater.inflate(layoutId, null);

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        overlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                return true;
            }
        });

        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            Log.e("OverlayManager", "Erro ao adicionar view de overlay: " + e.getMessage());
        }
    }

    public void hideOverlay() {
        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView);
                overlayView = null;
                Log.d("OverlayManager", "Escondendo overlay");
            } catch (Exception e) {
                Log.e("OverlayManager", "Erro ao remover view de overlay: " + e.getMessage());
            }
        }
    }
}