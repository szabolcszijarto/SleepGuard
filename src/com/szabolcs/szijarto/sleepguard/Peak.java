package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class Peak implements java.io.Serializable {
	private static final long serialVersionUID = 19743001L;
	public Date start_time;
	public Date end_time;
	public int start_index;
	public int end_index;
	public short max_pulse;
	public long duration;		// in sec

	public Peak (HeartRateRec r, int index) {
		start_time = r.timestamp;
		end_time = null;
		start_index = index;
		end_index = -1;
		max_pulse=r.pulse;
		duration = 0;
	}

	public void add(HeartRateRec r) {
		if (r.pulse > max_pulse) { max_pulse = r.pulse; };
		end_time=r.timestamp;
		duration = (end_time.getTime() - start_time.getTime()) / 1000 ;
	}

	public void close(int end) {
		end_index=end;
		duration = (end_time.getTime() - start_time.getTime()) / 1000 ;
	}
	
}