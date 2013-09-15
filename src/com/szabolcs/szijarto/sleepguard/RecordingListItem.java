package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

public class RecordingListItem {
	private String fileName, displayName;
	private File datDir, csvDir, pngDir;
	private Context myc;
	public static final String datExtension = ".dat";
	public static final String csvExtension = ".csv";
	public static final String pngExtension = ".png";
	public static final String EXTRA_RECFILENAME = "com.szabolcs.szijarto.sleepguard.recordingdatfilename";

	// constructor getting only a context
	public RecordingListItem (Context c) {
		myc = c;
		setDirs();
	}
	
	// constructor getting a .dat filename
	public RecordingListItem (Context c, String dfn) {
		myc = c;
		setDirs();
		setFileNameFromDat(dfn);
	}

	// constructor getting start and end timestamps of the recording
	public RecordingListItem (Context c, Date d1, Date d2) {
		myc = c;
		setDirs();
		setFileNameFromTimestamps(d1, d2);
	}
	
	private void setDirs() {
		datDir = myc.getExternalFilesDir(null);
		csvDir = myc.getExternalFilesDir(null);
		pngDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	}
	
	public void setFileNameFromDat(String s) {
		fileName = s.substring(0, s.length() - RecordingListItem.datExtension.length());
		setDisplayName();
	}

	public void setFileNameFromTimestamps(Date timeStarted, Date timeStopped) {
		SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddHHmmss", Locale.US);
		fileName = "sleepguard_" + ft.format(timeStarted) + "_" +ft.format(timeStopped);
		setDisplayName();
	}

	private void setDisplayName() {
		if (fileName != null) {
			String s = fileName;
			// get start and end timestamp from the filename and format it for display
			displayName = s.substring(11,15)+"."+s.substring(15,17)+"."+s.substring(17,19)+" "+
					s.substring(19,21)+":"+s.substring(21,23)+":"+s.substring(23,25);
		} else {
			displayName = "";
		}
	}
	
	public String getDisplayName() { return displayName; }
	public String toString() 	   { return displayName; }
	public String getDatFileName() { return fileName+datExtension; }
	public String getCsvFileName() { return fileName+csvExtension; }
	public String getPngFileName() { return fileName+pngExtension; }
	public File getDatDir()        { return datDir; }
	public File getCsvDir() 	   { return csvDir; }
	public File getPngDir()        { return pngDir; }
	public String getDatFullPath() { return datDir.toString()+"/"+getDatFileName(); } 
	public String getCsvFullPath() { return csvDir.toString()+"/"+getCsvFileName(); } 
	public String getPngFullPath() { return pngDir.toString()+"/"+getPngFileName(); } 
	
	public void deleteFiles() {
		File f;
		f = new File(getDatFullPath());
		f.delete();
		f = new File(getCsvFullPath());
		f.delete();
		f = new File(getPngFullPath());
		f.delete();
	}

}
