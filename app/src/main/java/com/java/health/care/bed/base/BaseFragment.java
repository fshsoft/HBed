package com.java.health.care.bed.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Administrator
 */
public abstract class BaseFragment extends Fragment {

    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(getLayoutId(),container,false);
        ButterKnife.bind(this,root);
        initView();
        initData();
        return root;
    }

    protected abstract int getLayoutId();
    protected abstract void initView();

    protected abstract void initData();

    public void goActivity(Class<?> clazz) {
        goActivity(clazz, null);
    }

    public void goActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public static void goActivity(Class<?> clazz, Bundle bundle, Context ctx) {
        Intent intent = new Intent(ctx, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        ctx.startActivity(intent);
    }

    public void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context ctx, String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(Context ctx, String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=unbinder){
            unbinder.unbind();
        }
    }
}
