package de.uni_weimar.benike.shakeshake;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        LocationListener {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private long periodTime;
    private Context context;
    private StateTransitionReceiver stateTransitionReceiver;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor = null;

    private LocationManager locationManager;
    private int sampleRateUS = 40000;

    private SimpleXYSeries accelerometerXSeries = null;
    private SimpleXYSeries accelerometerYSeries = null;
    private SimpleXYSeries accelerometerZSeries = null;
    private SimpleXYSeries accelerometerMSeries = null;

    private int mWindowSize = 200;
    private XYPlot accelerometerPlot;
    private ANNImplV1 ann;

    float accX0;
    float accX1;
    float accY0;
    float accY1;
    float accZ0;
    float accZ1;
    double pga;
    private long startingTimestamp;
    float valX0;
    float valX1;
    float valY0;
    float valY1;
    float valZ0;
    float valZ1;
    private TextView textANNResult;
    private TextView textPGA;

    private TextView textState;

    private DecimalFormat decimalFormat;
    private double lastKnownLatitude = 0.0;
    private double lastKnownLongitude = 0.0;
    private long lastTimestamp = 0;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // update instantaneous data:
        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};


        // get rid the oldest sample in history:
        if (accelerometerXSeries.size() > mWindowSize - 1) {
            accelerometerXSeries.removeFirst();
            accelerometerYSeries.removeFirst();
            accelerometerZSeries.removeFirst();
            accelerometerMSeries.removeFirst();
        }

        // add the latest history sample:
        final float accelXdata = sensorEvent.values[0];
        final float accelYdata = sensorEvent.values[1];
        final float accelZdata = sensorEvent.values[2];
        accelerometerXSeries.addLast(null, accelXdata);
        accelerometerYSeries.addLast(null, accelYdata);
        accelerometerZSeries.addLast(null, accelZdata);
        accelerometerMSeries.addLast(null, Math.sqrt(accelXdata * accelXdata
                + accelYdata * accelYdata + accelZdata * accelZdata) /* - 9.81 */
        );

        //Log.d(TAG, "Sample added. Size of m series: " + mAccelerometerMSeries.size());

        long timestamp = System.currentTimeMillis();
        double annResult = -1.0d;
        valX0 = accelXdata;
        valY0 = accelYdata;
        valZ0 = accelZdata;
        accX0 = toground_rt(valX0, valX1, accX0, accX1);
        accY0 = toground_rt(valY0, valY1, accY0, accY1);
        accZ0 = toground_rt(valZ0, valZ1, accZ0, accZ1);


        annResult = this.ann.addAccelerometerReading(timestamp, this.accX0, this.accY0, this.accZ0);

        this.valX1 = this.valX0;
        this.valY1 = this.valY0;
        this.valZ1 = this.valZ0;
        this.accX1 = this.accX0;
        this.accY1 = this.accY0;
        this.accZ1 = this.accZ0;
        this.pga = this.ann.getPGA();

        if(annResult != -1.0d) {

            //Log.d(TAG, String.format("ANN-Result: %f", annResult));
            double pga = ann.getPGA();
            //Log.d(TAG, String.format("PGA: %f", pga));
            textANNResult.setText(decimalFormat.format(annResult));
            textPGA.setText(decimalFormat.format(pga));

            if(pga >= 12.0 && Math.abs(timestamp - lastTimestamp) > 3000) {
                lastTimestamp = timestamp;
                notifyBackend();
                switchToStreamingState();
            }
        }

        // redraw the Plots
        accelerometerPlot.redraw();


    }

    private void notifyBackend() {
        String token = PreferenceManager.getDefaultSharedPreferences(context).getString("TOKEN", "noTOKEN");
        long timestamp = System.currentTimeMillis();
        Backend.getInstance().registerActivity(token, timestamp, lastKnownLatitude, lastKnownLongitude);
    }

    private void switchToStreamingState() {
        textState.setBackgroundColor(Color.YELLOW);
        textState.setText("streaming");
    }

    private float toground_rt(float x0, float x1, float a0, float a1) {
        return ((x0 - x1) / 1.1111112f) + (0.8f * a1);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed: " + location);
        lastKnownLatitude = location.getLatitude();
        lastKnownLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class StateTransitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(StateTransition.UPDATE_STATE)) {
                Log.d(TAG, "Received intent to update state");
                String state = intent.getStringExtra("state");
                if(state != null && state.equals("STREAMING")) {
                    switchToStreamingState();
                }
            }
            if (intent.getAction().equals(StateTransition.TRIGGER)) {
                Log.d(TAG, "Received intent to trigger");
                switchToTrigger();
            }
        }
    }

    private void switchToTrigger() {
        textState.setBackgroundColor(Color.RED);
        textState.setText("TRIGGER");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.accX0 = 0.0f;
        this.accX1 = 0.0f;
        this.accY0 = 0.0f;
        this.accY1 = 0.0f;
        this.accZ0 = 0.0f;
        this.accZ1 = 0.0f;
        this.valX0 = 0.0f;
        this.valX1 = 0.0f;
        this.valY0 = 0.0f;
        this.valY1 = 0.0f;
        this.valZ0 = 0.0f;
        this.valZ1 = 0.0f;
        this.pga = 0.0d;

        decimalFormat = new DecimalFormat("#.#######");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (this.context == null) {
            this.context = getApplicationContext();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textANNResult = (TextView)findViewById(R.id.textANNResult);
        textPGA = (TextView)findViewById(R.id.textPGA);
        textState = (TextView)findViewById(R.id.textState);
        textState.setBackgroundColor(Color.GREEN);
        textState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTextState();
            }
        });

        // register for accelerometer events:
        sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerSensor = sensor;
            }
        }

        /*
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else {
            // TODO: handle case
        } */


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startLocationServices();

        // if we can't access the accelerometer sensor then exit:
        if (accelerometerSensor == null) {
            Log.e(TAG, "Failed to attach to Accelerator Sensor.");
            Toast.makeText(this, "Error! Failed to create accelerometer sensor!", Toast.LENGTH_LONG)
                    .show();
            cleanup();
        }

        changeSampleRate(sampleRateUS);
        accelerometerPlot = (XYPlot) findViewById(R.id.accelerometerPlot);
        accelerometerPlot.setRangeBoundaries(-15, 15, BoundaryMode.FIXED);
        accelerometerPlot.setDomainBoundaries(0, mWindowSize - 1, BoundaryMode.FIXED);


        accelerometerXSeries = new SimpleXYSeries("X");
        accelerometerXSeries.useImplicitXVals();
        accelerometerYSeries = new SimpleXYSeries("Y");
        accelerometerYSeries.useImplicitXVals();
        accelerometerZSeries = new SimpleXYSeries("Z");
        accelerometerZSeries.useImplicitXVals();
        accelerometerMSeries = new SimpleXYSeries("magnitude");
        accelerometerMSeries.useImplicitXVals();



        accelerometerPlot.addSeries(accelerometerXSeries,
                new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        accelerometerPlot.addSeries(accelerometerYSeries,
                new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
        accelerometerPlot.addSeries(accelerometerZSeries,
                new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
        accelerometerPlot.addSeries(accelerometerMSeries,
                new LineAndPointFormatter(Color.rgb(0, 0, 0), null, null, null));
        accelerometerPlot.setDomainStepValue(5);
        accelerometerPlot.setTicksPerRangeLabel(3);
        accelerometerPlot.setDomainLabel("Sample Index");
        accelerometerPlot.getDomainLabelWidget().pack();
        accelerometerPlot.setRangeLabel("m/s^2");
        accelerometerPlot.getRangeLabelWidget().pack();

        final PlotStatistics histStats = new PlotStatistics(1000, false);
        accelerometerPlot.addListener(histStats);

        // perform hardware accelerated rendering of the plots
        accelerometerPlot.setLayerType(View.LAYER_TYPE_NONE, null);

        ann = new ANNImplV1(0);
    }

    private void resetTextState() {
        textState.setBackgroundColor(Color.GREEN);
        textState.setText("preprocessing");
    }


    private void cleanup() {
        // unregister with the orientation sensor before exiting:
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }


    private void startLocationServices() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the4 location is missing.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            //startLocationServices();
        } else {
            // Access to the location has been granted to the app.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 4.0f, this);
            Location mobileLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(mobileLocation != null) {
                lastKnownLatitude = mobileLocation.getLatitude();
                lastKnownLongitude = mobileLocation.getLongitude();
            }
        }
    }


    public void changeSampleRate(int us) {
        sampleRateUS = us;
        Log.d(TAG, "Samplerate value: " + us);
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this, accelerometerSensor, us);
    }


    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        this.periodTime = -1;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(Constants.PREF_UPLOAD_PERIOD, -1);
        editor.commit();

        if (stateTransitionReceiver == null) stateTransitionReceiver = new StateTransitionReceiver();
        IntentFilter intentFilterState = new IntentFilter(StateTransition.UPDATE_STATE);
        IntentFilter intentFilterTrigger = new IntentFilter(StateTransition.TRIGGER);
        registerReceiver(stateTransitionReceiver, intentFilterState);
        registerReceiver(stateTransitionReceiver, intentFilterTrigger);

        toggleServiceStatus(sharedPreferences.getBoolean(Constants.PREF_SERVICE_TOGGLE, true), 3);
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

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, "Pressed twice!", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

