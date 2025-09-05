package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

import java.io.ByteArrayOutputStream;
import java.util.List;

@CapacitorPlugin(name = "AppList")
public class AppListPlugin extends Plugin {

    @PluginMethod
    public void getInstalledApps(PluginCall call) {
        String myPackageName = getContext().getPackageName();
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        JSArray result = new JSArray();

        for (ApplicationInfo appInfo : apps) {
            Intent launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName);
            boolean isMyApp = myPackageName.equals(appInfo.packageName);

            if (launchIntent != null && !isMyApp) {
                JSObject app = new JSObject();
                app.put("name", pm.getApplicationLabel(appInfo).toString());
                app.put("packageName", appInfo.packageName);

                try {
                    Drawable iconDrawable = appInfo.loadIcon(pm);

                    Bitmap bitmap = drawableToBitmap(iconDrawable);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    app.put("icon", "data:image/png;base64," + base64Icon);
                } catch (Exception e) {
                    Log.e("AppListPlugin", "Erro ao converter ícone para: " + appInfo.packageName, e);
                }

                result.put(app);
            }
        }

        JSObject ret = new JSObject();
        ret.put("apps", result);
        call.resolve(ret);
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 96;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 96;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}