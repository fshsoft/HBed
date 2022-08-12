package com.java.health.care.bed.device;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author fsh
 * @date 2022/08/12 16:43
 * @Description
 *
 * https://www.jianshu.com/p/57a91bd2b68f
 */
public class LocalWebSocketService extends Service {
    private MyHandler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new MyHandler(new WeakReference<Context>(this).get());
        String url = "ws://172.19.1.130:18432/web-socket/";
        startWebSocket(url);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private void startWebSocket(String wsUrl) {
        Request request = new Request.Builder().url(wsUrl).build();
        OkHttpClient client = new OkHttpClient();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(okhttp3.WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
//                handler.obtainMessage(0, "WebSocket开启").sendToTarget();
//                webSocket.send("收到消息了吗");
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                handler.obtainMessage(1, text).sendToTarget();
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(okhttp3.WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
            }

            @Override
            public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(okhttp3.WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                handler.obtainMessage(4, "WebSocket连接已关闭").sendToTarget();
            }
        });
        client.dispatcher().executorService().shutdown();
    }

    public class MyHandler extends Handler {
        private Context context;

        public MyHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null != context) {
                switch (msg.what) {
                    case 1:
                        String json = (String) msg.obj;
                        if (!StringUtils.isEmpty(json)) {
                            try {
                                JSONObject jsonObject = new JSONObject(json);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 4:
                        ToastUtils.showShort((String) msg.obj);
                        break;
                    case 0:
                        ToastUtils.showShort((String) msg.obj);
                        break;
                }
            }
        }
    }
}


