package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AppBlockerService extends AccessibilityService{
    private static final String TAG = "AppBlockerService";
    private OverlayManager overlayManager;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            if (event.getPackageName() == null || event.getClassName() == null) {
                return;
            }

            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();

            Log.d(TAG, "EVENTO: pkg=" + packageName + ", cls=" + className);

            if (className.equals("io.ionic.starter.MainActivity") || packageName.contains("launcher")) {
                Log.d(TAG, "App principal ou Launcher em foco, escondendo overlay.");
                if (overlayManager != null) {
                    overlayManager.hideOverlay();
                }
                return;
            }

            if (packageName.equals(getPackageName())) {
                Log.d(TAG, "Nosso overlay está em foco, não fazer nada.");
                return; 
            }

            if (AppListPlugin.blockedPackages.contains(packageName)) {
                Log.d(TAG, "APP BLOQUEADO! Tentando abrir: " + packageName);

                if (overlayManager != null) {
                    overlayManager.showOverlay(packageName);
                }

            } else {
                Log.d(TAG, "App permitido em primeiro plano: " + packageName);
                if (overlayManager != null) {
                    overlayManager.hideOverlay();
                }
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);

        this.overlayManager = new OverlayManager(this);
        Log.d(TAG, "Serviço de Bloqueio de Apps conectado e rodando.");
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Serviço de Bloqueio de Apps interrompido.");
    }
}