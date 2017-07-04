package de.uni_weimar.benike.shakeshake;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long periodTime;
    private Context context;
    private StateTransitionReceiver stateTransitionReceiver;

    private class StateTransitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StateTransition.UPDATE_STATE)) {
                Log.d(TAG, "Received intent to update state");
                // Do stuff - maybe update my view based on the changed DB contents
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (this.context == null) {
            this.context = getApplicationContext();
        }
    }


    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.periodTime = -1;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_UPLOAD_PERIOD, -1);
        editor.commit();

        if (stateTransitionReceiver == null) stateTransitionReceiver = new StateTransitionReceiver();
        IntentFilter intentFilter = new IntentFilter(StateTransition.UPDATE_STATE);
        registerReceiver(stateTransitionReceiver, intentFilter);

        toggleServiceStatus(sharedPreferences.getBoolean(Constants.PREF_SERVICE_TOGGLE, true), 3);

        XMLRPCCallback listener = new XMLRPCCallback() {
            public void onResponse(long id, Object result) {
                Log.d(TAG, "received response");
            }
            public void onError(long id, XMLRPCException error) {
                Log.d(TAG, "received error");
            }
            public void onServerError(long id, XMLRPCServerException error) {
                Log.d(TAG, "received server error");
            }
        };

        try {
            URL url = new URL("http://192.168.2.102:8000/RPC2");
            XMLRPCClient client = new XMLRPCClient(url);
            long id = client.callAsync(listener, "add", 5, 10);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        if (stateTransitionReceiver != null) unregisterReceiver(stateTransitionReceiver);
        super.onPause();
    }

    public void toggleServiceStatus(boolean action, int triggerType) {
        if (ShakeShakeStateService.isRunning()) {
            if (!action) {
                stopService(new Intent(this, ShakeShakeStateService.class));
            }
        } else if (action) {
            Intent intent = new Intent(this, ShakeShakeStateService.class);
            intent.putExtra("TRIGGER", (short) triggerType);
            startService(intent);
        }
    }

}

