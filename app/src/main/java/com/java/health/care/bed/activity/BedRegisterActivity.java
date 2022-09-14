package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.text.InputType;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Bunk;
import com.java.health.care.bed.bean.Dept;
import com.java.health.care.bed.bean.Region;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/09/05 14:02
 * @Description
 */
public class BedRegisterActivity extends BaseActivity implements MainContract.View {
    private static final String TAG = BedRegisterActivity.class.getSimpleName();
    private MainPresenter mainPresenter;
    private OptionsPickerView pvOptions;

    private List<Dept> depts = new ArrayList<>();
    private List<List<String>> lists = new ArrayList<>();

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
        String dept = SPUtils.getInstance().getString(SP.DEPT_NUM);
        String region = SPUtils.getInstance().getString(SP.REGION_NUM);
        String bed = SPUtils.getInstance().getString(SP.BUNK_NUM);
        if(!dept.isEmpty() && !region.isEmpty()){
            bed_register_choice.setText(dept+"    "+region);
        }else {
            bed_register_choice.setText("");
        }

        if (!bed.isEmpty()){
            bed_register_num.setText(bed);
        }else {
            bed_register_num.setText("");
        }
    }

    @Override
    protected void initData() {
        mainPresenter = new MainPresenter(this, this);
        getDeptRegion();
    }

    /**
     * 初始化选择器
     */
    private void showHyPickerView() {
        //条件选择器
        pvOptions = new OptionsPickerBuilder(this, (options1, options2, options3, v) -> {
                bed_register_choice.setText(depts.get(options1).getName()+"    "+
                        depts.get(options1).getRegions().get(options2).getName());

                SPUtils.getInstance().put(SP.DEPT_NUM,depts.get(options1).getName());
                SPUtils.getInstance().put(SP.REGION_NUM, depts.get(options1).getRegions().get(options2).getName());

                SPUtils.getInstance().put(SP.DEPT_ID,depts.get(options1).getId());
                SPUtils.getInstance().put(SP.REGION_ID,depts.get(options1).getRegions().get(options2).getId());
        })
                .setTitleText("请选择科室和病区")
                .setLineSpacingMultiplier(3.0f)
                .setSubmitColor(getResources().getColor(R.color.btnColor))
                .setCancelColor(getResources().getColor(R.color.btnColor))
                .build();
        pvOptions.setPicker(depts, lists);
        pvOptions.show();
    }

    //点击床位编号，弹窗
    @SuppressLint("ResourceType")
    @OnClick(R.id.bed_register_rl)
    public void setBed() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("请输入床位编号")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("如：20", null, (dialog1, input) -> {
                    bed_register_num.setText(input);
                    SPUtils.getInstance().put(SP.BUNK_NUM, input.toString());

                })
                .positiveText("确定")
                .build();


        if (dialog.getTitleView() != null) {
            dialog.getTitleView().setTextSize(25);
        }
        if (dialog.getContentView() != null) {
            dialog.getInputEditText().setTextSize(25);
        }
        if (dialog.getActionButton(DialogAction.POSITIVE) != null) {
            dialog.getActionButton(DialogAction.POSITIVE).setTextSize(25);
        }

        dialog.show();
    }

    @OnClick(R.id.bed_register)
    public void register(){
        //调用接口
        int deptId = SPUtils.getInstance().getInt(SP.DEPT_ID);
        int regionId = SPUtils.getInstance().getInt(SP.REGION_ID);
        String bunkNum = SPUtils.getInstance().getString(SP.BUNK_NUM);
        mainPresenter.saveBedInfo(deptId,regionId,bunkNum);
    }


    //请求接口，选择科室和病区
    private void getDeptRegion() {
        mainPresenter.getDeptRegion();
    }

    @OnClick(R.id.bed_register_choice_rl)
    public void bedChoice() {
        if(depts==null || depts.size()==0 || lists==null || lists.size()==0){
            showToast("请检查服务器地址或者科室病区数据为空");
        }else {
            showHyPickerView();
        }
//        showHyPickerView();
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

    @Override
    public void setCode(String code) {
        Log.d(TAG, "code:" + code);

    }

    @Override
    public void setMsg(String msg) {
        Log.d(TAG, "msg:" + msg);

    }

    @Override
    public void setInfo(String msg) {
        if(msg.equals("添加成功")){
            showToast(msg);
            goActivity(SettingActivity.class);
            finish();
        }else {
            showToast(msg);
        }
    }

    @Override
    public void setObj(Object obj) {

        depts = (List<Dept>) obj;

        initJsonData(depts);

    }

    @Override
    public void setData(Object obj) {
        Bunk bunk = (Bunk) obj;
        if(bunk!=null){
            SPUtils.getInstance().put(SP.BUNK_ID, bunk.getId());
            SPUtils.getInstance().put(SP.BUNK_NUM, bunk.getBunkNo());
        }
    }


    public List<List<String>> initJsonData(List<Dept> jsonBean) {

        for (int i = 0; i < jsonBean.size(); i++) {
            List<String> regList = new ArrayList<>();
            List<Region> regionList = jsonBean.get(i).getRegions();

            for (int c = 0; c < regionList.size(); c++) {
                String regName = regionList.get(c).getName();
                Log.d(TAG,"dept=="+regName);
                regList.add(regName);
            }

            lists.add(regList);
        }
        return lists;
    }
}
