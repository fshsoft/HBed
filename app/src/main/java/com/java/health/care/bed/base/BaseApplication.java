package com.java.health.care.bed.base;

import com.blankj.utilcode.util.Utils;
import com.java.health.care.bed.model.Music;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.util.List;

/**
 * @author Administrator
 */
public class BaseApplication extends LitePalApplication {
    List<Music> musicList;
    private static BaseApplication myApp;
    @Override
    public void onCreate() {
        super.onCreate();
        myApp = this;
        //数据库的初始化
        LitePal.initialize(this);
        //工具库的初始化
        Utils.init(this);

    }
    public static BaseApplication getMyApp() {
        return myApp;
    }


    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }
}
