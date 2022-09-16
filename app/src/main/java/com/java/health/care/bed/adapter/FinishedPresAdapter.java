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
        holder.type.setText(finishedPres.getPreType());
        holder.time.setText(finishedPres.getDuration()+"秒");
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
