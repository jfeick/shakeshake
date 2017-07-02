package de.uni_weimar.benike.shakeshake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private long periodTime;
    private Context context;


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

        toggleServiceStatus(sharedPreferences.getBoolean(Constants.PREF_SERVICE_TOGGLE, true), 3);
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

