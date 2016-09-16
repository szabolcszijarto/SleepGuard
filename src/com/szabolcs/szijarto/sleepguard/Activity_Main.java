package com.szabolcs.szijarto.sleepguard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Activity_Main extends Activity {

	private static final String TAG = "Activity_Main";
	static private HeartRateWatcher watcher;

	private TextView connStatusTextView, recStatusTextView, elapsTimeTextView, currentPulseTextView;
	// TODO why are these public?
	public ImageButton connectButton, startStopButton;

	public RecordingListView getRecordingListView() {
		return recordingListView;
	}

	private RecordingListView recordingListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
        Log.i(TAG, "onCreate: findViews() completed");
		
		// TODO Is this really unnecessary since the constructor already takes care of it?
		// recordingListView.init();

		// create and initialize new HeartRateWatcher instance
		watcher = new HeartRateWatcher(this);
        Log.i(TAG, "onCreate: watcher constructed");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void setConnStatus ( String s ) {
		connStatusTextView.setText(s);
	}

	public void setRecStatus ( String s ) {
		recStatusTextView.setText(s);
	}

	public void setPulse ( String s ) {
		currentPulseTextView.setText(s);
	}

	public void setElapsTime ( String s ) { elapsTimeTextView.setText(s); }

	// connect-disconnect button onClick() handler
	public void connDisconnOnClickHandler(View view) {
		if ( watcher.isConnected() ) {
			watcher.disconnect(false);
		} else {
			watcher.connect();
		}
	}
	
	// start-stop button onClick() handler
	public void startStop(View view) {
		if (watcher.isRunning()) {
			RecordingListItem rli = watcher.stop();
			recordingListView.addItem(rli);
		} else {
			watcher.start();
		}
	}

	protected void findViews() {
		connectButton = (ImageButton) findViewById(R.id.connectButton);
		connStatusTextView = (TextView) findViewById(R.id.connStatusTextView);
		startStopButton = (ImageButton) findViewById(R.id.startStopButton);
		recStatusTextView = (TextView) findViewById(R.id.recStatusTextView);
		elapsTimeTextView = (TextView) findViewById(R.id.elapsTimeTextView);
		currentPulseTextView = (TextView) findViewById(R.id.currentPulseTextView);
		recordingListView = (RecordingListView) findViewById(R.id.recordingListView);
	}
	
}
