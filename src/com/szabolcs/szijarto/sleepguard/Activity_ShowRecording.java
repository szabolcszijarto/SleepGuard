package com.szabolcs.szijarto.sleepguard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Activity_ShowRecording extends Activity {

	private RecordingFile myrf;
	private Recording myrec;
	private Bitmap mybitmap;
	private int currPeakInd, maxPeakInd;		// currPeakind goes from 1..maxPeakInd 
	private Button firstButton, backButton, fwdButton, lastButton;
	private static int NOPEAKS = -999;

	private TextView recordingNameTextView;
	private TextView peakCounterTextView;
	private TextView durPeaksTextView, maxBpmTextView;
	private ImageView recordingImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showrecording);
		findViews();
		init();
		showPeakByIndex(currPeakInd);
	}
	
	private void init() {
		myrf = (RecordingFile) getIntent().getSerializableExtra(RecordingFile.EXTRA_RECORDINGFILEOBJECT);
		myrf.setContext(this);
		myrec = myrf.deserializeRecording();
		recordingNameTextView.setText(myrf.getDisplayName());
		maxPeakInd = myrec.getPeaks_cnt();
		if ( maxPeakInd == 0 ) {
			// no peaks, display default image
			currPeakInd = NOPEAKS;
			peakCounterTextView.setText("0 peaks");
			firstButton.setEnabled(false);
			backButton.setEnabled(false);
			fwdButton.setEnabled(false);
			lastButton.setEnabled(false);
		} else {
			currPeakInd = 1;
		}
	}

	public void firstPeak(View v) {
		if ( currPeakInd == NOPEAKS ) return;
		showPeakByIndex(1);
	}

	public void backPeak(View v) {
		if ( currPeakInd == NOPEAKS ) return;
		showPeakByIndex(currPeakInd-1);
	}

	public void fwdPeak(View v) {
		if ( currPeakInd == NOPEAKS ) return;
		showPeakByIndex(currPeakInd+1);
	}

	public void lastPeak(View v) {
		if ( currPeakInd == NOPEAKS ) return;
		showPeakByIndex(maxPeakInd);
	}

	public void gotoPeak(View v) {
		if ( currPeakInd == NOPEAKS ) return;
		;
	}
	
	private void findViews() {
		recordingNameTextView = (TextView) findViewById(R.id.recording_name);
		peakCounterTextView = (TextView) findViewById(R.id.peak_counter);
		durPeaksTextView = (TextView) findViewById(R.id.dur_value);
		maxBpmTextView = (TextView) findViewById(R.id.max_bpm_value);
		recordingImageView = (ImageView) findViewById(R.id.recording_image);
		firstButton = (Button) findViewById(R.id.button_first);
		backButton = (Button) findViewById(R.id.button_back);
		fwdButton = (Button) findViewById(R.id.button_fwd);
		lastButton = (Button) findViewById(R.id.button_last);
	}
	
	private void showPeakByIndex(int i) {
		if ( (i == NOPEAKS) || (i<1) || (i>maxPeakInd) ) {
			showImage();
			return;
		}
		currPeakInd = i;
		peakCounterTextView.setText(currPeakInd+" of "+maxPeakInd);
		Peak p = myrec.getPeaks().get(currPeakInd-1);	// one less because List index starts from 0
		durPeaksTextView.setText(p.getDurationString());
		maxBpmTextView.setText(String.valueOf((int)p.max_pulse));
		showImage(p);
	}
	
	private void showImage() {
		mybitmap = myrec.getChartBitmap();
		recordingImageView.setImageBitmap(mybitmap);
		recordingImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	}
	
	private void showImage(Peak p) {
		mybitmap = myrec.getChartBitmap(p);
		recordingImageView.setImageBitmap(mybitmap);
		recordingImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		;
	}
}
