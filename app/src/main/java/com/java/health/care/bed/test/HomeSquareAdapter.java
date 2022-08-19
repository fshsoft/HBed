package com.java.health.care.bed.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.java.health.care.bed.R;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeSquareAdapter extends RecyclerView.Adapter<HomeSquareAdapter.HomeSquareListHolder> {

    private Context mContext;

    private List<Article> mHomeSquareList = new ArrayList<>();

    private ItemOnClickListener itemOnClickListener;

    public void setItemOnClickListener(ItemOnClickListener itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    public HomeSquareAdapter(Context context, List<Article> articleList) {
        mContext = context;
        mHomeSquareList.addAll(articleList);
    }

    public void setHomeSquareList(List<Article> articleList) {
        mHomeSquareList.clear();
        mHomeSquareList.addAll(articleList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeSquareListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.article_item, parent, false);
        return new HomeSquareListHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull HomeSquareListHolder holder, @SuppressLint("RecyclerView") int position) {
        if (mHomeSquareList != null) {
            Article bean = mHomeSquareList.get(position);
            holder.mHomeSquareTitle.setText(Html.fromHtml(bean.title, Html.FROM_HTML_MODE_COMPACT));

            holder.mHomeSquareAuthor.setText(String.format(mContext.getResources().getString(R.string.article_author), bean.shareUser));
            holder.mHomeSquareDate.setText(bean.niceDate);
            String category = String.format(mContext.getResources().getString(R.string.article_category),
                    bean.superChapterName, bean.chapterName);
            holder.mHomeSquareType.setText(Html.fromHtml(category, Html.FROM_HTML_MODE_COMPACT));

            if (bean.isFresh) {
                holder.mNewView.setVisibility(View.VISIBLE);
            } else {
                holder.mNewView.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(itemOnClickListener!=null){
                        itemOnClickListener.onItemClick(view,position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mHomeSquareList != null ? mHomeSquareList.size() : 0;
    }

    class HomeSquareListHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_home_author)
        TextView mHomeSquareAuthor;
        @BindView(R.id.item_home_content)
        TextView mHomeSquareTitle;
        @BindView(R.id.item_article_type)
        TextView mHomeSquareType;
        @BindView(R.id.item_home_date)
        TextView mHomeSquareDate;
        @BindView(R.id.item_list_collect)
        ImageView mCollectView;
        @BindView(R.id.item_home_new)
        TextView mNewView;

        public HomeSquareListHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public interface ItemOnClickListener {

        void onItemClick(View view,int position);

    }
}
