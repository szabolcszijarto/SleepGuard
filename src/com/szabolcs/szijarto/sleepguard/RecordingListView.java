package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
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
import android.widget.Toast;

public class RecordingListView extends ExpandableListView implements OnItemClickListener,	OnItemLongClickListener {

    private RecordingListViewAdapter myAdapter;
    private ArrayList<RecordingListItem> rlis;

    private ActionMode mActionMode = null;
    private static final String TAG = "RecordingListView";

    public RecordingListView(Context context) {
        super(context);
        if (!isInEditMode()) init();
    }

    public RecordingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) init();
    }

    public RecordingListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) init();
    }

    private void init() {
        rlis = new ArrayList<RecordingListItem>();
        // TODO why does this toast not work?
        Toast.makeText(getContext(), "Loading data, please wait...", Toast.LENGTH_SHORT).show();
        for ( RecordingListItem rli : getRecordingListItemsFromFiles() ) { addItem(rli); }
        setOnItemClickListener(this);
        setOnItemLongClickListener(this);
    }

    private ArrayList<RecordingListItem> getRecordingListItemsFromFiles() {
        Recording r;
        RecordingFile rf;
        File[] datfiles;
        ArrayList<RecordingListItem> local_rlis = new ArrayList<RecordingListItem>();

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

        // create list of RecordingListItem objects
        for (int i = 0; i < datfiles.length; i++) {
            rf = new RecordingFile(c, datfiles[i].getName());
            r = rf.deserializeRecording();
            RecordingListItem rli = new RecordingListItem(r,rf);
            if ( nameAlreadyExists(rli.getDisplayName()) ) {
                makeNameUnique(rli);
                // persist dat file with new name
                Toast.makeText(getContext(), "Saving data, please wait...",Toast.LENGTH_SHORT).show();
                r.setDisplayName(rli.getDisplayName());
                rf.serializeRecording(r);
            }
            local_rlis.add(rli);
        }

        return local_rlis;
    }

    private boolean nameAlreadyExists(String name) {
        for ( RecordingListItem r : rlis ) {
            if (r.getDisplayName().compareTo(name) == 0) { return true; }
        }
        return false;
    }

    private void makeNameUnique(RecordingListItem rli) {
        String name = rli.getDisplayName();
        if ( !nameAlreadyExists(name) ) return;

        int i;
        String suffix = name.substring( name.length() - Recording.nameNumericSuffix.length() );
        try {
            i = Integer.parseInt(suffix);
            // numeric suffix found
        } catch (NumberFormatException e) {
            // no numeric suffix found at the end, append the default one
            suffix = Recording.nameNumericSuffix;
            i = Integer.parseInt(suffix);
            name = name + "_" + suffix ;
        }
        // check again and increment until name becomes unique...
        while ( nameAlreadyExists(name) ) {
            i++;
            name = name.substring( 0, name.length() - Recording.nameNumericSuffix.length() ) + String.format(Locale.US, "%03d", i);
        }
        rli.setDisplayName(name);
    }

    public void addItem(RecordingListItem rli) {

        // make the name unique
        makeNameUnique(rli);
        rlis.add(rli);

        String longname = rli.getLongDisplayName();

        if (myAdapter == null) {
            // Initialize the adapter with the beginner group
            List<String> titles = new ArrayList<String>();
            titles.add(longname);
            HashMap<String, List<String>> details = new HashMap<String, List<String>>();
            details.put(longname, rli.getFormattedAttributes());
            myAdapter = new RecordingListViewAdapter(getContext(), titles, details);
            setAdapter(myAdapter);
        } else {
            // Add the new group to the existing adapter
            myAdapter.addGroup(longname, rli.getFormattedAttributes());
        }

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
                        // TODO the rli and adapter group should be refreshed
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
