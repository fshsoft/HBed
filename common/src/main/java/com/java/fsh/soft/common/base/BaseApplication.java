package com.java.fsh.soft.common.base;

import com.blankj.utilcode.util.Utils;
import com.java.fsh.soft.common.net.ErrorCallback;
import com.kingja.loadsir.core.LoadSir;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * @author Administrator
 */
public class BaseApplication extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //数据库的初始化
        LitePal.initialize(this);
        //工具库的初始化
        Utils.init(this);
        //加载反馈管理框架初始化
        LoadSir.beginBuilder()
                .addCallback(new ErrorCallback())
                .commit();
    }
}
