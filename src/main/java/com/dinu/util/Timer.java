package com.dinu.util;

public class Timer {
    private long start;
    private long end;

    public Timer() {
        start = System.currentTimeMillis();
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public long durationInMillis() {
        return end - start;
    }

    public String durationAsString() {
        return durationInMillis() + " ms";
    }
}
