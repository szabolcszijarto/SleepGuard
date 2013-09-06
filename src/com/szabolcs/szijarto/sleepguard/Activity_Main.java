package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
//import android.widget.Toast;

public class Activity_Main extends Activity implements OnItemClickListener {

	static private HeartRateWatcher watcher;
	File[] datfiles = null;
	LinkedList<String> datfilenames = null;
	
	private TextView statusTextView, currentPulseTextView, startTimeTextView, endTimeTextView, totalTimeTextView, numPeaksTextView, timePeaksTextView;
	public Button connectButton;
	public ImageButton startStopButton;
	private ListView recordingListView;

	public final static String EXTRA_RECFILENAME = "com.szabolcs.szijarto.sleepguard.recordingfilename";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		
		// create and initialise new HeartRateWatcher instance
		watcher = new HeartRateWatcher(this);
		connectButton.setText("Connect"); // starting up in disconnected state, hence button label says "Connect"
		
		// Fill Recording ListView
		refreshRecordingList();
	}

	protected void refreshRecordingList () {
		datfiles = getExternalFilesDir(null).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(".dat" /* should be Recording.datExtension */ )); 
			}
		});
		// here, datfiles should be sorted in reverse order so that latest recording appears on top
		datfilenames = new LinkedList<String>(); // the old list becomes garbage
		String s;
		for (int i=0; i<datfiles.length; i++) {
			s = datfiles[i].getName();
			datfilenames.add( 
					s.substring(11,15)+"."+s.substring(15,17)+"."+s.substring(17,19)+" "+
							s.substring(19,21)+":"+s.substring(21,23)+":"+s.substring(23,25)				//+" ("+s+")"
					);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datfilenames);
		recordingListView.setAdapter(adapter);
		recordingListView.setOnItemClickListener(this); 
	}
	
	// this runs when the user clicks on a recording in the list
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent i = new Intent(this, Activity_ShowRecording.class);
		String dfn = datfiles[position].getName();
		String filename = dfn.substring(0, dfn.length()-Recording.datExtension.length())+Recording.pngExtension;
		i.putExtra(EXTRA_RECFILENAME, filename); 
//		Toast.makeText(this, "Pos = "+position+" Extra = "+i.getStringExtra(EXTRA_RECFILENAME), Toast.LENGTH_LONG).show();
		// open a ShowRecording activity passing the name of the png file to show in an intent
		startActivity(i);
	}
		
	protected void findViews() {
		connectButton = (Button) findViewById(R.id.connectButton);
		statusTextView = (TextView) findViewById(R.id.statusTextView);
		startStopButton = (ImageButton) findViewById(R.id.startStopButton);
		currentPulseTextView = (TextView) findViewById(R.id.currentPulseTextView);
		startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
		endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
		totalTimeTextView = (TextView) findViewById(R.id.totalTimeTextView);
		numPeaksTextView = (TextView) findViewById(R.id.numPeaksTextView);
		timePeaksTextView = (TextView) findViewById(R.id.timePeaksTextView);
		recordingListView = (ListView) findViewById(R.id.recordingListView);
	}
	
	public void setStatus ( String s ) {
		statusTextView.setText(s);
	}

	public void setPulse ( String s ) {
		currentPulseTextView.setText(s);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// connect-disconnect button onClick() handler
	public void connDisconn(View view) {
		if ( watcher.isConnected() ) {
			watcher.disconnect(false);
		} else {
			watcher.connect();
		}
	}
	
	// start-stop button onClick() handler
	public void startStop(View view) {
		if (watcher.isRunning()) {
			watcher.stop();
			refreshRecordingList();
		} else {
			watcher.start();
		}
		// update watcher labels
		startTimeTextView.setText(watcher.getTimeStarted());
		endTimeTextView.setText(watcher.getTimeStopped());
		totalTimeTextView.setText(watcher.getTimeElapsed());
		numPeaksTextView.setText(watcher.getNumPeaks());
		timePeaksTextView.setText(watcher.getTimePeaks());
	}
}
