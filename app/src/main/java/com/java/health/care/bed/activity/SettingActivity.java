package com.java.health.care.bed.activity;

import android.Manifest;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.constant.Api;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.util.LinDownloadAPk;
import com.java.health.care.bed.util.VersionUtil;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;

import java.util.ArrayList;
import java.util.List;

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

    @BindView(R.id.set_wc_press_num)
    AppCompatTextView set_wc_press_num;

    @BindView(R.id.set_bunk_num)
    AppCompatTextView set_bunk_num;

    @BindView(R.id.set_version_update_num)
    AppCompatTextView set_version_update_num;

    private String server_ip;

    private String apkName;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void initView() {
        presenter = new MainPresenter(this, this);
        set_version_update_num.setText("V "+VersionUtil.getAppVersionName(this));
        checkPermissions();
        BleManager.getInstance().disconnectAllDevice();
    }

    @Override
    protected void initData() {
        server_ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);

        String dept = SPUtils.getInstance().getString(SP.DEPT_NUM);
        String region = SPUtils.getInstance().getString(SP.REGION_NUM);
        String bed = SPUtils.getInstance().getString(SP.BUNK_NUM);
        String press = SPUtils.getInstance().getString(SP.WCPRESSVALUE);

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

        if(!press.isEmpty()){
            set_wc_press_num.setText(press);
        }else {
            set_wc_press_num.setText("");
        }


    }

    /**
     * ??????token
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

        apkName = (String) obj;
        //???fileName??????????????????versionCode????????????????????????????????????????????????????????????
        SPUtils.getInstance().put(SP.APK_NAME,apkName);
        showDialog();

    }
    //????????????
    private void showDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("??????????????????????????????")
                .positiveText("??????")
                .negativeText("??????")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    //????????????????????????
//                        presenter.download(apkName);
                        String ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);
                        String url ="http://"+ip+":1234";
                        LinDownloadAPk.downApk(SettingActivity.this, url+Api.download);
//

                    }
                })
                .build();


        if (dialog.getTitleView() != null) {
            dialog.getTitleView().setTextSize(25);
        }

        if (dialog.getActionButton(DialogAction.POSITIVE) != null) {
            dialog.getActionButton(DialogAction.POSITIVE).setTextSize(25);
        }

        if(dialog.getActionButton(DialogAction.NEGATIVE)!=null){
            dialog.getActionButton(DialogAction.NEGATIVE).setTextSize(25);
        }

        dialog.show();
    }

    //??????????????????
    @OnClick(R.id.set_ble_set_rl)
    public void setBle(){
        goActivity(BleSettingActivity.class);
    }

    //?????????????????????
    @OnClick(R.id.set_wc_press_rl)
    public void setPress(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("??????120/80", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        set_wc_press_num.setText(input);
                        SPUtils.getInstance().put(SP.WCPRESSVALUE,input.toString());

                    }
                })
                .positiveText("??????")
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
    //??????????????????????????????
    @OnClick(R.id.set_server_rl)
    public void setServer(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("??????192.168.1.100", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        set_server_address.setText(input);
                        SPUtils.getInstance().put(SP.IP_SERVER_ADDRESS,input.toString());
                        String token = SPUtils.getInstance().getString(SP.TOKEN);
//                        if(token.isEmpty()){
//                            getToken();
//                        }
                        getToken();
                    }
                })
                .positiveText("??????")
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

    /**
     * ??????????????????
     * ??????????????????????????????????????????
     * apk?????????????????????+versionName+versionCode+????????????+.apk
     * ???????????????versionCode????????????
     */
    @OnClick(R.id.set_version_update_rl)
    public void onClickUpdateVersion(){
        //?????????????????????versionCode????????????????????????????????????
        presenter.compareVersionApk();


    }



    //????????????????????????????????????
    @OnClick(R.id.set_bed_rl)
    public void setBed(){
        //??????????????????????????????ip
        if(set_server_address.getText().toString().trim().isEmpty()){
            showToast("???????????????????????????");
        }else {
            goActivity(BedRegisterActivity.class);
        }

    }
    //??????????????????
    @OnClick(R.id.set_version_update_rl)
    public void updateVersion(){

    }

    //??????????????????
    @OnClick(R.id.set_about_us_rl)
    public void aboutUs(){

    }

    @OnClick(R.id.back)
    public void back(){
//        goActivity(PrescriptionActivity.class);
        goActivity(WelcomeActivity.class);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            goActivity(WelcomeActivity.class);
            finish();
        }
        return false;

    }

    /**
     * ????????????
     */

    private void checkPermissions() {

        List requestList = new ArrayList();
        //?????????????????????????????????
        requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);

        if(!requestList.isEmpty()){
            PermissionX.init(this)
                    .permissions(requestList)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            scope.showRequestReasonDialog(deniedList,"?????????????????????????????????????????????","??????","??????");
                        }
                    })
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                            if (allGranted) {
                            } else {
                                Toast.makeText(SettingActivity.this, "????????????????????????"+deniedList, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }


}
