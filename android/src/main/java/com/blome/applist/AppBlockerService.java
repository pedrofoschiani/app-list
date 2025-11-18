package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

// --- NOVAS IMPORTAÇÕES ---
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
// --- FIM DAS NOVAS IMPORTAÇÕES ---

public class AppBlockerService extends AccessibilityService{
    private static final String TAG = "AppBlockerService";

    // --- Chaves do SharedPreferences (copiadas do AppListPlugin) ---
    private static final String PREFS_NAME = "AppBlockerPrefs";
    private static final String KEY_BLOCKED_PACKAGES = "blockedPackagesSet";
    private Set<String> localBlockedPackages = new HashSet<>(); // Cache local

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            if (event.getPackageName() == null || event.getClassName() == null) {
                return;
            }

            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();

            // --- INÍCIO DA NOVA LÓGICA ---
            // Não logamos mais cada evento, apenas os importantes

            if (packageName.equals("com.android.systemui") || packageName.equals(getPackageName())) {
                return; // Ignora SystemUI e o próprio app
            }

            // Lê a lista de bloqueio do cache local
            // (O cache é atualizado no onServiceConnected)
            if (this.localBlockedPackages.contains(packageName)) {
                Log.d(TAG, "App bloqueado detectado: " + packageName + ". Retornando para Home.");
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            } 
            // --- FIM DA NOVA LÓGICA ---
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

        // --- LÓGICA DE CARREGAMENTO ATUALIZADA ---
        // Em vez de chamar o AppListPlugin, o serviço lê o disco DIRETAMENTE
        // e salva em sua própria variável local.
        loadBlockedPackagesFromPrefs();
        // --- FIM DA ATUALIZAÇÃO ---

        Log.d(TAG, "Serviço de Bloqueio (PROCESSO SEPARADO) conectado. " + this.localBlockedPackages.size() + " apps na lista.");
    }

    /**
     * Novo método privado para ler o SharedPreferences diretamente.
     */
    private void loadBlockedPackagesFromPrefs() {
        try {
            // Usamos 'this' (que é o Contexto do Serviço)
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // Carrega a lista salva e salva no cache local 'localBlockedPackages'
            this.localBlockedPackages = prefs.getStringSet(KEY_BLOCKED_PACKAGES, new HashSet<String>());
            
            Log.d(TAG, "Lista de bloqueio carregada do disco para o cache do serviço.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar SharedPreferences: " + e.getMessage());
            this.localBlockedPackages = new HashSet<>(); // Usa uma lista vazia em caso de erro
        }
    }
}