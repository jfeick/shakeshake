package de.uni_weimar.benike.shakeshake;

import java.util.HashMap;
import java.util.Map;

public class SensorContentValues {
    private int cacheIndex;
    private Map<String, Object> contentValues;
    private int inCacheIndex;

    public SensorContentValues(int cacheIndex, int inCacheIndex) {
        this.cacheIndex = cacheIndex;
        this.inCacheIndex = inCacheIndex;
        this.contentValues = new HashMap();
    }

    public SensorContentValues(SensorContentValues newSensorContentValue) {
        newSensorContentValue.cacheIndex = this.cacheIndex;
        newSensorContentValue.inCacheIndex = this.inCacheIndex;
        newSensorContentValue.contentValues = new HashMap(this.contentValues);
    }

    public int getCacheIndex() {
        return this.cacheIndex;
    }

    public int getInCacheIndex() {
        return this.inCacheIndex;
    }

    public Map<String, Object> getMap() {
        return this.contentValues;
    }

    public void put(String field, String value) {
        this.contentValues.put(field, value);
    }

    public void put(String field, long value) {
        this.contentValues.put(field, Long.valueOf(value));
    }

    public void put(String field, float value) {
        this.contentValues.put(field, Float.valueOf(value));
    }

    public void put(String field, double value) {
        this.contentValues.put(field, Double.valueOf(value));
    }

    public void put(String field, int value) {
        this.contentValues.put(field, Integer.valueOf(value));
    }

    public Double getAsDouble(String field) {
        Double valueOf;
        Double d = null;
        Object value = this.contentValues.get(field);
        if (value != null) {
            try {
                valueOf = Double.valueOf(((Number) value).doubleValue());
            } catch (ClassCastException e) {
                if (!(value instanceof CharSequence)) {
                    return d;
                }
                try {
                    return Double.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    return d;
                }
            }
        }
        valueOf = d;
        return valueOf;
    }

    public Long getAsLong(String field) {
        Long valueOf;
        Long l = null;
        Object value = this.contentValues.get(field);
        if (value != null) {
            try {
                valueOf = Long.valueOf(((Number) value).longValue());
            } catch (ClassCastException e) {
                if (!(value instanceof CharSequence)) {
                    return l;
                }
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    return l;
                }
            }
        }
        valueOf = l;
        return valueOf;
    }

    public Float getAsFloat(String field) {
        Float valueOf;
        Float f = null;
        Object value = this.contentValues.get(field);
        if (value != null) {
            try {
                valueOf = Float.valueOf(((Number) value).floatValue());
            } catch (ClassCastException e) {
                if (!(value instanceof CharSequence)) {
                    return f;
                }
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    return f;
                }
            }
        }
        valueOf = f;
        return valueOf;
    }

    public Integer getAsInt(String field) {
        Integer valueOf;
        Integer num = null;
        Object value = this.contentValues.get(field);
        if (value != null) {
            try {
                valueOf = Integer.valueOf(((Number) value).intValue());
            } catch (ClassCastException e) {
                if (!(value instanceof CharSequence)) {
                    return num;
                }
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    return num;
                }
            }
        }
        valueOf = num;
        return valueOf;
    }
}
