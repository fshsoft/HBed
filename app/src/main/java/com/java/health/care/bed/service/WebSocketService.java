package com.java.health.care.bed.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @author fsh
 * @date 2022/08/15 10:27
 * @Description
 */
public class WebSocketService extends Service {
    private static final String TAG = WebSocketService.class.getSimpleName();
//    private static final String WS = "ws://192.168.0.13:8000/caskyc?app_key=client_001&secret=ef9b84b83b693bbf&inpatient_ward=1001&type=1";
//    private static final String WS = "ws://rpi.zcc93.cn:8000/caskyc?app_key=client_001&secret=ef9b84b83b693bbf&inpatient_ward=1001&type=1&userId=1888";
    private static final String WS = "ws://192.168.0.12:8000?app_key=client_001&secret=ef9b84b83b693bbf&inpatient_ward=1001&type=1&bed_id=111";

    private WebSocket webSocket;
    private WebSocketCallback webSocketCallback;
    private int reconnectTimeout = 5000;
    private boolean connected = false;
    private int connectedNum = 0;

    private Handler handler = new Handler();

    public class LocalBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        webSocket = connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            close();
        }
    }

    private WebSocket connect() {
        Log.d(TAG, "connect " + WS);
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(WS).build();
        return client.newWebSocket(request, new WebSocketHandler());
    }

    public void send(String text) {
        Log.d(TAG, "send " + text);
        if (webSocket != null) {
            webSocket.send(text);
        }
    }

    public void send(byte[] data){
        if(webSocket!=null){
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            ByteString byteString = ByteString.of(byteBuffer);
            webSocket.send(byteString);
            Log.d(TAG, "send " + byteString);
        }
    }
    public void close() {
        if (webSocket != null) {
            boolean shutDownFlag = webSocket.close(1000, "manual close");
            Log.d(TAG, "shutDownFlag " + shutDownFlag);
            webSocket = null;
        }
    }

    private void reconnect() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "reconnect...");
                if (!connected) {
                    connectedNum++;
                    if(connectedNum<5){
                        connect();
                        handler.postDelayed(this, reconnectTimeout);
                    }else {
                        Log.d(TAG,"重连失败，请检查网络或服务端");
                    /*    if (webSocketCallback != null) {
                            webSocketCallback.onMessage("重连失败，请检查网络或服务端");
                        }*/
                    }
                }
            }
        }, reconnectTimeout);
    }

    private class WebSocketHandler extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d(TAG, "onOpen");
            if (webSocketCallback != null) {
                webSocketCallback.onOpen();
            }
            connected = true;
            connectedNum=0;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "onMessage " + text);
            if (webSocketCallback != null) {
                webSocketCallback.onMessage(text);
            }

         /*   //收到服务器端发送来的信息后，每隔10秒发送一次心跳包
            final String message = "{\"type\":\"heartbeat\",\"user_id\":\"1888\"}";
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    webSocket.send(message);
                }
            },100,25000);*/
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d(TAG, "onClosed");
            if (webSocketCallback != null) {
                webSocketCallback.onClosed();
            }
            connected = false;
            reconnect();
        }

        /**
         * Invoked when a web socket has been closed due to an error reading from or writing to the
         * network. Both outgoing and incoming messages may have been lost. No further calls to this
         * listener will be made.
         */
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d(TAG, "onFailure " + t.getMessage());
            connected = false;
            reconnect();
        }
    }

    /**
     * 只暴露需要的回调给页面，onFailure 你给了页面，页面也无能为力不知怎么处理
     */
    public interface WebSocketCallback {
        void onMessage(String text);

        void onOpen();

        void onClosed();
    }

    public void setWebSocketCallback(WebSocketCallback webSocketCallback) {
        this.webSocketCallback = webSocketCallback;
    }

}

/**
 * public class MainActivity extends AppCompatActivity {
 *     WebSocketService webSocketService;
 *     TextView tvMessage;
 *     EditText etValue;
 *
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         setContentView(R.layout.activity_main);
 *
 *         bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
 *
 *         btnClose.setOnClickListener(new View.OnClickListener() {
 *             @Override
 *             public void onClick(View v) {
 *                 if (webSocketService != null) {
 *                     webSocketService.close();
 *                 }
 *             }
 *         });
 *         btnSend.setOnClickListener(new View.OnClickListener() {
 *             @Override
 *             public void onClick(View v) {
 *                 if (webSocketService != null) {
 *                     webSocketService.send(etValue.getText().toString().trim());
 *                 }
 *             }
 *         });
 *     }
 *
 *     @Override
 *     protected void onDestroy() {
 *         super.onDestroy();
 *         unbindService(serviceConnection);
 *     }
 *
 *     private ServiceConnection serviceConnection = new ServiceConnection() {
 *         @Override
 *         public void onServiceConnected(ComponentName name, IBinder service) {
 *             webSocketService = ((WebSocketService.LocalBinder) service).getService();
 *             webSocketService.setWebSocketCallback(webSocketCallback);
 *         }
 *
 *         @Override
 *         public void onServiceDisconnected(ComponentName name) {
 *             webSocketService = null;
 *         }
 *     };
 *
 *     private WebSocketService.WebSocketCallback webSocketCallback = new WebSocketService.WebSocketCallback() {
 *         @Override
 *         public void onMessage(final String text) {
 *             runOnUiThread(new Runnable() {
 *                 @Override
 *                 public void run() {
 *                     tvMessage.setText(text);
 *                 }
 *             });
 *         }
 *
 *         @Override
 *         public void onOpen() {
 *             runOnUiThread(new Runnable() {
 *                 @Override
 *                 public void run() {
 *                     tvMessage.setText("onOpen");
 *                 }
 *             });
 *         }
 *
 *         @Override
 *         public void onClosed() {
 *             runOnUiThread(new Runnable() {
 *                 @Override
 *                 public void run() {
 *                     tvMessage.setText("onClosed");
 *                 }
 *             });
 *         }
 *     };
 * }
 */
