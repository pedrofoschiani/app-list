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

            if (packageName.equals("com.android.systemui")) {
                Log.d(TAG, "Ignorando SystemUI, mantendo estado atual do overlay.");
                return;
            }

            if (AppListPlugin.blockedPackages.contains(packageName)) {
                
                if (packageName.equals(getPackageName()) || className.equals("io.ionic.starter.MainActivity")) {
                     Log.d(TAG, "Nosso app principal em foco, escondendo overlay.");
                     if (overlayManager != null) {
                         overlayManager.hideOverlay();
                     }
                } else {
                    Log.d(TAG, "App bloqueado em foco: " + packageName + ", mostrando overlay.");
                    if (overlayManager != null) {
                        overlayManager.showOverlay(packageName);
                    }
                } 
            } else {
                Log.d(TAG, "App permitido ou Launcher em foco: " + packageName + ", escondendo overlay.");
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

        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);

        this.overlayManager = new OverlayManager(this);
        Log.d(TAG, "Serviço de Bloqueio de Apps conectado e rodando.");
    }

    @Override
    public void onInterrupt() {
        if (overlayManager != null) {
             overlayManager.hideOverlay();
        }
        Log.e(TAG, "Serviço de Bloqueio de Apps interrompido.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayManager != null) {
             overlayManager.hideOverlay();
        }
    }
}