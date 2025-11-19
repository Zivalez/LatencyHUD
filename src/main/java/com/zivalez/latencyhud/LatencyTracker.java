package com.zivalez.latencyhud;

public class LatencyTracker {
    private static final int MAX_HISTORY = 2400;
    private final float[] frameTimes = new float[MAX_HISTORY];
    private int head = 0;
    private int count = 0;
    private double totalSum = 0;

    public void addSample(float ms) {
        // Remove old value from sum if buffer is full
        if (count >= Config.HISTORY_LENGTH.get()) {
            int tail = (head - Config.HISTORY_LENGTH.get() + MAX_HISTORY) % MAX_HISTORY;
            totalSum -= frameTimes[tail];
        } else {
            count++;
        }

        // Add new value
        frameTimes[head] = ms;
        totalSum += ms;

        // Move head
        head = (head + 1) % MAX_HISTORY;
    }

    public double getAverage() {
        if (count == 0) return 0;
        return totalSum / count;
    }

    public float getMin() {
        if (count == 0) return 0;
        float min = Float.MAX_VALUE;
        int limit = Math.min(count, Config.HISTORY_LENGTH.get());
        for (int i = 0; i < limit; i++) {
            int index = (head - 1 - i + MAX_HISTORY) % MAX_HISTORY;
            if (frameTimes[index] < min) min = frameTimes[index];
        }
        return min;
    }

    public float getMax() {
        if (count == 0) return 0;
        float max = Float.MIN_VALUE;
        int limit = Math.min(count, Config.HISTORY_LENGTH.get());
        for (int i = 0; i < limit; i++) {
            int index = (head - 1 - i + MAX_HISTORY) % MAX_HISTORY;
            if (frameTimes[index] > max) max = frameTimes[index];
        }
        return max;
    }

    public float getSample(int offset) {
        if (offset >= count || offset < 0) return 0;
        int index = (head - 1 - offset + MAX_HISTORY) % MAX_HISTORY;
        return frameTimes[index];
    }

    public int getCount() {
        return Math.min(count, Config.HISTORY_LENGTH.get());
    }
    
    public void reset() {
        head = 0;
        count = 0;
        totalSum = 0;
    }
}
