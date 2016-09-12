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
    HashMap<String, List<String>> listViewItems;

    private ActionMode mActionMode = null;

    public RecordingListView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    public RecordingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    public RecordingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init();
            register4clicks();
        }
    }

    private void register4clicks() {
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
    }

    private void init() {
        listViewItems = new HashMap<String, List<String>>();
        refreshItemsFromFiles();
        List<String> myListTitles = new ArrayList<String>(listViewItems.keySet());
        myAdapter = new RecordingListViewAdapter(getContext(), myListTitles, listViewItems);
        setAdapter(myAdapter);
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

    public void refreshItemsFromFiles() {
        ArrayList<RecordingListItem> rlis = getRecordingListItemsFromFiles();
        listViewItems.clear();
        for ( RecordingListItem rli : rlis) {
            addItem(rli);
        }
    }

    public void addItem(RecordingListItem rli) {
        List<String> childItems = new ArrayList<String>();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        childItems.add( "Started:  " + ft.format(rli.getTimeStarted()) );
        childItems.add( "Finished: " + ft.format(rli.getTimeStopped()) );
        childItems.add( "Duration: " + rli.getTotalDurationInHours() + " hours");
        childItems.add( "Total " + rli.getNumberOfPeaks() + " peaks lasting " + rli.getTotalDurationOfPeaksInMinutes() + " minutes");
        childItems.add( "Max peak BPM: " + rli.getMaximumHeartRateDuringPeaks());
        int np = rli.getNumberOfPeaks();
        float td = rli.getTotalDurationInHours();
        if (td > 0) {
            childItems.add( "Average peaks per hour: " + (np/td) );
        } else {
            childItems.add( "Average peaks per hour: " + "n/a" );
        }
        childItems.add( "Weighted hourly peak score: " + rli.getWeightedHourlyPeakScore() );

        String name = rli.getDisplayName();
        // check for uniqueness, make the name unique if necessary
        if (listViewItems.containsKey(name)) {
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
            while ( listViewItems.containsKey(name)) {
                i++;
                name = name.substring( 0, name.length() - Recording.nameSuffixLength - 1 ) + "_" + String.format("%03d", i);
            }
        }

        listViewItems.put(name, childItems);
        refreshView();
    }

    public void refreshView() {
        //TODO this doesn't do anything...
        if ( myAdapter == null ) return;
        synchronized (myAdapter) {
            //myAdapter.notifyAll();
            myAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View v, final int position, long id) {
    };


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
                String recName = (String) a.getGroup(position);
                RecordingFile rf = new RecordingFile(getContext(), recName+".dat");
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
