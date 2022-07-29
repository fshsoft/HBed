package com.java.fsh.soft.common.net;

import com.java.fsh.soft.common.R;
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
