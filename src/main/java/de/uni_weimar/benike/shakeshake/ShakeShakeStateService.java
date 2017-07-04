package de.uni_weimar.benike.shakeshake;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShakeShakeStateService extends Service {

    public static int CACHE_SIZE = 0;
    private static final String[] FIELDS;
    private static final int HEARTBEAT_TIME = 7200000;
    private static final int INITIAL_STARTUP_TIME = 2100;
    private static final int MINIMUM_SAMPLEING_RATE = 25;
    public static int NUM_CACHES = 0;
    private static final int SAMPLEING_ADJUSTMENT_NUM = 100;
    private static final String TAG = "StateMachineService";
    private static boolean isRunning;
    float[] f27I;
    //private final int NOTIFICATION_ID;
    int adjustedSamplingRate;

    private int bufferTime;
    private List<SensorContentValues[]> caches;
    boolean calculateNewSamplingRate;
    private int currentCacheIndex;
    private long currentEventTime;
    //private State currentState;
    private int deviceID;
    boolean gpsUploading;
    private int inCacheIndex;
    float[] inR;
    private boolean isPowerSaveMode;
    private long lastHBSentTime;
    private Location lastLocation;
    //private State lastState;
    private boolean lastStatus;
    long lastTs;
    private short logicAlgorithm;
    private String logicMode;
    int ltanum;
    private BroadcastReceiver mReceiver;
    private long offset;
    float[] orientation;
    float[] outR;
    private long periodTime;
    double pga;
    private int rate;
    int sampleCount;
    private int sensorGroupID;
    private SharedPreferences sharedPreferences;
    int stanum;
    private long startTime;
    private String stateDescription;
    private int steadyTime;
    private int streamingTime;
    float threshold;
    private short triggerType;
    private String uuid;

    private AccelerometerReader reader;


    static {
        isRunning = false;
        NUM_CACHES = 50;
        CACHE_SIZE = SAMPLEING_ADJUSTMENT_NUM;
        FIELDS = new String[]{Constants.COLUMN_DEVICE_TIMESTAMP,
                Constants.COLUMN_ACC_X_VAL,
                Constants.COLUMN_ACC_Y_VAL,
                Constants.COLUMN_ACC_Z_VAL,
                Constants.COLUMN_MAG_X_VAL,
                Constants.COLUMN_MAG_Y_VAL,
                Constants.COLUMN_MAG_Z_VAL,
                Constants.COLUMN_HEADING,
                Constants.COLUMN_LOC_LAT,
                Constants.COLUMN_LOC_LON,
                Constants.COLUMN_LOC_ALT,
                Constants.COLUMN_ACC_NORM_VALUE,
                Constants.COLUMN_STATE};

    }

    public ShakeShakeStateService() {
        //this.NOTIFICATION_ID = GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES;
        this.adjustedSamplingRate = 0;
        this.lastTs = 0;
        this.calculateNewSamplingRate = false;
        this.sampleCount = 0;
        this.gpsUploading = false;
        this.offset = 0;
        this.caches = new ArrayList();
        this.currentCacheIndex = 0;
        this.inCacheIndex = 0;
        //this.mNTPAlarm = new NTPAlarmReceiver();
        //this.mDBMonitorAlarm = new DBMonitorAlarmReceiver();
        //this.mPolicy = null;
        this.isPowerSaveMode = false;
        this.inR = new float[9];
        this.f27I = new float[9];
        this.orientation = new float[3];
        this.outR = new float[9];
    }


    public static boolean isRunning() {
        return isRunning;
    }

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Starting service");
        isRunning = true;
        int maxTime = Math.max(this.steadyTime, this.bufferTime);

        //this.mPolicy = new WakeLockPolicy(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "OnStart Received start id " + startId + ": " + intent);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.isPowerSaveMode = this.sharedPreferences.getBoolean(Constants.POWER_SAVING_CONFIG_KEY, false);
        if (this.isPowerSaveMode) {
            stopForeground(true);
        } else {
            runAsForegroundService();
        }
        //notifyServiceStatus(true);
        //this.appState = (GlobalApplicationState) getApplicationContext();
        //this.uploaderFactory = new UploaderFactory(this);
        this.lastStatus = false;
        this.startTime = System.currentTimeMillis();
        if (intent == null || intent.getExtras() == null) {
            //loadOptions();
        } else {
            //parseOptions(intent.getExtras().getString(HttpOptions.METHOD_NAME));
            short triggerFromGCM = intent.getExtras().getShort("TRIGGER", (short) -1);
            if (triggerFromGCM == (short) 4 || triggerFromGCM == (short) 5) {
                this.triggerType = triggerFromGCM;
            }
        }
        if (((int) (((double) (NUM_CACHES * CACHE_SIZE)) * ((1.0d / ((double) this.rate)) * 1000.0d))) < Math.max(this.steadyTime, this.bufferTime)) {
            this.bufferTime = Constants.BUFFER_TIME;
            this.steadyTime = Constants.STEADY_TIME;
        }
        for (int i = 0; i < NUM_CACHES; i++) {
            SensorContentValues[] cache = new SensorContentValues[CACHE_SIZE];
            for (int j = 0; j < CACHE_SIZE; j++) {
                SensorContentValues values = new SensorContentValues(i, j);
                for (String field : FIELDS) {
                    values.put(field, 0);
                }
                values.put(Constants.COLUMN_STATE, -1);
                cache[j] = values;
            }
            this.caches.add(cache);
        }
        //this.uploader.start();
        this.sensorGroupID = 0;
        this.pga = 0.0d;
        this.currentEventTime = System.currentTimeMillis();
        if (!(this.triggerType == (short) 4 || this.triggerType == (short) 5)) {
            //sendHeartbeatMsg(System.currentTimeMillis(), (short) 0);
        }
        this.lastHBSentTime = 0;
        //this.algorithmSTALTA = new STALTA(this, this.threshold, this.stanum, this.ltanum);
        //this.appState.setSensorService(this);
        //this.mPolicy.setUp();
        return Service.START_STICKY;
    }

    private void runAsForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(603979776);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        String appName = getResources().getString(R.string.app_name);
        String statusMsg = new String("Sensor active!");
        Notification notification = new Notification.Builder(this).setContentTitle(appName).setContentText(statusMsg).setSubText("App in foreground").setSmallIcon(R.drawable.app_icon).setContentIntent(pendingIntent).build();
        notification.flags |= 2;
        notification.flags |= 32;
        //startForeground(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES, notification);
        startForeground(23, notification);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    /*public void loadOptions() {
        this.reader = new AccelerometerReader(this);
        this.periodTime = this.sharedPreferences.getLong(Constants.PREF_UPLOAD_PERIOD, -1);
        if (this.periodTime > 0) {
            this.lastStatus = false;
            notifyNewStateTransition(new StreamingState(this, this.periodTime));
            this.lastState = new TriggerState();
        } else {
            this.lastStatus = false;
            notifyNewStateTransition(new SteadyState(this));
            this.lastState = this.currentState;
        }
        int samplingRate = this.sharedPreferences.getInt(Constants.PREF_SENSOR_DELAY, Constants.PREF_SENSOR_DELAY_IN_MICRO_SEC);
        if (samplingRate != 0) {
            this.rate = (int) (1.0d / (((double) samplingRate) / 1000000.0d));
        } else {
            this.rate = MINIMUM_SAMPLEING_RATE;
        }
        this.appState.setSamplingRate(this.rate);
        this.bufferTime = Integer.parseInt(this.sharedPreferences.getString(Constants.PREF_BUFFER_TIME, "120000"));
        this.steadyTime = this.sharedPreferences.getInt(Constants.PREF_STEADY_TIME, Constants.STEADY_TIME);
        this.streamingTime = this.sharedPreferences.getInt(Constants.PREF_STREAMING_TIME, Constants.STREAM_TIME);
        this.deviceID = this.sharedPreferences.getInt(Constants.PROPERTY_DEVICE_ID, -1);
        this.uuid = Utils.getDefaultDeviceUuid(getApplicationContext());
        this.logicMode = this.sharedPreferences.getString(Constants.PREF_LOGIC, Constants.PREF_LOGIC_ANN);
        if (this.logicMode.contentEquals(Constants.PREF_SERVICE_OPTION_JAVA)) {
            this.logicAlgorithm = (short) 1;
            this.triggerType = (short) 1;
        } else if (this.logicMode.contentEquals(Constants.PREF_SERVICE_OPTION_NATIVE)) {
            this.logicAlgorithm = (short) 2;
            this.triggerType = (short) 2;
        } else if (this.logicMode.contentEquals(Constants.PREF_LOGIC_ANN)) {
            this.logicAlgorithm = (short) 3;
            this.triggerType = (short) 3;
            ANNLoader.getInstance().requestUpdate(this);
        }
        Log.d(TAG, "Trigger logic: " + this.logicAlgorithm);
        this.threshold = this.sharedPreferences.getFloat(Constants.PREF_LOGIC_OPTION_STALTA_THRESHOLD, 15.0f);
    }*/

}
