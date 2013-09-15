package com.szabolcs.szijarto.sleepguard;

public class RecordingListItem {
	private String datFileName, displayName;
	
	public RecordingListItem (String s) {
		datFileName = s;

		// get start and end timestamp from the filename and format it for display
		displayName = s.substring(11,15)+"."+s.substring(15,17)+"."+s.substring(17,19)+" "+
				s.substring(19,21)+":"+s.substring(21,23)+":"+s.substring(23,25);
	}
	
	public String getDatFileName() {
		return datFileName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String toString() {
		return displayName;
	}

	public void delete() {
		
	}
	
}
