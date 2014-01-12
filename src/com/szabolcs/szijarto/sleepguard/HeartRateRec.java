package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class HeartRateRec implements java.io.Serializable {
	private static final long serialVersionUID = 19742001L;
	public final int seqno;
	public final Date timestamp;
	public final byte pulse;
	public final int heartbeats;
	
	public HeartRateRec ( int s, Date t, byte p, int h) {
		seqno = s;
		timestamp = t;
		pulse = p;
		heartbeats = h;
	}
}
