package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class AppBlockerService extends AccessibilityService {
    private static final String TAG = "AppBlockerService";

    private static final String PREFS_NAME = "AppBlockerPrefs";
    private static final String KEY_BLOCKED_PACKAGES = "blockedPackagesSet";
    
    // Cache local para acesso rápido
    private Set<String> localBlockedPackages = new HashSet<>(); 

    // Listener mantido como variável de classe para não ser coletado pelo GC
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // O Try-Catch envolve TODO o método para garantir que o serviço nunca morra
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                if (event.getPackageName() == null) return;

                String packageName = event.getPackageName().toString();

                // Ignora SystemUI (barra de notificação) e o próprio app Blome para evitar loop
                if (packageName.equals("com.android.systemui") || packageName.equals(getPackageName())) {
                    return;
                }

                // Verifica se o pacote está na lista carregada na memória
                if (this.localBlockedPackages.contains(packageName)) {
                    Log.d(TAG, "BLOQUEANDO APP: " + packageName);
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                } 
            }
        } catch (Exception e) {
            // Se der erro, loga e continua vivo. O serviço NÃO para.
            Log.e(TAG, "Erro fatal no loop de acessibilidade (ignorado)", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Serviço de acessibilidade interrompido pelo Android.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        
        // Null = ouve todos os apps
        info.packageNames = null; 

        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
                     
        setServiceInfo(info);

        // Inicia o listener de atualizações em tempo real
        setupLiveUpdates(); // <--- Corrigido o erro de digitação (era etup)

        // Carrega a lista inicial
        loadBlockedPackagesFromPrefs();

        Log.d(TAG, "Serviço Conectado e Listener Ativo. Apps na lista: " + this.localBlockedPackages.size());
    }

    private void setupLiveUpdates() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (KEY_BLOCKED_PACKAGES.equals(key)) {
                        Log.d(TAG, "UPDATER: A lista mudou no Capacitor! Recarregando...");
                        loadBlockedPackagesFromPrefs();
                    }
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(prefsListener);
            Log.d(TAG, "Listener registrado com sucesso.");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar listener: " + e.getMessage());
        }
    }
    
    private void loadBlockedPackagesFromPrefs() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // IMPORTANTE: Criamos um novo HashSet para garantir segurança na memória
            Set<String> savedSet = prefs.getStringSet(KEY_BLOCKED_PACKAGES, new HashSet<>());
            this.localBlockedPackages = new HashSet<>(savedSet);
            
            Log.d(TAG, "Cache atualizado. Total bloqueado: " + this.localBlockedPackages.size());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao ler SharedPreferences: " + e.getMessage());
            this.localBlockedPackages = new HashSet<>();
        }
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        // Boa prática: remover o listener ao desligar o serviço
        if (prefsListener != null) {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
        return super.onUnbind(intent);
    }
}
