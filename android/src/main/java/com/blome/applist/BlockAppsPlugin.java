package com.blome.applist;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import android.app.AppOpsManager;
import android.content.Context;

@CapacitorPlugin(name = "BlockApps")
public class BlockAppsPlugin extends Plugin {
    private static final String TAG = "BlockApps";

    @PluginMethod
    public void checkAndRequestPermissions(PluginCall call) {
        boolean hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext());
        boolean hasUsageStatsPermission = hasUsageStatsPermission();

        if (!hasOverlayPermission) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            getActivity().startActivityForResult(intent, 1); 
        }

        if (!hasUsageStatsPermission) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            getActivity().startActivityForResult(intent, 2);
        }

        JSObject result = new JSObject();
        result.put("overlay", hasOverlayPermission);
        result.put("usage", hasUsageStatsPermission);
        call.resolve(result);
    }


    @PluginMethod
    public void startService(PluginCall call) {
        JSArray packagesArray = call.getArray("packages");
        if (packagesArray == null) {
            call.reject("A lista de pacotes ('packages') não foi fornecida.");
            return;
        }
        
        try {
            List<String> packageList = packagesArray.toList();
            Log.d(TAG, "Iniciando o serviço com os pacotes: " + packageList.toString());

            Intent serviceIntent = new Intent(getContext(), AppBlockerService.class);
            serviceIntent.putStringArrayListExtra("blocked_packages", new ArrayList<>(packageList));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(serviceIntent);
            } else {
                getContext().startService(serviceIntent);
            }
            call.resolve();
        } catch (JSONException e) {
            call.reject("Erro ao processar a lista de pacotes.", e);
        }
    }

    @PluginMethod
    public void stopService(PluginCall call) {
        Log.d(TAG, "Parando o serviço.");
        Intent serviceIntent = new Intent(getContext(), AppBlockerService.class);
        getContext().stopService(serviceIntent);
        call.resolve();
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}