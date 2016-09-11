package com.szabolcs.szijarto.sleepguard;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class RecordingListView extends ExpandableListView implements OnItemClickListener,	OnItemLongClickListener {

    ExpandableListAdapter myAdapter;
    List<String> myListTitles;
    HashMap<String, List<String>> myListItems;

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

    private HashMap<String, List<String>> getData(Context c) {
        RecordingFile t;
        File[] datfiles;

        HashMap<String, List<String>> listDetail = new HashMap<String, List<String>>();

        //TODO recordingList.clear();

        // find dat files
        t = new RecordingFile(c);
        datfiles = t.getDatDir().listFiles(new FilenameFilter() {
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

        // fill expandable list
        for (int i = 0; i < datfiles.length; i++) {
            t = new RecordingFile(c, datfiles[i].getName());
            List<String> listChildrenItem = new ArrayList<String>();
            listChildrenItem.add("Number of peaks: ");
            listChildrenItem.add("Total peak duration: ");
            listChildrenItem.add("Max peak BPM: ");
            listChildrenItem.add("Total duration: ");
            listChildrenItem.add("Peaks per hour: ");
            listChildrenItem.add("Weighted hourly peak score: ");
            listDetail.put(t.getDisplayName(), listChildrenItem);
        }

        return listDetail;
    }

    public void refresh(Context c) {

        // TODO recordingList.clear();

        myListItems = getData(c);
        myListTitles = new ArrayList<String>(myListItems.keySet());
        myAdapter = new RecordingListViewAdapter(c, myListTitles, myListItems);
        setAdapter(myAdapter);

        /* TODO OLD set adapter
        ArrayAdapter<RecordingFile> adapter = new ArrayAdapter<RecordingFile>(
                c, android.R.layout.simple_list_item_1, recordingList);
        setAdapter(adapter);
        */
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // TODO this never gets invoked since the class has been refactored to an ExpandableListView
        // determine png file name for the item that was clicked
        RecordingFile rf = (RecordingFile) parent.getAdapter().getItem(position);
        Intent recordingIntent = new Intent(getContext(), Activity_ShowRecording.class);
        recordingIntent.putExtra(RecordingFile.EXTRA_RECORDINGFILEOBJECT, rf);
        getContext().startActivity(recordingIntent);
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
                RecordingListViewAdapter a = (RecordingListViewAdapter) getAdapter();
                RecordingFile rf = new RecordingFile(getContext(), (String) a.getGroup(position));
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
