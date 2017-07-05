package de.uni_weimar.benike.shakeshake;



public class CircularBuffer {

    public class AccelerometerReading {
        public long timestamp;
        public float f3x;
        public float f4y;
        public float f5z;
    }

    private AccelerometerReading[] buffer;
    private int head;
    private int f6n;
    private int tail;

    public CircularBuffer(int n) {
        this.buffer = new AccelerometerReading[n];
        this.f6n = n;
        this.tail = 0;
        this.head = 0;
        for (int i = 0; i < n; i++) {
            this.buffer[i] = new AccelerometerReading();
        }
    }

    public AccelerometerReading get(int index) {
        return this.buffer[(this.tail + index) % this.f6n];
    }

    public void add(long ts, float x, float y, float z) {
        this.buffer[this.head].timestamp = ts;
        this.buffer[this.head].f3x = x;
        this.buffer[this.head].f4y = y;
        this.buffer[this.head].f5z = z;
        this.head = (this.head + 1) % this.f6n;
        if (this.head == this.tail) {
            this.tail = (this.tail + 1) % this.f6n;
        }
    }

    public long getHeadTailTimeDelta() {
        if (this.head == this.tail) {
            return -1;
        }
        return this.buffer[(this.head + (this.f6n - 1)) % this.f6n].timestamp - this.buffer[this.tail].timestamp;
    }

    public int getHeadTailCount() {
        return this.head > this.tail ? this.head - this.tail : (this.head + this.f6n) - this.tail;
    }

    public void moveTail(long delta) {
        long threshold = this.buffer[this.tail].timestamp + delta;
        while (this.tail != this.head && this.buffer[this.tail].timestamp < threshold) {
            this.tail = (this.tail + 1) % this.f6n;
        }
        if (this.head != this.tail) {
        }
    }
}
