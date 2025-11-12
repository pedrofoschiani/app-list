package com.blome.applist;

import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

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

import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.content.ComponentName;
import android.net.Uri;

@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {

    public static Set<String> blockedPackages = new HashSet<>();

    private static final String PREFS_NAME = "AppBlockerPrefs";
    private static final String KEY_BLOCKED_PACKAGES = "blockedPackagesSet";

    @PluginMethod
    public void isAccessibilityServiceEnabled(PluginCall call) {
        String serviceName = new ComponentName(getContext(), AppBlockerService.class).flattenToString();

        String enabledServices;
        try {
            enabledServices = Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
        } catch (Exception e) {
            Log.e("AppListPlugin", "Erro ao ler serviços de acessibilidade", e);
            call.resolve(new JSObject().put("enabled", false));
            return;
        }

        if (enabledServices == null) {
            call.resolve(new JSObject().put("enabled", false));
            return;
        }

        boolean isEnabled = false;
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);

        while (splitter.hasNext()) {
            if (splitter.next().equalsIgnoreCase(serviceName)) {
                isEnabled = true;
                break;
            }
        }

        JSObject ret = new JSObject();
        ret.put("enabled", isEnabled);
        call.resolve(ret);
    }

    @PluginMethod
    public void openAccessibilitySettings(PluginCall call) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        getContext().startActivity(intent);
        call.resolve();
    }

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
            ArrayList<String> packages = call.getArray("packages", new ArrayList<String>());
            Set<String> packageSet = new HashSet<>(packages);
            
            saveBlockedPackages(getContext(), packageSet);

            blockedPackages = packageSet;

            Log.d("AppListPlugin", "Lista de apps bloqueados atualizada: " + blockedPackages.toString());
            call.resolve();

        } catch (Exception e) {
            Log.e("AppListPlugin", "Erro ao processar lista de pacotes", e);
            call.reject("Erro nativo: " + e.getMessage());
        }
    }

    private static void saveBlockedPackages(Context context, Set<String> packages) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_BLOCKED_PACKAGES, packages);
        editor.apply();
    }

    public static void loadBlockedPackages(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> savedPackages = prefs.getStringSet(KEY_BLOCKED_PACKAGES, new HashSet<String>());
        
        blockedPackages = savedPackages;
        Log.d("AppListPlugin", "Lista de bloqueio carregada do disco. " + blockedPackages.size() + " apps.");
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