package com.mikepenz.applicationreader.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.mikepenz.applicationreader.util.UploadHelper;

public class ApplicationReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences("com.mikepenz.applicationreader", 0);
        if (settings.getBoolean("autouploadenabled", false)) {
            if (intent.getData() != null) {
                String packageName = intent.getData().toString();
                if (packageName.contains("package:")) {
                    packageName = packageName.replace("package:", "");
                    Intent newAppsIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

                    if (newAppsIntent != null) {
                        UploadComponentInfoTask ucit = new UploadComponentInfoTask();
                        ucit.intent = newAppsIntent;
                        ucit.ctx = context;
                        ucit.pm = context.getPackageManager();
                        ucit.execute();
                    }
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadComponentInfoTask extends AsyncTask<Void, Void, Void> {

        Intent intent;
        PackageManager pm;
        Context ctx;

        @Override
        protected Void doInBackground(Void... params) {
            int applicationReaderVersion = -1;
            try {
                PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                applicationReaderVersion = pInfo.versionCode;
            } catch (Exception ignored) {
            }

            try {

                ActivityInfo ai = pm.getActivityInfo(intent.getComponent(), 0);
                Log.v("ApplicatoinReader", "Application installed/update. Autoupload enabled: " + ai.packageName + " ComponentInfo: " + ai.packageName + "/" + ai.name);

                UploadHelper.getInstance(null, null).postData(applicationReaderVersion, ai.packageName, ai.loadLabel(pm).toString(), ai.name, ai.loadLabel(pm).toString(), ai.packageName + "/" + ai.name);
            } catch (Exception ex) {
                Log.e("ApplicatoinReader", ex.toString());
            }

            return null;
        }
    }

}
