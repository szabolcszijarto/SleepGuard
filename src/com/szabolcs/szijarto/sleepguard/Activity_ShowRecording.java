package com.szabolcs.szijarto.sleepguard;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Activity_ShowRecording extends Activity {

	private TextView recordingNameTextView;
	private ImageView recordingImageView;
	private Bitmap b = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showrecording);
		findViews();
		loadImage();
	}
	
	protected void findViews() {
		recordingNameTextView = (TextView) findViewById(R.id.recordingName);
		recordingImageView = (ImageView) findViewById(R.id.recordingImage);
	}
	
	private void loadImage() {
		// determine full path of PNG file and show label
		String pngfilename = getExternalFilesDir(null).toString()+"/"+getIntent().getStringExtra(Recording.EXTRA_RECFILENAME);
		recordingNameTextView.setText(pngfilename);
		// decode and show image
		b = BitmapFactory.decodeFile(pngfilename, null);
		if (b==null) {
			Toast.makeText(this, "Bitmap couldn't be decoded: "+pngfilename, Toast.LENGTH_SHORT).show();
		}
		recordingImageView.setImageBitmap(b);
		recordingImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	}
}
