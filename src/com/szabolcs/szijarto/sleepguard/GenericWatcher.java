package com.szabolcs.szijarto.sleepguard;

import java.text.DateFormat;

public abstract class GenericWatcher {

	protected long timeStarted, timeStopped;
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
		running = true;
		wasEverStarted = true;
		timeStarted = System.currentTimeMillis();
		timeElapsedFormatted = "running...";
	}

	private void calculateTimeElapsedFormatted() {
		long elapsedMillis = timeStopped - timeStarted;
		long elapsedSecs = elapsedMillis / 1000;
		long elapsedMins = elapsedSecs / 60;
		long elapsedHours = elapsedMins / 60;
		timeElapsedFormatted = elapsedHours + ":" + elapsedMins + ":"
				+ elapsedSecs;
	}

	public void stop() {
		running = false;
		timeStopped = System.currentTimeMillis();
		calculateTimeElapsedFormatted();
	}

	public boolean isRunning() {
		return running;
	}

	public String getTimeStarted() throws GenericWatcherException {
		final String timeFormatted;
		if (wasEverStarted) {
			timeFormatted = DateFormat.getDateTimeInstance()
					.format(timeStarted);
		} else {
			throw new GenericWatcherException(
					"Watcher was asked for start time, but was never started.");
		}
		return timeFormatted;
	}

	private boolean wasStoppedSinceLastStart() {
		return wasEverStarted && !isRunning();
	}

	public String getTimeStopped() throws GenericWatcherException {
		final String timeFormatted;
		if (wasStoppedSinceLastStart()) {
			timeFormatted = DateFormat.getDateTimeInstance()
					.format(timeStopped);
		} else {
			throw new GenericWatcherException(
					"Watcher was asked for stop time, but was not stopped since last start.");
		}
		return timeFormatted;
	}

	public String getTimeElapsed() {
		return timeElapsedFormatted;
	}

}
