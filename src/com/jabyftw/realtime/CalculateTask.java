package com.jabyftw.realtime;

import java.util.Date;

public class CalculateTask implements Runnable {
    private RealTime plugin;

    public CalculateTask(RealTime plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        calculateTime();
    }
        
    public float getTimeSec(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int min = Integer.parseInt(time.substring(3, 5));
        int sec = Integer.parseInt(time.substring(6, 8));
        return (hour * 60 * 60) + (min * 60) + (sec);
    }

    private void calculateTime() {
        String time = new Date().toString().substring(11, 19);
        plugin.resultedTime = (int) ((getTimeSec(time) / 3.6) - 6000) + (plugin.timeFix);
    }
}