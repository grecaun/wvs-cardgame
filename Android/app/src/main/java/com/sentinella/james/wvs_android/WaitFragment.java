/**
 * Copyright (c) 2017 James.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.sentinella.james.wvs_android;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class WaitFragment extends Fragment {
    private static final String TAG = "WaitFragment";

    private loader loadTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, ViewGroup aContainer, Bundle aSavedInstanceState) {
        View rootView = aInflater.inflate(R.layout.fragment_wait, aContainer, false);
        if (loadTask == null) {
            loadTask = new loader(this);
            loadTask.execute();
        }
        return rootView;
    }

    public Activity getCurrentActivity() {
        return getActivity();
    }

    private static class loader extends AsyncTask<Void, Void, Void> {
        private WaitFragment waitFragment;
        private Resources resources;
        private Context   context;

        public loader(WaitFragment frag) {
            this.waitFragment = frag;
            resources = frag.getActivity().getResources();
            context = frag.getActivity().getApplicationContext();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG,"Sleeping");
            PlayView.preInit(resources,context);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Awake");
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            Log.d(TAG,"Connected.  Loading play view now.");
            FragmentManager fm = waitFragment.getActivity().getFragmentManager();
            if (fm != null && fm.getBackStackEntryCount() > 0) fm.popBackStack();
            fm.beginTransaction().replace(R.id.fragContainer, new PlayFragment()).addToBackStack("main").commit();
        }
    }
}
