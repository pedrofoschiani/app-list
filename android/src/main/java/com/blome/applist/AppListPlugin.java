package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        String myPackageName = getContext().getPackageName();
        PackageManager pm = getContext().getPackageManager();
        JSArray result = new JSArray();

        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_META_DATA);

        for (PackageInfo pi : installedPackages) {
            ApplicationInfo appInfo = pi.applicationInfo;
            if (appInfo == null) {
                continue;
            }

            Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
            boolean isMyApp = myPackageName.equals(appInfo.packageName);


            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            boolean isUpdatedSystemApp = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

            if (launchIntent != null && !isMyApp && (!isSystemApp || isUpdatedSystemApp)) {
                JSObject app = new JSObject();
                app.put("name", appInfo.loadLabel(pm).toString());
                app.put("packageName", appInfo.packageName);
                result.put(app);
            }
        }

        JSObject ret = new JSObject();
        ret.put("apps", result);
        call.resolve(ret);
    }
}