package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class RecordingFile {
	private String fileName, displayName;
	private File datDir, csvDir, pngDir;
	private Context myc;
	public static final String datExtension = ".dat";
	public static final String csvExtension = ".csv";
	public static final String pngExtension = ".png";
	public static final String EXTRA_RECFILENAME = "com.szabolcs.szijarto.sleepguard.recordingdatfilename";

	// constructor getting only a context
	public RecordingFile (Context c) {
		myc = c;
		setDirs();
	}
	
	// constructor getting a .dat filename
	public RecordingFile (Context c, String dfn) {
		myc = c;
		setDirs();
		setFileNameFromDat(dfn);
	}

	// constructor getting start and end timestamps of the recording
	public RecordingFile (Context c, Date d1, Date d2) {
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
		fileName = s.substring(0, s.length() - RecordingFile.datExtension.length());
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
	
	public void deleteFiles(boolean delDat, boolean delCsv, boolean delPng) {
		File f;
		if (delDat) {
			f = new File(getDatFullPath());
			f.delete();
		}
		if (delCsv) {
			f = new File(getCsvFullPath());
			f.delete();
		}
		if (delPng) {
			f = new File(getPngFullPath());
			f.delete();
		}
	}

	public void refreshFiles (boolean refreshCsv, boolean refreshPng) {
		Recording r = deserializeRecording();
		if (refreshCsv) {
			saveCsv(r);
		}
		if (refreshPng) {
			savePng(r);
		}
	}
	
	public void serializeRecording(Recording r) {
		// serialize object r to the dat file
		try {
			FileOutputStream fout = new FileOutputStream(getDatFullPath());
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(r);
			oos.close();
			fout.close();
		} catch(IOException i) {
			Toast.makeText(myc, "DAT save error: "+i.toString(), Toast.LENGTH_SHORT).show();
			i.printStackTrace();
		}
	}
	
	public Recording deserializeRecording() {
		try {
			FileInputStream fileIn = new FileInputStream(getDatFullPath());
			ObjectInputStream in = new ObjectInputStream(fileIn);
	        Recording r = (Recording) in.readObject();
	        in.close();
	        fileIn.close();
		    return r;
	    } catch(IOException i) {
			Toast.makeText(myc, "Exception when deserializing Recording: "+i.toString(), Toast.LENGTH_SHORT).show();
			i.printStackTrace();
	        return null;
	    } catch(ClassNotFoundException c) {
			Toast.makeText(myc, "Exception when deserializing Recording: "+c.toString(), Toast.LENGTH_SHORT).show();
			c.printStackTrace();
	        return null;
	    }
	}

	public void saveCsv(Recording r) {
		try {
			File f = new File(getCsvDir(), getCsvFileName());
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			r.dumpToCsv(w);
			w.close();
		} catch (IOException i) {
			Toast.makeText(myc, "CSV save error: "+i.toString(), Toast.LENGTH_SHORT).show();
			i.printStackTrace();
		}
	}
	
	public void savePng(Recording r) {
		try {
			File f = new File(getPngDir(), getPngFileName());
			FileOutputStream of = new FileOutputStream(f);
			r.drawChartBitmap();
			r.dumpToPng(of);
			of.close();
		} catch (IOException i) {
			Toast.makeText(myc, "PNG save error: "+i.toString(), Toast.LENGTH_SHORT).show();
			i.printStackTrace();
		}
	}

}
