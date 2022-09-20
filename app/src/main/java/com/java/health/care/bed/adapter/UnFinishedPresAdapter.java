package com.java.health.care.bed.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.java.health.care.bed.R;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.util.TimeUtil;

import java.util.List;

/**
 * @author fsh
 * @date 2022/09/16 08:18
 * @Description
 */
public class UnFinishedPresAdapter extends RecyclerView.Adapter<UnFinishedPresAdapter.ViewHolder> {
    private Context context;
    private List<UnFinishedPres> unFinishedPresList;
    private UnFinishedPres unFinishedPres;
    private OnPresItemClickListener onPresItemClickListener;

    public UnFinishedPresAdapter(Context context,List<UnFinishedPres> unFinishedPresList){
        this.context = context;
        this.unFinishedPresList = unFinishedPresList;
    }
    @NonNull
    @Override
    public UnFinishedPresAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_unfinished, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UnFinishedPresAdapter.ViewHolder holder, int position) {
        unFinishedPres = unFinishedPresList.get(position);

        String type = unFinishedPres.getPreType();
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



        holder.time.setText(TimeUtil.second2Time((long) unFinishedPres.getDuration()));
        holder.date.setText(unFinishedPres.getExecTime());

        if(onPresItemClickListener!=null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    onPresItemClickListener.OnPresItemClick(holder.itemView,pos);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return unFinishedPresList.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        private TextView type,time,date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.item_type);
            time = itemView.findViewById(R.id.item_time);
            date = itemView.findViewById(R.id.item_date);
        }
    }

    public interface OnPresItemClickListener {
        void OnPresItemClick(View view,int position);
    }

    public void setPresItemClickListener(OnPresItemClickListener onPresItemClickListener){
        this.onPresItemClickListener = onPresItemClickListener;
    }
}
