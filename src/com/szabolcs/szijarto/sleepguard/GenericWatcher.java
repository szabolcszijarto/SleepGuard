package com.szabolcs.szijarto.sleepguard;

import java.text.DateFormat;
import java.util.Formatter;

public abstract class GenericWatcher {

	protected long timeStarted, timeStopped;
    private long elapsedMillis, elapsedSecs, elapsedMins, elapsedHours;

    private String timeElapsedFormatted = "";
	private boolean running = false;
	private boolean wasEverStarted = false;

	protected Activity_Main myact = null;

	public GenericWatcher(Activity_Main a) {
		myact = a;
	}

	public GenericWatcher() {
		running = false;
	}

	public void start() {
		if (!running) {
			running = true;
			wasEverStarted = true;
			timeStarted = System.currentTimeMillis();
		}
	}

	public Object stop() {
		if (running) {
			running = false;
			timeStopped = System.currentTimeMillis();
			calculateTimeElapsed();
		}
		return null;
	}

  	public boolean isRunning() {
		return running;
	}

	private void calculateTimeElapsed() {
        long endTime;

		if (isRunning()) {
            // still running, calculate elapsed based on current time
            endTime = System.currentTimeMillis();
        } else {
            // not running, calculate elapsed based on stop time
            endTime = timeStopped;
        }
        elapsedMillis = endTime - timeStarted;
        elapsedSecs = elapsedMillis / 1000;
        elapsedMins = elapsedSecs / 60;
		elapsedSecs = elapsedSecs - ( elapsedMins * 60 );
        elapsedHours = elapsedMins / 60;
		elapsedMins = elapsedMins - ( elapsedHours * 60 );
	}

    public String getTimeStartedString() {
		final String timeFormatted;
		if (wasEverStarted)
			timeFormatted = DateFormat.getDateTimeInstance().format(timeStarted);
        else
            timeFormatted = "";
		return timeFormatted;
	}

	public String getTimeStoppedString() {
		final String timeFormatted;
		if (wasEverStarted && !isRunning())
			timeFormatted = DateFormat.getDateTimeInstance().format(timeStopped);
        else
            timeFormatted = "";
		return timeFormatted;
	}

	public String getTimeElapsedString() {
        calculateTimeElapsed();
        timeElapsedFormatted = String.format("%02d:%02d:%02d", elapsedHours , elapsedMins, elapsedSecs);
        return timeElapsedFormatted;
	}
}
