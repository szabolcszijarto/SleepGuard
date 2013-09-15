package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class RecordingListView extends ListView implements OnItemClickListener,	OnItemLongClickListener {

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
		RecordingListItem t;
		File[] datfiles;
		recordingList.clear();
		// find dat files
		t = new RecordingListItem(c);
		datfiles = t.getDatDir().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith(RecordingListItem.datExtension)); 
			}
		});
		// sort by date, descending
		Arrays.sort(datfiles, new Comparator<File>() {
		    public int compare(File f1, File f2) {
		        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		    }
		});
		// fill recording list with RecordingListItem objects
		for (int i=0; i<datfiles.length; i++) {
			t = new RecordingListItem(c, datfiles[i].getName());
			recordingList.add(t);
		}
		// set adapter
		ArrayAdapter<RecordingListItem> adapter = new ArrayAdapter<RecordingListItem>(
				c, android.R.layout.simple_list_item_1 , recordingList);
		setAdapter(adapter);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// determine png file name for the item that was clicked
		RecordingListItem ri = (RecordingListItem) parent.getAdapter().getItem(position);
		String pngfn = ri.getPngFullPath();
		//if png file doesn't exist, but dat file does, then recreate png
		if ( (!(new File(pngfn)).exists()) && (new File(ri.getDatFullPath()).exists()) ) {
			Recording r = ri.deserializeRecording();
			ri.savePng(r);
		}
		if ( (new File(pngfn)).exists() ) {
			// create intent and show image using gallery
			Intent photoIntent = new Intent(Intent.ACTION_VIEW);
			photoIntent.setDataAndType(Uri.fromFile(new File(pngfn)),"image/*");
			parent.getContext().startActivity(photoIntent);
		}
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
    	        		//delete the files and refresh the listview
    	            	@SuppressWarnings("unchecked")
						ArrayAdapter<RecordingListItem> a = (ArrayAdapter<RecordingListItem>) parent.getAdapter();
    	            	RecordingListItem ri = (RecordingListItem) a.getItem(position);
    	            	ri.deleteFiles();
    	            	a.remove(ri);
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
