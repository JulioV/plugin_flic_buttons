package com.aware.plugin.flic_buttons;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    private Intent aware;

    /**
     * Broadcasted with button label <br/>
     * Extra: button, String label
     */
    public static final String ACTION_AWARE_PLUGIN_BUTTON_CLICKED = "ACTION_AWARE_PLUGIN_BUTTON_CLICKED";

    /**
     * Extra for ACTION_AWARE_PLUGIN_BUTTON_CLICKED
     * (String) clicked button label
     */
    public static final String EXTRA_BUTTON = "button";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_AWARE_PLUGIN_BUTTON_CLICKED);
        registerReceiver(flicReceiver, filter);
        if (Aware.DEBUG) Log.d(Plugin.TAG, "Buttons broadcast receiver registered");
        /**
         * Plugins share their current status, i.e., context using this method.
         * This method is called automatically when triggering
         * {@link Aware#ACTION_AWARE_CURRENT_CONTEXT}
         **/
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
            }
        };

        //Add permissions you need (Android M+).
        //By default, AWARE asks access to the #Manifest.permission.WRITE_EXTERNAL_STORAGE

        //REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.FlicButtons_Data.CONTENT_URI }; //this syncs dummy FlicButtons_Data to server

        //Initialise AWARE's service
        aware = new Intent(this, Aware.class);
        startService(aware);
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            //Initialize our plugin's settings
            Aware.setSetting(this, Settings.STATUS_PLUGIN_FLIC_BUTTONS, true);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(flicReceiver);

        Aware.setSetting(this, Settings.STATUS_PLUGIN_FLIC_BUTTONS, false);

        //Stop AWARE's service
        stopService(aware);
    }

    private final BroadcastReceiver flicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_AWARE_PLUGIN_BUTTON_CLICKED)){
                String buttonLabel = intent.getStringExtra(EXTRA_BUTTON);
                if(buttonLabel != null){
                    ContentValues data = new ContentValues();
                    data.put(Provider.FlicButtons_Data.TIMESTAMP, System.currentTimeMillis());
                    data.put(Provider.FlicButtons_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                    data.put(Provider.FlicButtons_Data.BUTTON, buttonLabel);

                    getContentResolver().insert(Provider.FlicButtons_Data.CONTENT_URI, data);
                    if (Aware.DEBUG) Log.d(Plugin.TAG, data.toString());
                }
            }
        }
    };
}
