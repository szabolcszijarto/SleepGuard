package com.szabolcs.szijarto.sleepguard;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class SleepChart {
	
	private LinkedList<HeartRateRec> hrrl;

	private int x_sec_per_pixel;				// horizontal resolution of chart: normally 10
	private int chart_width, chart_height;		// vertical size of the chart in BPM
	private int x_border, y_border, header_height;
	private int x_size, y_size, min_width; 
	private int x_origo, y_origo;
	private int marker_size;
	private int max_bpm, min_bpm, bpm_minor, bpm_major;
	private int text_size_small, text_height_small;
	private long elapsed_secs;
	private HeartRateRec rec_first, rec_last;
	private int n;
	private Bitmap b;
	private Canvas c;
	private Paint p;
	
	public SleepChart(LinkedList<HeartRateRec> hrrlst) {
		hrrl = hrrlst;
		draw();
	}

	public void setHRRList(LinkedList<HeartRateRec> hrrlst) {
		hrrl = hrrlst;
		draw();
	}

	public void draw() {
		init();
		draw_background();
		draw_x_axis();
		draw_y_axis();
		draw_chart();
		draw_header();
	}

	private void init() {
		// the chart will only be created up to this position, in case values are still being added in parallel
		n = hrrl.lastIndexOf(rec_last);

		// calculate dimensions
		x_sec_per_pixel = 10;
		if (elapsed_secs < 600) {				// but we reduce it to 1 if recording is shorter than 10 min
			x_sec_per_pixel = 1;
		}
		min_bpm = 50;
		max_bpm = 250;
		bpm_minor = 10;
		bpm_major = 50;
		rec_first = hrrl.getFirst();
		rec_last = hrrl.getLast();
		elapsed_secs = ( rec_last.timestamp.getTime() - rec_first.timestamp.getTime() ) / 1000;
		x_border = 20;
		y_border = 15;
		header_height = 50;
		chart_width = (int)Math.ceil(elapsed_secs / x_sec_per_pixel);
		chart_height = max_bpm;
		min_width = 300;
		x_size = chart_width+2*x_border;
		y_size = chart_height+4*y_border+header_height;
		if (x_size<min_width) { x_size = min_width; }
		x_origo = x_border;
		y_origo = y_size-y_border;
		marker_size = 2;
		text_size_small = 8;
		text_height_small = text_size_small + 2;

		// create bitmap, canvas and paint
		b = Bitmap.createBitmap( x_size, y_size, Bitmap.Config.ARGB_8888);
		c = new Canvas (b);
		p = new Paint();
	}

	private void draw_background() {
		p.setColor(Color.LTGRAY);
		c.drawPaint(p);
	}

	private void draw_header() {
		p.setColor(Color.BLACK);
		c.drawRect(x_border, y_border, x_size-x_border, y_border+header_height, p);
		p.setTextSize(text_size_small);
		SimpleDateFormat ft;
		ft = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss", Locale.US);
		print_header_text(1, "Timeframe : "+ft.format(rec_first.timestamp.getTime()) +" - "+ ft.format(rec_last.timestamp.getTime()) );
		ft = new SimpleDateFormat ("HH:mm:ss", Locale.US);
		print_header_text(2, "Duration  : "+ft.format(rec_last.timestamp.getTime() - rec_first.timestamp.getTime()) );
	}

	private void print_header_text(int n, String s) {
		p.setColor(Color.BLACK);
		c.drawText(s, x_origo+x_border/2, y_border+y_border/2+n*text_height_small, p);
	}
	
	private void draw_x_axis() {
		p.setColor(Color.BLACK);
		c.drawLine(x_origo-1, y_origo+1, x_origo+chart_width, y_origo+1, p);

	}
	
	private void draw_y_axis() {
		p.setColor(Color.BLACK);
		c.drawLine(x_origo-1, y_origo+1, x_origo-1, y_origo-chart_height-y_border, p);
		
		// draw markers and label
		for (int mind = min_bpm; mind <= max_bpm ; mind=mind+bpm_minor) {
			p.setColor(Color.BLACK);
			p.setTextSize(text_size_small);
			if (mind%bpm_major == 0) {
				c.drawLine(x_origo-1-2*marker_size, y_origo+1-mind, x_origo-1+2*marker_size, y_origo+1-mind, p);	// Y axis long marker
				c.drawText(String.valueOf(mind), 3, y_origo+1-mind, p);
			} else {
				c.drawLine(x_origo-1-marker_size, y_origo+1-mind, x_origo-1+marker_size, y_origo+1-mind, p);		// Y axis short marker
			}
			p.setColor(Color.GRAY);
			c.drawLine(x_origo-1+2*marker_size, y_origo+1-mind, x_origo-1+chart_width, y_origo+1-mind, p);			// horizontal line
		}
	}

	private void draw_chart() {
		int offset;
		boolean started;
		HeartRateRec r;
		ListIterator<HeartRateRec> l = hrrl.listIterator(0) ;
		Path t = new Path();
		started = false;
		while (l.hasNext() && (l.nextIndex()<n)) {		// stop if no more records, or at rec_last, just in case extra records were added to the list in the meantime
			r = l.next();
			offset = (int) Math.floor( (r.timestamp.getTime() - rec_first.timestamp.getTime()) /1000.0 /x_sec_per_pixel );
			if (!started) {
				t.setLastPoint(x_origo+offset, y_origo-r.pulse);
				started = true;
			}
			t.lineTo(x_origo+offset, y_origo-r.pulse);
		}
		p.setColor(Color.argb(255, 0, 200, 0)); // dark green, non-transparent
		p.setStyle(Paint.Style.STROKE);
		c.drawPath(t, p);
	}
	
	public Bitmap getBitmap() {
		return b;
	}
}
