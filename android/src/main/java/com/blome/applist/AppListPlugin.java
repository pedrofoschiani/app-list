package com.blome.applist;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
                    // --- NOVA LÓGICA DE CARREGAMENTO DE ÍCONE (MÉTODO DE BAIXO NÍVEL) ---
                    Drawable iconDrawable;
                    if (appInfo.icon != 0) { // Garante que o app tem um recurso de ícone definido
                        // 1. Pega os recursos do outro aplicativo
                        Resources resources = pm.getResourcesForApplication(appInfo.packageName);
                        // 2. Puxa o ícone usando o ID do recurso diretamente
                        iconDrawable = resources.getDrawable(appInfo.icon, null);
                    } else {
                        // Se o app não definir um ícone, usa o padrão do sistema como último caso
                        iconDrawable = pm.getDefaultActivityIcon();
                    }
                    // --- FIM DA NOVA LÓGICA ---

                    Bitmap bitmap = drawableToBitmap(iconDrawable);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    app.put("icon", "data:image/png;base64," + base64Icon);
                } catch (Exception e) {
                    Log.e("AppListPlugin", "Falha final ao processar ícone para: " + appInfo.packageName, e);
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