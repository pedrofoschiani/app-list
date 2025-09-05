package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable; 
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build; 
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi; 

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
        JSArray result = new JSArray();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo ri : installedApps) {
            ApplicationInfo appInfo = ri.activityInfo.applicationInfo;
            boolean isMyApp = myPackageName.equals(appInfo.packageName);

            if (!isMyApp) {
                JSObject app = new JSObject();
                app.put("name", appInfo.loadLabel(pm).toString());
                app.put("packageName", appInfo.packageName);

                try {
                    Drawable iconDrawable = appInfo.loadIcon(pm);
                    Bitmap bitmap = drawableToBitmap(iconDrawable);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    app.put("icon", "data:image/png;base64," + base64Icon);
                } catch (Exception e) {
                    Log.e("AppListPlugin", "Erro ao processar ícone para: " + appInfo.packageName, e);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            AdaptiveIconDrawable adaptiveIcon = (AdaptiveIconDrawable) drawable;
            Drawable backgroundDr = adaptiveIcon.getBackground();
            Drawable foregroundDr = adaptiveIcon.getForeground();

            int width = adaptiveIcon.getIntrinsicWidth();
            int height = adaptiveIcon.getIntrinsicHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            backgroundDr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            backgroundDr.draw(canvas);

            foregroundDr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            foregroundDr.draw(canvas);

            return bitmap;
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 128;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 128;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}