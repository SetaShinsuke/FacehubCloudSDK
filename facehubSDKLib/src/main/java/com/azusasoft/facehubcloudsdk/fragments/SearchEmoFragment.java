package com.azusasoft.facehubcloudsdk.fragments;

import android.view.LayoutInflater;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;
import com.azusasoft.facehubcloudsdk.views.advrecyclerview.RecyclerViewEx;

/**
 * Created by SETA on 2016/7/6.
 */
public class SearchEmoFragment extends BaseFragment {
    private RecyclerViewEx recyclerView;
    private View noResult;

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_result,null);
        recyclerView = (RecyclerViewEx) view.findViewById(R.id.result_recycler_view);
        noResult = view.findViewById(R.id.no_result);
        return view;
    }
}
