package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class HeartRateRec implements java.io.Serializable {
	private static final long serialVersionUID = 19742001L;
	public int seqno 		= -1;
	public Date timestamp 	= null;
	public short pulse 		= -1;
	public int heartbeats 	= -1;
	
	public HeartRateRec ( int s, Date t, short p, int h) {
		seqno = s;
		timestamp = t;
		pulse = p;
		heartbeats = h;
	}
}
