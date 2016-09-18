package com.szabolcs.szijarto.sleepguard;

import java.util.Date;
import java.util.Locale;

public class Peak implements java.io.Serializable {
	private static final long serialVersionUID = 19743001L;
	public Date start_time, end_time;
	public int start_index, end_index;
	public byte max_pulse;
	public long duration;		// in millisec
	public boolean beginner;
	public float score;

	public Peak (HeartRateRec r, int index) {
		start_index = index;
		start_time = r.timestamp;
		end_time = start_time;
		duration = 0;
		max_pulse=r.pulse;
		beginner = false;
	}

	public void add(HeartRateRec r, int index) {
		if (r.pulse > max_pulse) max_pulse = r.pulse;
		end_index=index;
		end_time=r.timestamp;
		duration = (end_time.getTime() - start_time.getTime());
		score = duration / 1000 * max_pulse / 1000;
	}

	public void close() {
	}
	
	public String getDurationString() {
		long dur = duration / 1000 ; // in sec
		int hour  = (int) ( dur / 1000 / 3600 ) ;
		int min   = (int) ( dur - (hour*3600) ) / 60 ;
		int sec   = (int) ( dur - (hour*3600) ) % 60 ;
		return ( String.format(Locale.US, "%02d:%02d:%02d", hour, min, sec ) );
	}
}