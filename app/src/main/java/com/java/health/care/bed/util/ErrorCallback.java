package com.java.health.care.bed.util;

import com.java.health.care.bed.R;
import com.kingja.loadsir.callback.Callback;

/**
 * Description: 网络错误提示界面
 * @author Administrator
 */
public class ErrorCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.network_error;
    }
}
