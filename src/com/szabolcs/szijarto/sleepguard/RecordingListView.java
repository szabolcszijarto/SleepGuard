package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RecordingListView extends ListView implements OnItemClickListener,	OnItemLongClickListener {

	// TODO get rid of this...
	File[] datfiles = null;
	LinkedList<RecordingListItem> recordingList = new LinkedList<RecordingListItem>();	// list of .dat filenames containing serialized Recording objects
	private ActionMode mActionMode = null;

	public RecordingListView(Context context) {
		super(context);
		register4clicks();
	}

	public RecordingListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		register4clicks();
	}
	
	public RecordingListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		register4clicks();
	}
	
	private void register4clicks() {
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}
	
	public void refresh(Context c) {
		recordingList.clear();

		// find dat files
		datfiles = c.getExternalFilesDir(null).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(Recording.datExtension)); 
			}
		});

		// TODO: here, datfiles should be sorted in reverse order so that latest recording appears on top
		
		// fill recording list with RecordingListItem objects
		RecordingListItem t;
		for (int i=0; i<datfiles.length; i++) {
			t = new RecordingListItem(datfiles[i].getName());
			recordingList.add(t);
		}

		ArrayAdapter<RecordingListItem> adapter = new ArrayAdapter<RecordingListItem>(
				c, android.R.layout.simple_list_item_1 , recordingList);
		setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		
		// determine png file name for the item that was clicked
		String dfn = datfiles[position].getName();
		String filename = dfn.substring(0, dfn.length()-Recording.datExtension.length())+Recording.pngExtension;

		// create intent and call ShowRecording activity
		Intent i = new Intent(parent.getContext(), Activity_ShowRecording.class);
		i.putExtra(Recording.EXTRA_RECFILENAME, filename); 
		// TODO log.makeText(this, "Pos = "+position+" Extra = "+i.getStringExtra(EXTRA_RECFILENAME), Toast.LENGTH_LONG).show();
		parent.getContext().startActivity(i);
	}
		
	@Override
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
    	        		// TODO : delete the files and refresh the listview
    	            	String s = parent.getAdapter().getItem(position).toString();
    	        		Toast.makeText(v.getContext(), "Pos = "+position+" name = "+s, Toast.LENGTH_LONG).show();
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

}
