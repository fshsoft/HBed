package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.text.InputType;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.appcompat.widget.AppCompatTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/09/05 14:02
 * @Description
 */
public class BedRegisterActivity extends BaseActivity {
    @BindView(R.id.bed_register_num)
    AppCompatTextView bed_register_num;
    @BindView(R.id.bed_register_choice)
    AppCompatTextView bed_register_choice;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_bed_register;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    //点击床位编号，弹窗
    @SuppressLint("ResourceType")
    @OnClick(R.id.bed_register_rl)
    public void setBed(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("请输入床位编号")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("如：20", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
//                        ToastUtils.showShort(input);
                        bed_register_num.setText(input);
                        SPUtils.getInstance().put(Constant.BED_NUM,input.toString());
                    }
                })
                .positiveText("确定")
                .build();


        if (dialog.getTitleView() != null){
            dialog.getTitleView().setTextSize(25);
        }
        if (dialog.getContentView() != null){
            dialog.getInputEditText().setTextSize(25);
        }
        if (dialog.getActionButton(DialogAction.POSITIVE) != null){
            dialog.getActionButton(DialogAction.POSITIVE).setTextSize(25);
        }

        dialog.show();
    }

    //请求接口，选择科室和病区
    @OnClick(R.id.bed_register_choice_rl)
    public void bedChoice(){

    }
    @OnClick(R.id.back)
    public void back(){
        finish();
    }

}
