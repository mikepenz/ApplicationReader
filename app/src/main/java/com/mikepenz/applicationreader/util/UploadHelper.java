package com.mikepenz.applicationreader.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mikepenz.applicationreader.entity.AppInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikepenz on 18.10.14.
 */
public class UploadHelper {
    private static UploadHelper instance = null;

    private UploadHelper(AppCompatActivity act, List<AppInfo> applicationList) {
        this.act = act;
        try {
            PackageInfo pInfo = act.getPackageManager().getPackageInfo(act.getPackageName(), 0);
            applicationReaderVersion = pInfo.versionCode;
        } catch (Exception ignored) {
        }

        if (applicationList != null) {
            this.applicationList = applicationList;
        } else {
            this.applicationList = new ArrayList<>();
        }
    }

    private AppCompatActivity act;
    private List<AppInfo> applicationList;
    private int applicationReaderVersion = -1;

    public static UploadHelper getInstance(AppCompatActivity act, List<AppInfo> applicationList) {
        if (instance == null) {
            instance = new UploadHelper(act, applicationList);
        } else if (act != null) {
            instance.act = act;
        }
        return instance;
    }

    public void upload(AppInfo appInfo) {
        new UploadComponentInfoTask().execute(appInfo);
    }

    public void uploadAll() {
        new UploadComponentInfoTask().execute();
    }

    public class UploadComponentInfoTask extends AsyncTask<AppInfo, Integer, Boolean> {
        ProgressDialog mProgressDialog = new ProgressDialog(act);

        @Override
        protected void onPreExecute() {
            if (!isAvailiable(act)) {
                this.cancel(true);

                Toast.makeText(act, "No internet connection!", Toast.LENGTH_LONG).show();
            } else {
                mProgressDialog.setTitle("Uploading...");
                mProgressDialog.setMessage("Processing and uploading your applications. Please be patient!");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setOnCancelListener(dialog -> cancel(true));
                mProgressDialog.setMax(applicationList.size());
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
            }

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(AppInfo... params) {
            boolean updateRequired = false;
            if (params == null || params.length == 0) {
                mProgressDialog.setMax(applicationList.size());

                for (AppInfo ai : applicationList) {
                    updateRequired = postData(applicationReaderVersion, ai.getPackageName(), ai.getName(), ai.getActivityName(), ai.getName(), ai.getPackageName() + "/" + ai.getActivityName());
                    publishProgress(mProgressDialog.getProgress() + 1);
                    if (updateRequired) {
                        break;
                    }
                    if (isCancelled()) {
                        break;
                    }
                }
            } else if (params.length == 1) {
                updateRequired = postData(applicationReaderVersion, params[0].getPackageName(), params[0].getName(),
                        params[0].getActivityName(), params[0].getName(),
                        params[0].getPackageName() + "/" + params[0].getActivityName());
                publishProgress(applicationList.size());
            }

            return updateRequired;
        }

        @Override
        protected void onPostExecute(Boolean updateRequired) {
            mProgressDialog.dismiss();

            if (updateRequired) {
                Toast.makeText(act, "ApplicationReader is outdated. Please updated!\nThere was an important API update!", Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(updateRequired);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values.length > 0) {
                mProgressDialog.setProgress(values[0]);
            }
            super.onProgressUpdate(values);
        }

    }

    public boolean postData(int applicationVersion, String package_name, String app_name,
                            String activity, String activity_name, String component_info) {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("https://api.yourdomain.com/add/index.php");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("applicationreader_version", String.valueOf(applicationVersion)));
            nameValuePairs.add(new BasicNameValuePair("package_name", package_name));
            nameValuePairs.add(new BasicNameValuePair("app_name", app_name));
            nameValuePairs.add(new BasicNameValuePair("activity", activity));
            nameValuePairs.add(new BasicNameValuePair("activity_name", activity_name));
            nameValuePairs.add(new BasicNameValuePair("component_info", component_info));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            if (responseString.equals("ApplicationReader outdated")) {
                return true;
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    static boolean isAvailiable(Context ctx) {
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        return i.isAvailable();
    }
}
