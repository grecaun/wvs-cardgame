/**
 * Copyright (c) 2017 James Sentinella.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.sentinella.james.wvs_android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class ConSettingsFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {
    private static final String TAG = "ConSettingsFragment";

    private SharedPreferences prefs;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.conSettingsCancel:
                Log.d(TAG,"Cancel clicked.");
                close();
                break;
            case R.id.conSettingsSubmit:
                Log.d(TAG,"Submit clicked.");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(MainActivity.CONPORT,((EditText)getView().findViewById(R.id.conSettingsPort)).getText().toString());
                edit.putString(MainActivity.CONADDR,((EditText)getView().findViewById(R.id.conSettingsIP)).getText().toString());
                edit.apply();
                close();
                break;
            default:
                Log.d(TAG,"Unknown clicked.");
                close();
        }
    }

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        Log.d(TAG, "Setting up");
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        View rootView = aInflater.inflate(R.layout.fragment_connectionsettings, aContainer, false);
        rootView.findViewById(R.id.conSettingsSubmit).setOnClickListener(this);
        rootView.findViewById(R.id.conSettingsCancel).setOnClickListener(this);
        rootView.findViewById(R.id.conSettingsPort).setOnKeyListener(this);
        rootView.findViewById(R.id.conSettingsIP).setOnKeyListener(this);
        String tmp = prefs.getString(MainActivity.CONPORT,"");
        Log.d(TAG,String.format("Preferences has port as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.conSettingsPort)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.CONADDR,"");
        Log.d(TAG,String.format("Preferences has ip as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.conSettingsIP)).setText(tmp);
        }
        MainActivity.incrementCounter();
        return rootView;
    }

    private void close() {
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
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
            Log.d(TAG, "Enter hit.");
            return true;
        }
        return false;
    }
}
