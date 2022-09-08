package com.java.health.care.test;

import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.java.health.care.bed.R;

import java.util.ArrayList;


public class PickActivity extends AppCompatActivity {
    private TextView tvAddress;
    private OptionsPickerView pvOptions;
    private LevelsListDate levelsListDate;
    private ArrayList<JsonBean> jsonBeans;
    private ArrayList<ArrayList<String>> arrayLists;
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                showHyPickerView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        tvAddress = findViewById(R.id.tvAddress);
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pvOptions != null) {
                    pvOptions.show();
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    levelsListDate = new LevelsListDate(PickActivity.this);
                    jsonBeans = levelsListDate.initJsonData("citys_data.json");
                    arrayLists = levelsListDate.initJsonData1("citys_data.json");
                    for (int i=0;i<arrayLists.size();i++){
                        Log.d("arrayList===",arrayLists.get(i).toString());
                    }
//                    arrayLists1 = levelsListDate.initJsonData2("citys_data.json");
                    handler1.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 初始化城市选择器
     */
    private void showHyPickerView() {
        //条件选择器
        pvOptions = new OptionsPickerBuilder(PickActivity.this, new com.bigkoo.pickerview.listener.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                try {
//                    tvAddress.setText(jsonBeans.get(options1).getCity().get(options2).getRes().get(options3).getREGION_NAME());
                    tvAddress.setText(jsonBeans.get(options1).getCity().get(options2).getRes().get(options3).getREGION_NAME());
                } catch (Exception e) {
                    // cityId = jsonBeans.get(options1).getCity().get(options2).getID() + "";
                    tvAddress.setText(jsonBeans.get(options1).getCity().get(options2).getREGION_NAME());
                }
            }
        })
                .setTitleText("请选择科室和病区")
                .setLineSpacingMultiplier(3.0f)
                        .build();
        pvOptions.setPicker(jsonBeans, arrayLists);
    }
}
