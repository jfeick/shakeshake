package de.uni_weimar.benike.shakeshake;

import android.content.Context;
import android.location.Location;

public interface SensorService {
    void accuracyChanged();

    Context getContext();

    String getLogicMode();

    String getStateDescription();

    Object getSystemService(String str);

    String getUploadMode();

    SensorContentValues nextEvent(SensorContentValues sensorContentValues);

    void preProcessingFinished(boolean z);

    void processAccelerometerEvent(float[] fArr, float[] fArr2, long j, Location location, long j2);

    void requestHeartBeatMessage();

    void setDuration(long j, short s);

    void setStateDescription(String str);

    void steadyFinished(long j, float f, float f2, float f3, double d, double d2);

    void streamingFinished();

    void triggerFinished(long j);
}
