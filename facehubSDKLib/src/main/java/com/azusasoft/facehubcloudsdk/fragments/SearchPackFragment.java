package com.azusasoft.facehubcloudsdk.fragments;

import android.view.LayoutInflater;
import android.view.View;

import com.azusasoft.facehubcloudsdk.R;

/**
 * Created by SETA on 2016/7/6.
 */
public class SearchPackFragment extends BaseFragment {

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_result,null);
        view.setBackgroundColor(0);
        return view;
    }
}
