package com.blome.applist;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AppBlockerService extends AccessibilityService{
    private static final String TAG = "AppBlockerService";

    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "AppBlockerChannel";
    private NotificationManager notificationManager;

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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        Notification notification = buildNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);

        AppListPlugin.loadBlockedPackages(this);

        Log.d(TAG, "Serviço de Bloqueio de Apps (Modo Interceptação) conectado.");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Serviço de Bloqueio de Apps",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Informa que o serviço de bloqueio de apps está ativo.");
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        int icon = getApplicationInfo().icon;
        if (icon == 0) {
            icon = android.R.drawable.ic_dialog_info;
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Proteção de Apps Ativada")
                .setContentText("O bloqueio de aplicativos está monitorando o uso.")
                .setSmallIcon(icon) 
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        Log.e(TAG, "Serviço de Bloqueio de Apps interrompido.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        Log.d(TAG, "Serviço de Bloqueio de Apps destruído.");
    }
}