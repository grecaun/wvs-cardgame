package com.sentinella.james.wvs_android;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.sentinella.james.Client;

public class MainActivity extends AppCompatActivity implements MainAppCallback {
    public static final String TAG = "MainActivity";
    public static final String CONPORT     = "CONPORT";
    public static final String CONADDR     = "CONADDRESS";
    public static final String SERVPORT    = "SERVERPORT";
    public static final String LOBBYTO     = "LOBBYTIMEOUT";
    public static final String PLAYTO      = "PLAYTIMEOUT";
    public static final String MINPLAYERS  = "MINPLAYERS";
    public static final String MAXCLIENTS  = "MAXCLIENTS";
    public static final String MAXSTRIKES  = "MAXSTRIKES";

    private SharedPreferences prefs;

    private static int settings_counter = 0;

    private Client client;
    private Thread clientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate - setting fragment");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragContainer, new ConnectFragment()).commit();
            ConnectFragment.setMainAppCallBack(this);
        }
        Log.d(TAG, "onCreate - setting preferences");
        prefs = getPreferences(Context.MODE_PRIVATE);
        if (prefs.getString(CONPORT,"").equalsIgnoreCase("")) {
            prefs.edit().putString(CONPORT, "36789").apply();
        }
        if (prefs.getString(CONADDR,"").equalsIgnoreCase("")) {
            prefs.edit().putString(CONADDR,"localhost").apply();
        }
        if (prefs.getString(SERVPORT,"").equalsIgnoreCase("")) {
            prefs.edit().putString(SERVPORT, "36789").apply();
        }
        if (prefs.getString(LOBBYTO,"").equalsIgnoreCase("")) {
            prefs.edit().putString(LOBBYTO, "15").apply();
        }
        if (prefs.getString(PLAYTO,"").equalsIgnoreCase("")) {
            prefs.edit().putString(PLAYTO, "10").apply();
        }
        if (prefs.getString(MINPLAYERS,"").equalsIgnoreCase("")) {
            prefs.edit().putString(MINPLAYERS, "3").apply();
        }
        if (prefs.getString(MAXCLIENTS,"").equalsIgnoreCase("")) {
            prefs.edit().putString(MAXCLIENTS, "90").apply();
        }
        if (prefs.getString(MAXSTRIKES,"").equalsIgnoreCase("")) {
            prefs.edit().putString(MAXSTRIKES, "5").apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.conSettings:
                Log.d(TAG, "Connection Settings");
                closeKeyboard();
                if (settings_counter >  0) {
                    getFragmentManager().popBackStack();
                }
                Log.d(TAG, String.format("Counter at %d",settings_counter));
                getFragmentManager().beginTransaction().replace(R.id.fragContainer, new ConSettingsFragment()).addToBackStack(null).commit();
                return true;
            /*case R.id.startAI:
                Log.d(TAG, "Start AI");
                return true;
            case R.id.listAI:
                Log.d(TAG, "List AI");
                return true;
            case R.id.closeAI:
                Log.d(TAG, "Close AI");
                return true; //*/
            /*case R.id.controlServer:
                Log.d(TAG, "Start/Close Server");
                return true;
            case R.id.serverLog:
                Log.d(TAG, "Server Log");
                return true;
            case R.id.serverOptions:
                Log.d(TAG, "Server Options");
                closeKeyboard();
                if (settings_counter >  0) {
                    getFragmentManager().popBackStack();
                }
                Log.d(TAG, String.format("Counter at %d",settings_counter));
                getFragmentManager().beginTransaction().replace(R.id.fragContainer, new ServerSettingsFragment()).addToBackStack(null).commit();
                return true;//*/
            case R.id.howToPlay:
                Log.d(TAG, "How To Play");
                closeKeyboard();
                if (settings_counter >  0) {
                    getFragmentManager().popBackStack();
                }
                Log.d(TAG, String.format("Counter at %d",settings_counter));
                HelpFragment howToPlayFrag = new HelpFragment();
                Bundle howToPlaySettings   = new Bundle();
                howToPlaySettings.putInt("layout_id",R.layout.fragment_howtoplay);
                howToPlaySettings.putInt("button_id",R.id.howToPlayClose);
                howToPlayFrag.setArguments(howToPlaySettings);
                getFragmentManager().beginTransaction().replace(R.id.fragContainer, howToPlayFrag).addToBackStack(null).commit();
                return true;
            case R.id.about:
                Log.d(TAG, "About");
                closeKeyboard();
                if (settings_counter >  0) {
                    getFragmentManager().popBackStack();
                }
                Log.d(TAG, String.format("Counter at %d",settings_counter));
                HelpFragment aboutFrag = new HelpFragment();
                Bundle aboutSettings = new Bundle();
                aboutSettings.putInt("layout_id",R.layout.fragment_about);
                aboutSettings.putInt("button_id",R.id.aboutClose);
                aboutFrag.setArguments(aboutSettings);
                getFragmentManager().beginTransaction().replace(R.id.fragContainer, aboutFrag).addToBackStack(null).commit();
                return true;
            default:
                Log.d(TAG, "???");
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.d(TAG, "Backstack available for pop");
            fm.popBackStack();
        } else {
            Log.d(TAG, "No backstack to pop from. Calling super.");
            super.onBackPressed();
        }
    }

    @Override
    public void setClientInfo(Client client, Thread clientThread) {
        Log.d(TAG,"Setting client info.");
        this.client = client;
        this.clientThread = clientThread;
    }

    public static void decrementCounter() {
        settings_counter--;
        Log.d(TAG, String.format("Counter at %d",settings_counter));
    }
    public static void incrementCounter() {
        settings_counter++;
        Log.d(TAG, String.format("Counter at %d",settings_counter));
    }
    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
