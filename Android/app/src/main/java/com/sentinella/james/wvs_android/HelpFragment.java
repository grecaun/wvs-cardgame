/**
 * Copyright (c) 2017 James.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.sentinella.james.wvs_android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class HelpFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HelpFragment";

    @Override
    public void onClick(View view) {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            fm.beginTransaction().replace(R.id.fragContainer, new ConnectFragment()).commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.decrementCounter();
    }

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        Bundle bundle = getArguments();
        Log.d(TAG,String.format("Received layout id '%d' and button id '%d'.",bundle.getInt("layout_id"),bundle.getInt("button_id")));
        View rootView = aInflater.inflate(bundle.getInt("layout_id"), aContainer, false);
        rootView.findViewById(bundle.getInt("button_id")).setOnClickListener(this);
        MainActivity.incrementCounter();
        return rootView;
    }
}
