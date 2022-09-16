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
        holder.type.setText(unFinishedPres.getPreType());
        holder.time.setText(unFinishedPres.getDuration()+"秒");
        holder.date.setText(unFinishedPres.getExecTime());
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
