package com.szabolcs.szijarto.sleepguard;

import java.text.DateFormat;
import java.util.Date;

public abstract class GenericWatcher {

	Date timeStarted, timeStopped;
	String timeElapsed = "";
	boolean started = false;
	long elapsedMillis, elapsedSecs, elapsedMins, elapsedHours;

	Activity_Main myAct = null;
	
	public GenericWatcher (Activity_Main a) {
		started = false;
		myAct = a;
	}
	
	public GenericWatcher() {
		started = false;
	}
		
	public void start() {
	  started = true;
	  timeStarted = new Date();
	  timeStopped = null;
	  timeElapsed = "running...";
	}
	
	public void stop() {
	  started = false;
	  timeStopped = new Date();
	  elapsedMillis = timeStopped.getTime() - timeStarted.getTime();
	  elapsedSecs = elapsedMillis/1000;
	  elapsedMins = elapsedSecs/60;
	  elapsedHours = elapsedMins/60;
	  timeElapsed =  elapsedHours +":"+ elapsedMins +":"+ elapsedSecs; 
	}

	public boolean isRunning() {
		  return started;
	}

	public String getTimeStarted() {
		if (timeStarted != null) {
			return DateFormat.getDateTimeInstance().format(timeStarted);
		} else return "";
	}

	public String getTimeStopped() {
		if (timeStopped != null) {
			return DateFormat.getDateTimeInstance().format(timeStopped);
		} else return "";
	}

	public String getTimeElapsed() {
		return timeElapsed;
	}
	
}
