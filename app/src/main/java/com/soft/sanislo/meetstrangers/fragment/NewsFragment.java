package com.soft.sanislo.meetstrangers.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.soft.sanislo.meetstrangers.R;

import butterknife.ButterKnife;

/**
 * Created by root on 18.12.16.
 */

public class NewsFragment extends Fragment {
    public static final String TAG = NewsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_news, container, false);
        ButterKnife.bind(this, view);
        Toast.makeText(getActivity(), "news fragment", Toast.LENGTH_SHORT).show();
        return view;
    }

    public static NewsFragment newInstance() {
        Bundle args = new Bundle();
        NewsFragment fragment = new NewsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
