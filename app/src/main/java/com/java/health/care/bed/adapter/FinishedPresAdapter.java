package com.java.health.care.bed.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.java.health.care.bed.R;
import com.java.health.care.bed.bean.FinishedPres;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.util.TimeUtil;

import java.util.List;

/**
 * @author fsh
 * @date 2022/09/16 08:18
 * @Description
 */
public class FinishedPresAdapter extends RecyclerView.Adapter<FinishedPresAdapter.ViewHolder> {
    private Context context;
    private List<FinishedPres> finishedPresList;
    private FinishedPres finishedPres;

    public FinishedPresAdapter(Context context,List<FinishedPres> finishedPresList){
        this.context = context;
        this.finishedPresList = finishedPresList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_finished, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        finishedPres = finishedPresList.get(position);
        String type = finishedPres.getPreType();
        if(type.equals(SP.FANGXING)){
            holder.type.setText("芳香理疗");
        }else if(type.equals(SP.SHENGBO)){
            holder.type.setText("声波理疗");
        }else if(type.equals(SP.SMTZ) || type.equals(SP.WCXY)){
            holder.type.setText("生命体征监测");
        }else if(type.equals(SP.ZZSJ)){
            holder.type.setText("自主神经评估");
        }else if(type.equals(SP.XFXZ)){
            holder.type.setText("心肺谐振训练");
        }
        holder.time.setText(TimeUtil.second2Time((long) finishedPres.getDuration()));
        holder.date.setText(finishedPres.getExecTime());
    }

    @Override
    public int getItemCount() {
        return finishedPresList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView type,time,date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.item_type);
            time = itemView.findViewById(R.id.item_time);
            date = itemView.findViewById(R.id.item_date);
        }
    }
}
