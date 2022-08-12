package com.java.health.care.bed.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * Description: Base Activity
 * @author MVP基类
 * https://github.com/fly803/BaseProject
 * https://github.com/JsonChao/Awesome-WanAndroid.git
 */
public abstract class BaseMVPActivity<P extends BasePresenter> extends AppCompatActivity implements IBaseView{

    protected P mPresenter;

    /**
     * 使用ButterKnife
     */
    protected Unbinder unbinder;

    /**
     * 获取布局id
     * @return 当前需要加载的布局
     */
    protected abstract int getContentViewId();

    /**
     * 初始化
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * 创建Presenter
     * @return 返回当前Presenter
     */
    protected abstract P createPresenter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        unbinder = ButterKnife.bind(this);
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        init(savedInstanceState);

    }

    @Override
    public void onLoadFailed() {

    }

    @Override
    public void onLoadSuccess() {

    }

    @Override
    public void onLoading() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        mPresenter.detachView();
    }
}