package com.blome.applist;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.view.WindowInsets;

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

        int buttonId = context.getResources().getIdentifier("btn_exit_app", "id", context.getPackageName());
        Button exitButton = overlayView.findViewById(buttonId);

        if (exitButton != null) {
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startMain);
                }
            });
        } else {
            Log.w("OverlayManager", "Botão 'btn_exit_app' não foi encontrado no layout.");
        }
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final int baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                baseFlags,
                PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { 
            params.setFitInsetsTypes(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else { 
            params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        }

        params.gravity = Gravity.CENTER;

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