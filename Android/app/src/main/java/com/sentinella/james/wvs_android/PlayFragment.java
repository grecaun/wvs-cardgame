package com.sentinella.james.wvs_android;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by grecaun on 8/26/17.
 */

public class PlayFragment extends Fragment {
    private static final String TAG = "PlayFragment";

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        View rootView = aInflater.inflate(R.layout.fragment_play_vert, aContainer, false);
        return rootView;
    }
}
