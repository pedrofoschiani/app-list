package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AppBlockerService extends AccessibilityService{
    private static final String TAG = "AppBlockerService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            if (event.getPackageName() == null || event.getClassName() == null) {
                return;
            }

            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();

            if (packageName.equals("com.android.systemui")) {
                Log.d(TAG, "Ignorando SystemUI.");
                return;
            }

            if (packageName.equals(getPackageName())) {
                Log.d(TAG, "Ignorando nosso próprio app.");
                return; 
            }

            if (AppListPlugin.blockedPackages.contains(packageName)) {
                Log.d(TAG, "App bloqueado detectado: " + packageName + ". Retornando para Home.");
                
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            } 
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Serviço de acessibilidade interrompido.");
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

        AppListPlugin.loadBlockedPackages(this);

        Log.d(TAG, "Serviço de Bloqueio de Apps (Modo Interceptação) conectado.");
    }
}