package com.blome.applist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppBlockerService extends Service {
    private static final String TAG = "AppBlockerService";
    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "AppBlockerChannel";

    private Handler handler = new Handler();
    private OverlayManager overlayManager;
    private ArrayList<String> blockedPackages;
    private boolean isServiceRunning = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String foregroundApp = getForegroundApp();

            if (foregroundApp != null && blockedPackages != null && blockedPackages.contains(foregroundApp)) {
                overlayManager.showOverlay();
            } else {
                overlayManager.hideOverlay();
            }

            if(isServiceRunning) {
                handler.postDelayed(this, 500); 
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Serviço criado.");
        overlayManager = new OverlayManager(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Serviço iniciado.");

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Proteção Ativa")
                .setContentText("O aplicativo está monitorando o uso de apps.")
                .setSmallIcon(android.R.drawable.ic_dialog_info) 
                .build();
        startForeground(NOTIFICATION_ID, notification);

        if (intent != null && intent.hasExtra("blocked_packages")) {
            blockedPackages = intent.getStringArrayListExtra("blocked_packages");
            Log.d(TAG, "Lista de apps bloqueados recebida: " + blockedPackages.toString());
        }

        isServiceRunning = true;
        handler.post(runnable); 

        return START_STICKY; 
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Serviço destruído.");
        isServiceRunning = false;
        handler.removeCallbacks(runnable); 
        overlayManager.hideOverlay();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getForegroundApp() {
        String currentApp = null;
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }
        return currentApp;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Blocker Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}