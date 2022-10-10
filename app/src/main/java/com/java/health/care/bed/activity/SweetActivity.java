package com.java.health.care.bed.activity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Param;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.util.TimeUtil;
import com.java.health.care.bed.widget.CountDownProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/08/25 16:58
 * @Description 芳香理疗界面  时长一样，只是针对不同的通道
 * 好眠复方精油/乐活复方精油/沁心复方精油/舒冒复方精油   对应通道1 2 3 4
 */
public class SweetActivity extends BaseActivity implements MainContract.View {
    @BindView(R.id.btn_start)
    AppCompatButton btn_start;
    private MainPresenter presenter;

    private int patientId;

    private int bunkId;

    private int regionId;

    private CountDownProgressBar cpb_countdown;
    public static final String TAG = SweetActivity.class.getSimpleName();
    private int preId;
    private String startTime;
    private String endTime;
    //通道
    private int one,two,three,four;
    //时长
    private int duration;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_sweet;
    }

    @Override
    protected void initView() {
        Button btn_start = findViewById(R.id.btn_start);
        cpb_countdown = (CountDownProgressBar) findViewById(R.id.cpb_countdown);
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);
        duration = unFinishedPres.getDuration();
        preId = unFinishedPres.getPreId();
        List<Param> paramList = unFinishedPres.getParam();
        int length = paramList.size();
        for(int i = 0;i<length;i++){
            Param param = paramList.get(i);
            if(param.getKey().equals("好眠复方精油")){
                one = 1;
            }else if(param.getKey().equals("乐活复方精油")){
                two = 2;
            }else if(param.getKey().equals("沁心复方精油")){
                three = 3;
            }else if(param.getKey().equals("舒冒复方精油")){
                four = 4;
            }
        }
        cpb_countdown.setDuration(duration);

       /* btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
    }

    @OnClick({R.id.btn_start})
    public void onClickBtn() {
        if (btn_start.getText().toString().equals("开始")) {
            //打开通道
            String dur = Integer.toHexString(duration/60);
            if(dur.length()==1){
                dur = "0"+dur;
            }
            if(one!=0){
                EventBus.getDefault().post(Constant.OPEN_SWEET_ONE_HALL +  dur
                        +Integer.toHexString(12+duration/60)+ Constant.OPEN_SWEET_LAST);
            }
            if(two!=0){
                EventBus.getDefault().post(Constant.OPEN_SWEET_TWO_HALL + dur
                        +Integer.toHexString(13+duration/60)+ Constant.OPEN_SWEET_LAST);
            }
            if(three!=0){
                EventBus.getDefault().post(Constant.OPEN_SWEET_THREE_HALL +  dur
                        +Integer.toHexString(14+duration/60)+ Constant.OPEN_SWEET_LAST);
            }
            if(four!=0){
                EventBus.getDefault().post(Constant.OPEN_SWEET_FOUR_HALL + dur
                        +Integer.toHexString(15+duration/60)+ Constant.OPEN_SWEET_LAST);
            }

            //记录开始时间
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTime = sdf.format(d);
            cpb_countdown.setDuration(duration*1000, new CountDownProgressBar.OnFinishListener() {
                @Override
                public void onFinish() {
                    //记录结束时间
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    endTime = sdf.format(d);
                    //上传接口
                    /**
                     *   *  "duration": 100,
                     *      * 	"endTime": "2022-12-22 14:30:50",
                     *      *  "preId": 3,
                     *      * 	"preType": "SONIC_WAVE",
                     *      * 	"startTime": "2022-12-22 14:25:50"
                     */
                    presenter.upExec(preId,"FRAGRANCE",duration,startTime,endTime);

                }
            });
        }else if(btn_start.getText().toString().equals("中断")){
            showDialog();
        }
    }
    //按键返回

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showDialog();
        }
        return false;

    }

    //返回箭头
    @OnClick(R.id.back)
    public void back() {
        //todo
        showDialog();
    }


    /**
     * 中断逻辑，记录结束时间，上传接口
     */
    private void overAndUpdate(){
        //记录结束时间
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endTime = sdf.format(d);
        presenter.upExec(preId,"FRAGRANCE",duration,startTime,endTime);
    }

    //弹窗提示
    private void showDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("你确定要中断此次理疗吗？")
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"MaterialDialog确定");


                        //调用接口，上传文件
                        overAndUpdate();

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

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);

        presenter = new MainPresenter(this, this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if(event instanceof Integer){
            int num = (int) event;
            patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
            bunkId = SPUtils.getInstance().getInt(SP.BUNK_ID);
            regionId = SPUtils.getInstance().getInt(SP.REGION_ID);
            presenter.sendMessage(regionId,bunkId,num,patientId);
        }
    }
    @Override
    public void setCode(String code) {
        if(code.equals("200")){
            Toast.makeText(SweetActivity.this, "理疗完成，报告上传成功", Toast.LENGTH_SHORT).show();
            goActivity(PrescriptionActivity.class);
            finish();
        }
    }

    @Override
    public void setMsg(String msg) {

    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {

    }

    @Override
    public void setData(Object obj) {

    }
}
