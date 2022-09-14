package com.java.health.care.bed.activity;

import android.text.InputType;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.util.VersionUtil;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/02 13:55
 * @Description
 *
 * dialog https://blog.csdn.net/qq_39652726/article/details/81262061
 */
public class SettingActivity extends BaseActivity implements MainContract.View {

    private MainPresenter presenter;

    @BindView(R.id.set_server_address)
    AppCompatTextView set_server_address;

    @BindView(R.id.set_bunk_num)
    AppCompatTextView set_bunk_num;

    @BindView(R.id.set_version_update_num)
    AppCompatTextView set_version_update_num;

    private String server_ip;



    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void initView() {
        presenter = new MainPresenter(this, this);
        set_version_update_num.setText("V "+VersionUtil.getAppVersionName(this));

    }

    @Override
    protected void initData() {
        server_ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);

        String dept = SPUtils.getInstance().getString(SP.DEPT_NUM);
        String region = SPUtils.getInstance().getString(SP.REGION_NUM);
        String bed = SPUtils.getInstance().getString(SP.BUNK_NUM);

        if(!server_ip.isEmpty()){
            set_server_address.setText(server_ip);
        }else {
            set_server_address.setText("");
        }

        if(!dept.isEmpty() && !region.isEmpty()&&!bed.isEmpty()){
            set_bunk_num.setText(dept+"    "+region+"    "+bed);
        }else {
            set_bunk_num.setText("");
        }


    }

    /**
     * 获取token
     */
    private void getToken(){
        presenter.getToken("client_credentials","cas-app","1qs4rfGhu85");
    }



    @Override
    public void setCode(String code) {
        Log.d("setting====","code:"+code);
    }

    @Override
    public void setMsg(String msg) {
        Log.d("setting====","msg:"+msg);
    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {
        Token token = (Token) obj;
        if(token!=null){
            Log.d("setting====","token:"+token.getTokenType()+"===="+token.getValue());
            SPUtils.getInstance().put(SP.TOKEN,token.getTokenType()+" "+token.getValue());
        }

    }

    @Override
    public void setData(Object obj) {

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
                        set_server_address.setText(input);
                        SPUtils.getInstance().put(SP.IP_SERVER_ADDRESS,input.toString());
                        String token = SPUtils.getInstance().getString(SP.TOKEN);
                        if(token.isEmpty()){
                            getToken();
                        }
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

   /* //点击服务器地址，弹窗
    @OnClick(R.id.set_server_port_rl)
    public void setPort(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("请输入端口号")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("如：8080", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        set_server_port_address.setText(input);
                        SPUtils.getInstance().put(SP.IP_SERVER_PORT,input.toString());
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

    }*/

    //点击床位编号，跳转到注册
    @OnClick(R.id.set_bed_rl)
    public void setBed(){
        //判断是否设置了服务器ip
        if(set_server_address.getText().toString().trim().isEmpty()){
            showToast("请先设置服务器地址");
        }else {
            goActivity(BedRegisterActivity.class);
        }

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
