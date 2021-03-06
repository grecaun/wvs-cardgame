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


public class ServerSettingsFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {
    private static final String TAG = "ConSettingsFragment";

    private SharedPreferences prefs;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.serverSettingsCancel:
                Log.d(TAG,"Cancel clicked.");
                close();
                break;
            case R.id.serverSettingsSubmit:
                Log.d(TAG,"Submit clicked.");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(MainActivity.SERVPORT,((EditText)getView().findViewById(R.id.serverSettingsPort)).getText().toString());
                edit.putString(MainActivity.LOBBYTO,((EditText)getView().findViewById(R.id.serverSettingsLobbyTO)).getText().toString());
                edit.putString(MainActivity.PLAYTO,((EditText)getView().findViewById(R.id.serverSettingsPlayTO)).getText().toString());
                edit.putString(MainActivity.MINPLAYERS,((EditText)getView().findViewById(R.id.serverSettingsMinPlayers)).getText().toString());
                edit.putString(MainActivity.MAXCLIENTS,((EditText)getView().findViewById(R.id.serverSettingsMaxClients)).getText().toString());
                edit.putString(MainActivity.MAXSTRIKES,((EditText)getView().findViewById(R.id.serverSettingsMaxStrikes)).getText().toString());
                edit.apply();
                close();
                break;
            default:
                Log.d(TAG,"Unknown clicked.");
                close();
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
        Log.d(TAG, "Setting up");
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        View rootView = aInflater.inflate(R.layout.fragment_serversettings, aContainer, false);
        rootView.findViewById(R.id.serverSettingsSubmit).setOnClickListener(this);
        rootView.findViewById(R.id.serverSettingsCancel).setOnClickListener(this);
        rootView.findViewById(R.id.serverSettingsPort).setOnKeyListener(this);
        rootView.findViewById(R.id.serverSettingsLobbyTO).setOnKeyListener(this);
        rootView.findViewById(R.id.serverSettingsPlayTO).setOnKeyListener(this);
        rootView.findViewById(R.id.serverSettingsMinPlayers).setOnKeyListener(this);
        rootView.findViewById(R.id.serverSettingsMaxClients).setOnKeyListener(this);
        rootView.findViewById(R.id.serverSettingsMaxStrikes).setOnKeyListener(this);
        String tmp = prefs.getString(MainActivity.SERVPORT,"");
        Log.d(TAG,String.format("Preferences has port as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsPort)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.LOBBYTO,"");
        Log.d(TAG,String.format("Preferences has lobby timeout as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsLobbyTO)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.PLAYTO,"");
        Log.d(TAG,String.format("Preferences has play timeout as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsPlayTO)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.MINPLAYERS,"");
        Log.d(TAG,String.format("Preferences has minimum players as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsMinPlayers)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.MAXCLIENTS,"");
        Log.d(TAG,String.format("Preferences has max clients as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsMaxClients)).setText(tmp);
        }
        tmp = prefs.getString(MainActivity.MAXSTRIKES,"");
        Log.d(TAG,String.format("Preferences has max strikes as '%s'.",tmp));
        if (!tmp.equalsIgnoreCase("")) {
            ((EditText) rootView.findViewById(R.id.serverSettingsMaxStrikes)).setText(tmp);
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
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
            Log.d(TAG, "Enter hit.");
            return true;
        }
        return false;
    }
}
