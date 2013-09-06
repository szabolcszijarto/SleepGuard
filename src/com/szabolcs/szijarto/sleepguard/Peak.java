package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class Peak implements java.io.Serializable {
	private static final long serialVersionUID = 19743001L;
	public Date start		= null;
	public Date end			= null;
	public int startseqno	= -1;
	public int endseqno		= -1;
	public short maxPulse	= -1;
	public short avgPulse	= -1;
	public int duration		= -1; // in sec
	public int heartbeats	= -1;
}
