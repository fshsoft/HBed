package com.java.health.care.bed.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.service.WebSocketService;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author fsh
 * @date 2022/08/02 13:53
 * @Description
 */
public class InputPassWordActivity extends BaseActivity {
    @BindView(R.id.input_password_send)
    AppCompatButton input_password_send;
    @BindView(R.id.input_password_close)
    AppCompatButton input_password_close;
    WebSocketService webSocketService;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @OnClick(R.id.input_password_close)
    public void closeClick() {
        if (webSocketService != null) {
            webSocketService.close();
        }
    }

    @OnClick(R.id.input_password_send)
    public void sendClick() {
        if (webSocketService != null) {
            webSocketService.send("010205154646100515454");
        }
    }

    @OnClick(R.id.input_password_btn)
    public void inputPassword() {
        goActivity(SettingActivity.class);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketService = ((WebSocketService.LocalBinder) service).getService();
            webSocketService.setWebSocketCallback(webSocketCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
        }
    };
    private WebSocketService.WebSocketCallback webSocketCallback = new WebSocketService.WebSocketCallback() {
        @Override
        public void onMessage(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText(text);
                }
            });
        }

        @Override
        public void onOpen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onOpen");
                }
            });
        }

        @Override
        public void onClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onClosed");
                }
            });
        }
    };
}
