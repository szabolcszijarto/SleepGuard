package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

public class Activity_Main extends Activity implements OnItemClickListener, OnItemLongClickListener {

	static private HeartRateWatcher watcher;

	private TextView statusTextView, currentPulseTextView, startTimeTextView, endTimeTextView, totalTimeTextView, numPeaksTextView, timePeaksTextView;
	public Button connectButton;
	public ImageButton startStopButton;
	private ListView recordingListView;

	public final static String EXTRA_RECFILENAME = "com.szabolcs.szijarto.sleepguard.recordingfilename";

	private File[] datfiles = null;

	private ActionMode mActionMode = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
		
		// create and initialize new HeartRateWatcher instance
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

		// TO-DO: here, datfiles should be sorted in reverse order so that latest recording appears on top
		LinkedList<String> datfilenames = new LinkedList<String>();
		String s;
		for (int i=0; i<datfiles.length; i++) {
			s = datfiles[i].getName();
			// get start and end timestamp from filename and fill the list
			datfilenames.add( 
					s.substring(11,15)+"."+s.substring(15,17)+"."+s.substring(17,19)+" "+
							s.substring(19,21)+":"+s.substring(21,23)+":"+s.substring(23,25)				//+" ("+s+")"
					);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datfilenames);
		recordingListView.setAdapter(adapter);
		recordingListView.setOnItemClickListener(this);
		recordingListView.setOnItemLongClickListener(this);
	}
	
	// this runs when the user clicks on a recording in the list
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent i = new Intent(this, Activity_ShowRecording.class);
		String dfn = datfiles[position].getName();
		String filename = dfn.substring(0, dfn.length()-Recording.datExtension.length())+Recording.pngExtension;
		i.putExtra(EXTRA_RECFILENAME, filename); 
		//Toast.makeText(this, "Pos = "+position+" Extra = "+i.getStringExtra(EXTRA_RECFILENAME), Toast.LENGTH_LONG).show();

		// open a ShowRecording activity passing the name of the png file to show in an intent
		startActivity(i);
	}
		

	public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, long id) {
        if (mActionMode != null) {
            return false;
        }
        
    	final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

    	    // Called when the action mode is created; startActionMode() was called
    	    @Override
    	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    	        // Inflate a menu resource providing context menu items
    	        MenuInflater inflater = mode.getMenuInflater();
    	        inflater.inflate(R.menu.listview_item_context, menu);
    	        return true;
    	    }

    	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    	    // may be called multiple times if the mode is invalidated.
    	    @Override
    	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    	        return false; // Return false if nothing is done
    	    }

    	    // Called when the user selects a contextual menu item
    	    @Override
    	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    	        switch (item.getItemId()) {
    	            case R.id.item_delete:
    	            	// TO-DO
    	            	String s = parent.getAdapter().getItem(position).toString();
    	        		Toast.makeText(v.getContext(), "Pos = "+position+" name = "+s, Toast.LENGTH_LONG).show();
    	        		// TO-DO : delete the files and refresh the listview
    	                mode.finish(); // Action picked, so close the CAB
    	                return true;
    	            default:
    	                return false;
    	        }
    	    }

    	    // Called when the user exits the action mode
    	    @Override
    	    public void onDestroyActionMode(ActionMode mode) {
    	        mActionMode = null;
    	    }
    	};

        // Start the CAB using the ActionMode.Callback defined above
        mActionMode = startActionMode(mActionModeCallback);
        v.setSelected(true);
        return true;
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

		// peaks not yet supported
		//numPeaksTextView.setText(watcher.getNumPeaks());
		//timePeaksTextView.setText(watcher.getTimePeaksStr());
	}
}
