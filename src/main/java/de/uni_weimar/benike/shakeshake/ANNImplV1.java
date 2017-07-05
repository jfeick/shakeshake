package de.uni_weimar.benike.shakeshake;

import java.util.Arrays;

public class ANNImplV1  {
    private static final int BUFFER_SIZE_IN_SECONDS = 10;
    private static final int DEFAULT_SAMPLING_RATE = 25;
    private static final String TAG = "ANNImpl";
    private float[] accX;
    private float[] accY;
    private float[] accZ;
    private CircularBuffer buffer;
    double feature1;
    double feature2;
    double feature3;
    double[] featureMatrix;
    final double[] featureScale1;
    final double[] featureScale2;
    double[] featureScaled;
    final double firstHiddenLayerIntercept;
    final double[] firstLayerIntercept;
    double pga;
    int samplingRate;
    private long[] time;
    private int totalDataNum;
    private double[] vectorSum;
    private int version;
    final double[][] weight1;
    final double[][] weight2;

    public ANNImplV1(int samplingRate) {
        this.version = 1;
        this.featureScale1 = new double[]{0.0d, 0.0227272d, 2.51066E-5d};
        this.featureScale2 = new double[]{1.21348896d, 44.0d, 3.0072554d};
        this.weight1 = new double[][]{new double[]{19.4522866d, -25.47379755d, 2.90223887d, -3.65146956d, 3.73615375d}, new double[]{-1.36542935d, 1.4726235d, -4.50064579d, -6.1051623d, 1.14908854d}, new double[]{6.06770995d, -9.7919339d, 7.67831497d, 4.07577297d, -0.55094589d}};
        double[][] r1 = new double[5][];
        r1[0] = new double[]{15.03806598d};
        r1[1] = new double[]{-21.32847482d};
        r1[2] = new double[]{-14.084015d};
        r1[3] = new double[]{-7.77852932d};
        r1[4] = new double[]{2.02010355d};
        this.weight2 = r1;
        this.firstLayerIntercept = new double[]{1.0116998d, -0.86434653d, -0.0227769d, 3.12699554d, 1.10308159d};
        this.firstHiddenLayerIntercept = -0.28625936d;
        if (samplingRate == 0) {
            samplingRate = DEFAULT_SAMPLING_RATE;
        }
        this.samplingRate = samplingRate;
        int maxSize = samplingRate * BUFFER_SIZE_IN_SECONDS;
        this.accX = new float[maxSize];
        this.accY = new float[maxSize];
        this.accZ = new float[maxSize];
        this.time = new long[maxSize];
        this.vectorSum = new double[maxSize];
        this.buffer = new CircularBuffer(maxSize);
        this.featureScaled = new double[3];
        this.featureMatrix = new double[3];
        this.pga = 0.0d;
    }

    public int getANNversion() {
        return this.version;
    }

    public double addAccelerometerReading(long ts, float x, float y, float z) {

        this.buffer.add(ts, x, y, z);
        if (this.buffer.getHeadTailTimeDelta() < 2000) {
            return -1.0d;
        }
        this.feature1 = calcFeature1();
        this.feature2 = (double) calcFeature2();
        this.feature3 = calcFeature3();
        calcScaledFeatures();
        this.featureMatrix[0] = this.featureScaled[0];
        this.featureMatrix[1] = this.featureScaled[1];
        this.featureMatrix[2] = this.featureScaled[2];
        double[] a1 = add(dot1(this.featureMatrix, this.weight1), this.firstLayerIntercept);
        return 1.0d / (1.0d + Math.exp(-(dot2(new double[]{1.0d / (1.0d + Math.exp(-a1[0])), 1.0d / (1.0d + Math.exp(-a1[1])), 1.0d / (1.0d + Math.exp(-a1[2])), 1.0d / (1.0d + Math.exp(-a1[3])), 1.0d / (1.0d + Math.exp(-a1[4]))}, this.weight2) - 14.83970048d)));
    }

    public double getPGA() {
        return this.pga;
    }

    private void calcScaledFeatures() {
        double adjustedFeatureScale = this.featureScale2[1] * (((double) this.samplingRate) / 25.0d);
        this.featureScaled[0] = (this.feature1 - this.featureScale1[0]) / this.featureScale2[0];
        this.featureScaled[1] = (this.feature2 - this.featureScale1[1]) / adjustedFeatureScale;
        this.featureScaled[2] = (this.feature3 - this.featureScale1[2]) / this.featureScale2[2];
    }

    private double calcFeature1() {
        this.totalDataNum = this.buffer.getHeadTailCount();
        for (int i = 0; i < this.totalDataNum; i++) {
            CircularBuffer.AccelerometerReading v = this.buffer.get(i);
            float valX0 = v.f3x;
            float valY0 = v.f4y;
            float valZ0 = v.f5z;
            this.accX[i] = valX0;
            this.accY[i] = valY0;
            this.accZ[i] = valZ0;
            this.vectorSum[i] = Math.sqrt((double) (((valX0 * valX0) + (valY0 * valY0)) + (valZ0 * valZ0)));
            this.time[i] = v.timestamp;
        }
        this.buffer.moveTail(1000);
        double[] interQurtl = new double[this.totalDataNum];
        System.arraycopy(this.vectorSum, 0, interQurtl, 0, this.totalDataNum);
        Arrays.sort(interQurtl);
        return interQurtl[(this.totalDataNum * 3) / 4] - interQurtl[this.totalDataNum / 4];
    }

    private int calcFeature2() {
        int zcX = 0;
        int zcY = 0;
        int zcZ = 0;
        this.pga = 0.0d;
        for (int i = 0; i < this.totalDataNum - 1; i++) {
            if (this.accX[i] * this.accX[i + 1] < 0.0f) {
                zcX++;
            }
            if (this.accY[i] * this.accY[i + 1] < 0.0f) {
                zcY++;
            }
            if (this.accZ[i] * this.accZ[i + 1] < 0.0f) {
                zcZ++;
            }
            this.pga = Math.max(this.pga, Math.max(Math.abs((double) this.accX[i]), Math.max(Math.abs((double) this.accY[i]), Math.abs((double) this.accZ[i]))));
        }
        return Math.max(Math.max(zcX, zcY), zcZ);
    }

    private double calcFeature3() {
        double cav = 0.0d;
        for (int i = 0; i < this.totalDataNum - 1; i++) {
            double amp = (this.vectorSum[i] + this.vectorSum[i + 1]) / 2.0d;
            cav += (((double) (this.time[i + 1] - this.time[i])) / 1000.0d) * amp;
        }
        return cav;
    }

    public static double[] dot1(double[] x, double[][] y) {
        double[] matrix = new double[5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < x.length; j++) {
                matrix[i] = matrix[i] + (x[j] * y[j][i]);
            }
        }
        return matrix;
    }

    public static double dot2(double[] x, double[][] y) {
        double sum = 0.0d;
        for (int i = 0; i < 5; i++) {
            sum += x[i] * y[i][0];
        }
        return sum;
    }

    public static double[] add(double[] x, double[] y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] + y[i];
        }
        return z;
    }

}
