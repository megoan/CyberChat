package com.cyberx.shmuel.cyberx.controller.tab_fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cyberx.shmuel.cyberx.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyChatFragment extends Fragment {


    public MyChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_chat, container, false);
    }

}
