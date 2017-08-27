package com.sentinella.james.wvs_android;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Copyright (c) 2017 James.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

public class ConnectFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {
    public static final String TAG = "ConnectFragment";

    private static MainAppCallback mainApp = null;

    @Override
    public void onClick(View view) {
        connect();
    }

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        View rootView = aInflater.inflate(R.layout.fragment_main, aContainer, false);
        rootView.findViewById(R.id.connectButton).setOnClickListener(this);
        rootView.findViewById(R.id.connectName).setOnKeyListener(this);
        return rootView;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
            connect();
            return true;
        }
        return false;
    }

    public static void setMainAppCallBack(MainAppCallback mainApp) {
        Log.d(TAG, "Setting main app.");
        ConnectFragment.mainApp = mainApp;
    }

    private void connect() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().findViewById(R.id.connectName).getWindowToken(), 0);
        String name = ((EditText)getView().findViewById(R.id.connectName)).getText().toString();
        Log.d(TAG,String.format("Name given is: '%s' - Starting wait fragment",name));
        getFragmentManager().beginTransaction().replace(R.id.fragContainer, new WaitFragment()).addToBackStack("main").commit();
    }
}
