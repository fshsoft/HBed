package com.java.health.care.bed.activity;

import android.text.InputType;

import androidx.appcompat.widget.AppCompatTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/02 13:55
 * @Description
 *
 * dialog https://blog.csdn.net/qq_39652726/article/details/81262061
 */
public class SettingActivity extends BaseActivity {
    @BindView(R.id.set_server_address)
    AppCompatTextView set_server_address;

    @BindView(R.id.set_bed_num)
    AppCompatTextView set_bed_num;

    private String server_ip;
    private String area_num;
    private String bed_num;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        server_ip = SPUtils.getInstance().getString(Constant.SERVER_IP);
        area_num = SPUtils.getInstance().getString(Constant.AREA_NUM);
        bed_num = SPUtils.getInstance().getString(Constant.BED_NUM);
        if(server_ip!=null){
            set_server_address.setText(server_ip);
        }else {
            set_server_address.setText("");
        }

        if(bed_num!=null){
            set_bed_num.setText(bed_num);
        }else {
            set_bed_num.setText("");
        }
    }

    //点击蓝牙设置
    @OnClick(R.id.set_ble_set_rl)
    public void setBle(){
        goActivity(BleSettingActivity.class);
    }

    //点击服务器地址，弹窗
    @OnClick(R.id.set_server_rl)
    public void setServer(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("请输入服务器地址")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("如：192.168.1.100", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
//                        ToastUtils.showShort(input);
                        set_server_address.setText(input);
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

    //点击床位编号，弹窗
    @OnClick(R.id.set_bed_rl)
    public void setBed(){
        goActivity(BedRegisterActivity.class);
    }
    //点击版本更新
    @OnClick(R.id.set_version_update_rl)
    public void updateVersion(){

    }

    //点击关于我们
    @OnClick(R.id.set_about_us_rl)
    public void aboutUs(){

    }

    @OnClick(R.id.back)
    public void back(){
        finish();
    }


}
