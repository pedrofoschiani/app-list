package com.blome.applist;

import android.content.Context;

import android.content.Intent;
import android.util.Log;

public class OverlayManager {

    private Context context;

    public OverlayManager(Context context) {
        this.context = context;
    }

    public void showOverlay(string packageName) {
        Log.d("OverlayManager", "Showing overlay" + packageName);
        // Implement overlay display logic here
    }

    public void hideOverlay() {
        Log.d("OverlayManager", "Hiding overlay");
        // Implement overlay hide logic here
    }
}
