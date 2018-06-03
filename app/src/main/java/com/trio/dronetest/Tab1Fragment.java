package com.trio.dronetest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trio.drone.R;

/**
 * Created by orhun on 6/3/2018.
 */

public class Tab1Fragment extends Fragment {
    private static final String TAB = "Tab1Fragment";
    Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(mToolbar != null){
            ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        }

        return view;
    }

}
