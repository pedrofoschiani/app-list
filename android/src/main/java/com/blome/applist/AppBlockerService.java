package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

// Se você estiver usando AndroidX, pode importar: import androidx.core.app.NotificationCompat;
// Caso contrário, usaremos o android.app.Notification.Builder nativo para garantir compatibilidade.

public class AppBlockerService extends AccessibilityService {
    private static final String TAG = "AppBlockerService";
    private static final String CHANNEL_ID = "blome_blocked_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            if (event.getPackageName() == null || event.getClassName() == null) {
                return;
            }

            String packageName = event.getPackageName().toString();
            // String className = event.getClassName().toString(); // Não estamos usando, mas pode manter

            if (packageName.equals("com.android.systemui")) {
                return;
            }

            if (packageName.equals(getPackageName())) {
                return; 
            }

            if (AppListPlugin.blockedPackages.contains(packageName)) {
                Log.d(TAG, "App bloqueado detectado: " + packageName + ". Bloqueando...");
                
                // 1. Força a ida para a Home
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

                // 2. Envia a notificação visual para o aluno
                sendBlockedNotification();
            } 
        }
    }

    /**
     * Método responsável por criar e exibir a notificação
     */
    private void sendBlockedNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para abrir o SEU aplicativo (BLOME) ao clicar na notificação
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent == null) {
            Log.e(TAG, "Não foi possível encontrar a Intent de lançamento do próprio app.");
            return;
        }
        // Flags importantes para garantir que o app abra corretamente vindo do background
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        // Tente usar um ícone que já exista no seu projeto (ex: R.mipmap.ic_launcher ou um drawable específico)
        // Aqui estou usando um ícone genérico do sistema para garantir que não dê erro de compilação
        int iconResId = android.R.drawable.ic_dialog_alert; 
        
        // Se você tiver acesso à classe R do seu pacote, troque por: R.drawable.seu_icone_de_bloqueio

        Notification notification = builder
                .setContentTitle("Acesso Bloqueado")
                .setContentText("Toque aqui para ver os aplicativos liberados.")
                .setSmallIcon(iconResId)
                .setContentIntent(pendingIntent) // Define a ação do clique
                .setAutoCancel(true) // Remove a notificação ao clicar
                .build();

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Cria o canal de notificação (Obrigatório para Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Bloqueio de Apps",
                        NotificationManager.IMPORTANCE_HIGH // IMPORTANCE_HIGH faz a notificação "popar" na tela
                );
                channel.setDescription("Notifica quando um aplicativo bloqueado é acessado");
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
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
        
        // Configuração padrão do serviço
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        setServiceInfo(info);

        AppListPlugin.loadBlockedPackages(this);

        // Inicializa o canal de notificação assim que o serviço conecta
        createNotificationChannel();

        Log.d(TAG, "Serviço de Bloqueio conectado e canal de notificação criado.");
    }
}