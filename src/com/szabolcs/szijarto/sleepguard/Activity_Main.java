package com.szabolcs.szijarto.sleepguard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class Activity_Main extends Activity {

	static private HeartRateWatcher watcher;

	private TextView statusTextView, currentPulseTextView, startTimeTextView, endTimeTextView, totalTimeTextView;
	public Button connectButton;
	public ImageButton startStopButton;
	private RecordingListView recordingListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		
		// Fill Recording ListView
		refreshRecordingList();

		// create and initialize new HeartRateWatcher instance
		watcher = new HeartRateWatcher(this);
		connectButton.setText("Connect"); // starting up in disconnected state, hence button label says "Connect"
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void refreshRecordingList () {
		recordingListView.refresh(this);
	}
	
	public void setStatus ( String s ) {
		statusTextView.setText(s);
	}

	public void setPulse ( String s ) {
		currentPulseTextView.setText(s);
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
		try {
			startTimeTextView.setText(watcher.getTimeStarted());
			endTimeTextView.setText(watcher.getTimeStopped());
			totalTimeTextView.setText(watcher.getTimeElapsed());
		} catch (GenericWatcherException gwe) {
		}
	}

	protected void findViews() {
		connectButton = (Button) findViewById(R.id.connectButton);
		statusTextView = (TextView) findViewById(R.id.statusTextView);
		startStopButton = (ImageButton) findViewById(R.id.startStopButton);
		currentPulseTextView = (TextView) findViewById(R.id.currentPulseTextView);
		startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
		endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
		totalTimeTextView = (TextView) findViewById(R.id.totalTimeTextView);
		recordingListView = (RecordingListView) findViewById(R.id.recordingListView);
	}
	
}
