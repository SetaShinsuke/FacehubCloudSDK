package com.azusasoft.facehubcloudsdk.views.advrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by SETA_WORK on 2016/9/29.
 */
public class AdapterEx extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private OnItemClickListenerEx mOnItemClickListenerEx;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListenerEx.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public int getIndexByPosition(int position){
        return position;
    }
    public int getPositionByIndex(int index) {
        return index;
    }

    public void setOnItemClickListenerEx(OnItemClickListenerEx onItemClickListenerEx) {
        mOnItemClickListenerEx = onItemClickListenerEx;
    }
}
