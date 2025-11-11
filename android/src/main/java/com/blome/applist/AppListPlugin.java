package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

import java.util.List;

@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {

    public static Set<String> blockedPackages = new HashSet<>();

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

                try {
                    Drawable iconDrawable = appInfo.loadIcon(pm);
                    String encodedIcon = drawableToBase64(iconDrawable);
                    app.put("icon", encodedIcon);
                } catch (Exception e) {
                }

                result.put(app);
            }
        }

        JSObject ret = new JSObject();
        ret.put("apps", result);
        call.resolve(ret);
    }

    @PluginMethod
    public void setBlockedPackages(PluginCall call) {
        try {
            JSArray packages = call.getArray("packages");
            if (packages == null) {
                call.reject("Erro: array 'packages' não foi enviado.");
                return;
            }

            blockedPackages.clear();
            for (int i = 0; i < packages.length(); i++) {
                blockedPackages.add(packages.getString(i));
            }

            Log.d("AppListPlugin", "Lista de apps bloqueados atualizada: " + blockedPackages.toString());
            call.resolve();
            
        } catch (Exception e) {
            Log.e("AppListPlugin", "Erro ao processar lista de pacotes", e);
            call.reject("Erro nativo: " + e.getMessage());
        }
    }

    private String drawableToBase64(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 96;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 96;

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}