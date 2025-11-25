package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo; // NOVO
import android.content.pm.PackageManager; // NOVO
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AppBlockerService extends AccessibilityService {
    private static final String TAG = "AppBlockerService";
    // Mudei o ID novamente caso precise resetar configurações, mas o foco agora é o conteúdo
    private static final String CHANNEL_ID = "blome_security_alert_v3"; 
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            if (event.getPackageName() == null) {
                return;
            }

            String packageName = event.getPackageName().toString();

            // --- NOVO: LOG DE DEPURAÇÃO ---
            // Isso vai mostrar no Logcat TODOS os pacotes que o serviço "vê"
            Log.d(TAG, ">>> Janela detectada: " + packageName); 

            if (packageName.equals("com.android.systemui")) return;
            if (packageName.equals(getPackageName())) return;

            if (AppListPlugin.blockedPackages.contains(packageName)) {
                Log.w(TAG, "BLOQUEIO ATIVADO PARA: " + packageName);
                
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);

                // --- NOVO: Passando o pacote para pegar o nome ---
                sendBlockedNotification(packageName); 
            } 
        }
    }

    private void sendBlockedNotification(String blockedPackage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tenta pegar o nome legível do app (Ex: "WhatsApp" em vez de "com.whatsapp")
        String appName = blockedPackage;
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(blockedPackage, 0);
            appName = (String) pm.getApplicationLabel(ai);
        } catch (PackageManager.NameNotFoundException e) {
            // Mantém o nome do pacote se der erro
        }

        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent == null) return;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        int iconResId = android.R.drawable.ic_dialog_alert; 

        Notification notification = builder
                .setContentTitle("Aplicativo Bloqueado")
                // --- NOVO: Mostra qual app causou o bloqueio ---
                .setContentText("O acesso ao " + appName + " não é permitido agora.") 
                .setSmallIcon(iconResId)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Alertas de Bloqueio",
                        NotificationManager.IMPORTANCE_HIGH 
                );
                channel.setDescription("Notifica bloqueios ativos");
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // ... (Restante dos métodos onInterrupt e onServiceConnected iguais ao anterior)
    @Override
    public void onInterrupt() { }

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
        createNotificationChannel();
    }
}