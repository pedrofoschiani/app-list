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
    private boolean isOverlayVisible = false;

    public OverlayManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void showOverlay() {
        if (isOverlayVisible) {
            return;
        }
        Log.d("OverlayManager", "Mostrando overlay...");
        isOverlayVisible = true;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layoutId = context.getResources().getIdentifier("overlay_layout", "layout", context.getPackageName());
        overlayView = inflater.inflate(layoutId, null);

        int layout_parms;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layout_parms,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        windowManager.addView(overlayView, params);
    }

    public void hideOverlay() {
        if (!isOverlayVisible || overlayView == null) {
            return;
        }
        Log.d("OverlayManager", "Escondendo overlay.");
        isOverlayVisible = false;
        windowManager.removeView(overlayView);
        overlayView = null;
    }
}