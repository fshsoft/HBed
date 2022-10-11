package com.java.health.care.bed.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseApplication;
import com.java.health.care.bed.constant.SP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * 下载工具类（开发中一般用于APK应用升级）
 * https://www.jianshu.com/p/3e5af8827157
 */
public class LinDownloadAPk
{
    private static int FILE_LEN = 0;
    private static RemoteViews mNotifiviews;
    public static String APK_UPGRADE = Environment.getExternalStorageDirectory().getPath()+"/HBed/apk/KYC.apk";
    private static PendingIntent nullIntent;
    private static Context mContext;

    /**
     * 判断8.0 安装权限
     */
    public static void downApk(Context context, String url) {
        mContext = context;
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = context.getPackageManager().canRequestPackageInstalls();
            if (b) {
                downloadAPK( url, null);
            } else {
                //请求安装未知应用来源的权限
                startInstallPermissionSettingActivity();
            }
        } else {
            downloadAPK( url, null);
        }
    }

    /**
     * 开启安装APK权限(适配8.0)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        mContext.startActivity(intent);
    }

    /**
     * 下载APK文件
     */
    private static void downloadAPK( String url,String localAddress)
    {
        // 下载
        if (localAddress != null)
        {
            APK_UPGRADE = localAddress;
        }

        new UpgradeTask().execute(url);
    }

    static class UpgradeTask extends AsyncTask<String, Integer, Void>
    {
        @Override
        protected void onPreExecute()
        {
            // 发送通知显示升级进度
//            sendNotify();
        }
        @Override
        protected Void doInBackground(String... params)
        {

            String apkUrl = params[0];
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                ////设置header内的参数 connection.setRequestProperty("健, "值")
                conn.setRequestMethod("POST");
                conn.setUseCaches(false); // Post请求不能使用缓存
                conn.setDoInput(true);// 设置是否从HttpURLConnection输入，默认值为 true
                conn.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出，默认值为 false

                conn.setRequestProperty("authorization", SPUtils.getInstance().getString(SP.TOKEN));
                conn.setRequestProperty("Content-Type", "application/json");
                ////设置body内的参数，put到JSONObject中
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", SPUtils.getInstance().getString(SP.APK_NAME));
                jsonObject.put("source", "pad");
                jsonObject.put("strategy","file_downloadLfs");

                // 得到请求的输出流对象
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
                writer.write(jsonObject.toString());
                writer.flush();


                // 设置连接超时时间
                conn.setConnectTimeout(25000);
                // 设置下载数据超时时间
                conn.setReadTimeout(25000);

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    return null;// 服务端错误响应
                }
                is = conn.getInputStream();

                //文件大小
                FILE_LEN = conn.getContentLength();

                File apkFile = new File(APK_UPGRADE);
                // 如果文件夹不存在则创建
                if (!apkFile.getParentFile().exists())
                {
                    apkFile.getParentFile().mkdirs();
                }
                fos = new FileOutputStream(apkFile);
                byte[] buffer = new byte[8024];
                int len = 0;
                int loadedLen = 0;// 当前已下载文件大小
                // 更新10次
                int updateSize = FILE_LEN / 10;
                int num = 0;
                while (-1 != (len = is.read(buffer)))
                {
                    loadedLen += len;
                    fos.write(buffer, 0, len);
                    if (loadedLen > updateSize * num)
                    {
                        num++;
                        publishProgress(loadedLen);
                    }
                }
                fos.flush();
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (SocketTimeoutException e)
            {
                // 处理超时异常，提示用户在网络良好情况下重试
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally
            {
                if (is != null)
                {
                    try
                    {
                        is.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (fos != null)
                {
                    try
                    {
                        fos.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            // 更新通知
//            updateNotify(values[0]);
        }

        @Override
        protected void onPostExecute(Void result)
        {
            Toast.makeText(mContext, "下载完成", Toast.LENGTH_LONG).show();
            startIntent(APK_UPGRADE);
        }
    }



    /**
     * 调往系统APK安装界面（适配7.0）
     * @return
     */
    public static void startIntent( String filePath) {
        //apk文件的本地路径
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri contentUri = getUriForFile(apkFile);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    /**
     * 将文件转换成uri
     * @return
     */
    public static Uri getUriForFile(File file) {

        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(mContext, mContext.getPackageName()+".file_provider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }
}