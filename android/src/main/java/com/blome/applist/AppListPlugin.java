package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {
    private static final String TAG = "AppListPlugin";
    private static final boolean DEBUG_SAVE_BITMAPS = false; 

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        String myPackageName = getContext().getPackageName();
        PackageManager pm = getContext().getPackageManager();
        JSArray result = new JSArray();

        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_META_DATA);

        for (PackageInfo pi : installedPackages) {
            ApplicationInfo appInfo = pi.applicationInfo;
            if (appInfo == null) continue;

            Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
            boolean isMyApp = myPackageName.equals(appInfo.packageName);

            if (launchIntent != null && !isMyApp) {
                JSObject app = new JSObject();
                app.put("name", appInfo.loadLabel(pm).toString());
                app.put("packageName", appInfo.packageName);

                try {
                    Drawable iconDrawable = appInfo.loadIcon(pm);
                    Bitmap bitmap = drawableToBitmap(iconDrawable);

                    if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
                        Log.w(TAG, "Bitmap inválido para " + appInfo.packageName);
                        app.put("icon", "");
                    } else {
                        if (DEBUG_SAVE_BITMAPS) saveBitmapToCache(bitmap, appInfo.packageName + ".png");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                        app.put("icon", "data:image/png;base64," + base64Icon);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar ícone para: " + appInfo.packageName, e);
                    app.put("icon", "");
                }

                result.put(app);
            }
        }

        JSObject ret = new JSObject();
        ret.put("apps", result);
        call.resolve(ret);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptiveIcon = (AdaptiveIconDrawable) drawable;
            Drawable backgroundDr = adaptiveIcon.getBackground();
            Drawable foregroundDr = adaptiveIcon.getForeground();

            int width = adaptiveIcon.getIntrinsicWidth();
            int height = adaptiveIcon.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                width = 192;
                height = 192;
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            backgroundDr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            backgroundDr.draw(canvas);

            foregroundDr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            foregroundDr.draw(canvas);

            return bitmap;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) {
            width = 192; 
            height = 192;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void saveBitmapToCache(Bitmap bmp, String filename) {
        try {
            File f = new File(getContext().getCacheDir(), filename);
            FileOutputStream fos = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "Saved icon to " + f.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Erro salvando bitmap", e);
        }
    }
}