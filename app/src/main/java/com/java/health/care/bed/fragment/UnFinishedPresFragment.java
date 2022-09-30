package com.java.health.care.bed.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.activity.AssessActivity;
import com.java.health.care.bed.activity.DrillActivity;
import com.java.health.care.bed.activity.SoundWaveActivity;
import com.java.health.care.bed.activity.SweetActivity;
import com.java.health.care.bed.activity.VitalSignsActivity;
import com.java.health.care.bed.adapter.UnFinishedPresAdapter;
import com.java.health.care.bed.base.BaseFragment;
import com.java.health.care.bed.bean.Param;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;

/**
 * @author fsh
 * @date 2022/08/01 14:16
 * @Description 未完成处方列表
 */
public class UnFinishedPresFragment extends BaseFragment implements MainContract.View {
    private MainPresenter mainPresenter;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    List<UnFinishedPres> unFinishedPresList;
    private UnFinishedPresAdapter unFinishedPresAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_unfinished;
    }

    @Override
    protected void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void initData() {
        mainPresenter = new MainPresenter(getActivity(), this);
        int patientID = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        if(patientID!=0) {
            //调用接口
            mainPresenter.getPrescription(patientID);
        }

         getUnFinishedPres();


       /* //测试数据上传
        new Thread(new Runnable() {
            @Override
            public void run() {
                String zip = Environment.getExternalStorageDirectory().getPath() + "/HBed/data/"+  "20-20220930170334/ecgData.zip";
                //获取文件
                File file = FileUtils.getFileByPath(zip);

                //文件上传
                mainPresenter.uploadFile(file,"file_uploadReportLfs", "20", "19","BLOOD_PRESSURE");
            }
        }).start();*/


    }

    private void getUnFinishedPres(){
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int patientID = SPUtils.getInstance().getInt(SP.PATIENT_ID);
                        if(patientID!=0){
                            //调用接口
                            mainPresenter.getPrescription(patientID);
                            refreshLayout.finishRefresh();

                        }
                    }
                },0);
            }
        });

        //触发自动刷新
//        refreshLayout.autoRefresh();


    }

    @Override
    public void setCode(String code) {

    }

    @Override
    public void setMsg(String msg) {

    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {
        Prescription  pres = (Prescription) obj;
        if(pres!=null){
            unFinishedPresList = pres.getUnfinished();
            unFinishedPresAdapter = new UnFinishedPresAdapter(getActivity(),unFinishedPresList);
            recyclerView.setAdapter(unFinishedPresAdapter);
            unFinishedPresAdapter.setPresItemClickListener(new UnFinishedPresAdapter.OnPresItemClickListener() {
                @Override
                public void OnPresItemClick(View view, int position) {
                    showToast("positon:"+unFinishedPresList.get(position).getPreType()+"positon"+position);
                    //判断跳转到不同的页面
                    UnFinishedPres unFinishedPres = unFinishedPresList.get(position);
                    String type = unFinishedPres.getPreType();
                    //bundle传递对象unFinishedPres，对象里面包含list集合，对象需要实现Parcelable
                    Bundle bundle = new Bundle();
                    if(type.equals(SP.FANGXING)){ //芳香理疗
                        goActivity(SweetActivity.class);
                    }else if(type.equals(SP.SHENGBO)){ //声波理疗
                        goActivity(SoundWaveActivity.class);
                    }else if(type.equals(SP.SMTZ) ){ //生命体征
                        unFinishedPres.setFlag(1);
                        bundle.putParcelable(VitalSignsActivity.TAG,unFinishedPres);
                        goActivity(VitalSignsActivity.class,bundle);

                    }else if(type.equals(SP.WCXY)){//无创连续血压 依然是生命体征
                        unFinishedPres.setFlag(2);
                        bundle.putParcelable(VitalSignsActivity.TAG,unFinishedPres);
                        goActivity(VitalSignsActivity.class,bundle);

                    }else if(type.equals(SP.ZZSJ)){//自主神经评估
                        bundle.putParcelable(AssessActivity.TAG,unFinishedPres);
                        goActivity(AssessActivity.class,bundle);
                    }else if(type.equals(SP.XFXZ)){ //心肺谐振训练
                        bundle.putParcelable(DrillActivity.TAG,unFinishedPres);
                        goActivity(DrillActivity.class,bundle);
                    }
                }
            });
        }
    }

    @Override
    public void setData(Object obj) {


    }
}
