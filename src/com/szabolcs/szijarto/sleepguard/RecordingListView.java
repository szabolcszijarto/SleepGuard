package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;

public class RecordingListView extends ExpandableListView implements OnItemClickListener,	OnItemLongClickListener {

    RecordingListViewAdapter myAdapter;
    ArrayList<RecordingListItem> rlis;

    private ActionMode mActionMode = null;
    private static final String TAG = "RecordingListView";

    public RecordingListView(Context context) {
        super(context);
        Log.i(TAG, "constructor #1 called");
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    public RecordingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "constructor #2 called");
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    public RecordingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG, "constructor #3 called");
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    private void register4clicks() {
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
        Log.i(TAG, "register4clicks() completed");
    }

    private void init() {
        Log.d(TAG, "init() called");

        // first, initialize the adapter with empty lists
        myAdapter = new RecordingListViewAdapter(getContext());
        setAdapter(myAdapter);

        rlis.clear();
        for ( RecordingListItem rli : getRecordingListItemsFromFiles() ) {
            addItem(rli);
        }

        Log.i(TAG, "init() completed");
    }

    private ArrayList<RecordingListItem> getRecordingListItemsFromFiles() {
        // TODO move this method to RecordingFile.java
        Recording r;
        RecordingFile rf;
        File[] datfiles;
        ArrayList<RecordingListItem> rlis = new ArrayList<RecordingListItem>();

        Context c = getContext();

        // find dat files
        rf = new RecordingFile(c);
        datfiles = rf.getDatDir().listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(RecordingFile.datExtension));
            }
        });

        // sort by date, descending
        Arrays.sort(datfiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        // create output list
        for (int i = 0; i < datfiles.length; i++) {
            rf = new RecordingFile(c, datfiles[i].getName());
            r = rf.deserializeRecording();
            RecordingListItem rli = new RecordingListItem(r,rf);
            rlis.add(rli);
        }

        return rlis;
    }

    public void addItem(RecordingListItem rli) {

        String name = rli.getDisplayName();

        // check for uniqueness, make the name unique if necessary
        if (rlis.contains(name)) {
            String suffix = name.substring( name.length() - Recording.nameSuffixLength );
            try {
                int i = Integer.parseInt(suffix);
                // suffix found, increment until it becomes unique...
            } catch (NumberFormatException e) {
                // no incrementable suffix found at the end, add an arbitrary one
                name = name + "_000";
            }

            // check again and increment until name becomes unique...
            suffix = name.substring( name.length() - Recording.nameSuffixLength );
            int i = Integer.parseInt(suffix);
            while ( rlis.contains(name)) {
                i++;
                name = name.substring( 0, name.length() - Recording.nameSuffixLength - 1 ) + "_" + String.format("%03d", i);
            }
        }

        rlis.add(rli);
        myAdapter.addGroup(name, rli.getFormattedAttributes());
        refreshView();
    }

    public void refreshView() {
        if ( myAdapter != null ) {
            synchronized (myAdapter) { myAdapter.notifyDataSetChanged(); }
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View v, final int position, long id) {
        return;
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
                @SuppressWarnings("unchecked")
                RecordingListViewAdapter a = (RecordingListViewAdapter) getExpandableListAdapter();
                String datFileName = rlis.get(position).getDatFileName();
                RecordingFile rf = new RecordingFile(getContext(), datFileName);
                switch (item.getItemId()) {
                    case R.id.item_delete:
                        //delete all the files and refresh the listview
                        rf.deleteFiles(true, true, true);
                        a.removeGroup(position);
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.item_refresh:
                        // refresh png and csv file for the recording
                        rf.refreshFiles(true, true);
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.item_view:
                        // view whole chart in external image viewer
                        String pngfn = rf.getPngFullPath();
                        File pngf = new File(pngfn);
                        //if png file doesn't exist, but dat file does, then recreate png
                        if ((!pngf.exists()) && (new File(rf.getDatFullPath()).exists())) {
                            // regenerate png and csv from dat
                            rf.refreshFiles(true, true);
                        }
                        if (pngf.exists()) {
                            // create intent and show image using gallery
                            Intent photoIntent = new Intent(Intent.ACTION_VIEW);
                            photoIntent.setDataAndType(Uri.fromFile(new File(pngfn)), "image/*");
                            parent.getContext().startActivity(photoIntent);
                        }
                        // TODO is this OK?
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.item_view_peaks:
                        // determine png file name for the item that was clicked
                        Intent recordingIntent = new Intent(getContext(), Activity_ShowRecording.class);
                        recordingIntent.putExtra(RecordingFile.EXTRA_RECORDINGFILEOBJECT, rf);
                        getContext().startActivity(recordingIntent);
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
