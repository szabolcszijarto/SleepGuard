package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class Peak implements java.io.Serializable {
	private static final long serialVersionUID = 19743001L;
	public Date start_time	= null;
	public Date end_time	= null;
	public int start_index	= -1;
	public int end_index	= -1;
	public short max_pulse	= -1;
	public short avg_pulse	= -1;
	public int duration		= -1; // in sec
}
