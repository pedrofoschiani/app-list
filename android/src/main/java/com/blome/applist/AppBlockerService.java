package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast; // <-- Adicione esta importação

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

            Log.d(TAG, "EVENTO: pkg=" + packageName + ", cls=" + className);

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
                
                Toast.makeText(this, "Este aplicativo está bloqueado.", Toast.LENGTH_SHORT).show();
                
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
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

        Log.d(TAG, "Serviço de Bloqueio de Apps (Modo Interceptação) conectado.");
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Serviço de Bloqueio de Apps interrompido.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}