package com.java.health.care.bed.activity;

import androidx.appcompat.widget.AppCompatEditText;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/02 13:53
 * @Description
 */
public class InputPassWordActivity extends BaseActivity {
    @BindView(R.id.pass_edit)
    AppCompatEditText pass_edit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
    }


    @OnClick(R.id.back)
    public void back() {
       finish();
    }

    @OnClick(R.id.pass_confirm)
    public void confirm(){
        String pass= pass_edit.getText().toString().trim();
        if(pass.equals("1234567q")){
            goActivity(SettingActivity.class);
            finish();
        }else {
            showToast("密码输入错误，请重新输入");
        }
    }

}
