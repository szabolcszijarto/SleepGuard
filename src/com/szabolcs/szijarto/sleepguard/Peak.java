package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class Peak implements java.io.Serializable {
	private static final long serialVersionUID = 19743001L;
	public Date start_time;
	public Date end_time;
	public int start_index;
	public int end_index;
	public byte max_pulse;
	public long duration;		// in sec

	public Peak (HeartRateRec r, int index) {
		start_index = index;
		start_time = r.timestamp;
		max_pulse=r.pulse;
		duration = 0;
	}

	public void add(HeartRateRec r, int index) {
		if (r.pulse > max_pulse) {
			max_pulse = r.pulse;
		}
		end_index=index;
		end_time=r.timestamp;
		duration = (end_time.getTime() - start_time.getTime()) / 1000 ;
	}

	public void close() {
	}
	
	public String getDurationString() {
		if ( (start_time == null) || (end_time == null) ) {
			return "";
		}
		long dur = (end_time.getTime() - start_time.getTime()) / 1000 ;
		int hour  = (int) ( dur / 3600 ) ;
		int min   = (int) ( dur - (hour*3600) ) / 60 ;
		int sec   = (int) ( dur - (hour*3600) ) % 60 ;
		return ( hour + ":" + min + ":" + sec );
	}
}