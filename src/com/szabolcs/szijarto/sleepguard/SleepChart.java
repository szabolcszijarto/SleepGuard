package com.szabolcs.szijarto.sleepguard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

public class SleepChart {
	
	private LinkedList<HeartRateRec> hrlist;
	private LinkedList<Peak> peaklist;
	private int x_sec_per_pixel;				// horizontal resolution of chart: normally 10
	private int chart_width, chart_height;		// vertical size of the chart in BPM
	private int x_border, y_border, header_height;
	private int x_size, y_size, min_width; 
	private int x_origo, y_origo;
	private int marker_size;
	private int max_bpm, min_bpm, bpm_minor, bpm_major;
	private int text_size_small;
	private int text_size_medium, text_height_medium;
	private long elapsed_secs;
	private int elapsed_hour, elapsed_min, elapsed_sec;
	private HeartRateRec rec_first, rec_last;
	private int max_ind;
	private short treshold;
	private Bitmap b;
	private Canvas c;
	private Paint p;
	
	public SleepChart(LinkedList<HeartRateRec> hrlst, LinkedList<Peak> peaklst, short t) {
		hrlist = hrlst;
		peaklist = peaklst;
		treshold = t;
		draw();
	}

	public void setHrList(LinkedList<HeartRateRec> hrlst) {
		hrlist = hrlst;
	}

	public void setPeakList(LinkedList<Peak> peaklst) {
		peaklist = peaklst;
	}
	
	public void draw() {
		init();
		draw_background();
		draw_x_axis();
		draw_y_axis();
		draw_treshold();
		draw_peaks();
		draw_chart();
		draw_header();
	}

	private void init() {
		min_bpm = 50;
		max_bpm = 250;
		bpm_minor = 10;
		bpm_major = 50;
		rec_first = hrlist.getFirst();
		rec_last = hrlist.getLast();
		
		// the chart will only be created up to this position, in case values are still being added in parallel
		max_ind = hrlist.lastIndexOf(rec_last);

		elapsed_secs = ( rec_last.timestamp.getTime() - rec_first.timestamp.getTime() ) / 1000;
		elapsed_hour = (int) (elapsed_secs / 3600 ); 
		elapsed_min = (int) ( (elapsed_secs-elapsed_hour*3600) / 60 );
		elapsed_sec = (int) (elapsed_secs-elapsed_hour*3600-elapsed_min*60) ;
		// calculate dimensions
		if (elapsed_secs < 600) {
			x_sec_per_pixel = 1;	// if total duration is < 10 minutes, 1 pixel = 1 sec (high horizontal resolution)
		} else {
			// TODO TESTING ONLY, OTHERWISE 5
			x_sec_per_pixel = 1;	// otherwise 1 pixel = 1 sec (lower horizontal resolution)
		}
		x_border = 20;
		y_border = 15;
		header_height = 50;
		chart_width = (int)Math.ceil(elapsed_secs / x_sec_per_pixel);
		chart_height = max_bpm;
		min_width = 380;
		x_size = chart_width+2*x_border;
		y_size = chart_height+4*y_border+header_height;
		if (x_size<min_width) { x_size = min_width; }
		x_origo = x_border;
		y_origo = y_size-y_border;
		marker_size = 2;
		text_size_small = 8;
		text_size_medium = 12;
		text_height_medium = text_size_medium + 3;

		// create bitmap, canvas and paint
		b = Bitmap.createBitmap( x_size, y_size, Bitmap.Config.ARGB_8888);
		c = new Canvas (b);
		p = new Paint();
	}

	private void draw_background() {
		p.setColor(Color.argb(255, 225, 225, 225)); // light gray, non-transparent
		c.drawPaint(p);
	}

	private void draw_treshold() {
		p.setColor(Color.argb(128, 100, 0, 0)); // red, semi-transparent
		c.drawLine(x_origo, y_origo-treshold, x_origo+chart_width, y_origo-treshold, p);
	}
	
	private void draw_peaks() {
		Peak peak;
		int x1, x2;
		
		p.setColor(Color.argb(128, 200, 0, 0)); // light red, semi-transparent
		p.setStyle(Style.FILL);
		
		ListIterator<Peak> l = peaklist.listIterator(0) ;
		while (l.hasNext()) {
			peak = l.next();
			x1 = (int) Math.floor( (peak.start_time.getTime() - rec_first.timestamp.getTime()) /1000.0 /x_sec_per_pixel );
			x2 = (int) Math.floor( (peak.end_time.getTime() - rec_first.timestamp.getTime()) /1000.0 /x_sec_per_pixel );
			Rect r = new Rect(x_origo+x1, y_origo-10, x_origo+x2, y_origo-chart_height+10);
			c.drawRect(r, p);
		}
	}
	
	private void draw_header() {
		p.setColor(Color.BLACK);
		c.drawRect(x_border, y_border, x_size-x_border, y_border+header_height, p);
		SimpleDateFormat ft;
		ft = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss", Locale.US);
		print_header_text(1, "Timeframe : "+ft.format(rec_first.timestamp.getTime()) +" - "+ ft.format(rec_last.timestamp.getTime()) );
		print_header_text(2, "Duration     : "+elapsed_hour+":"+elapsed_min+":"+elapsed_sec);
	}

	private void print_header_text(int n, String s) {
		p.setColor(Color.BLACK);
		p.setTextSize(text_size_medium);
		c.drawText(s, x_origo+x_border/2, y_border+y_border/2+n*text_height_medium, p);
	}
	
	private void draw_x_axis() {
		p.setColor(Color.BLACK);
		c.drawLine(x_origo, y_origo, x_origo+chart_width, y_origo, p);
		
		// calculate offset of markers
		@SuppressWarnings("deprecation")
		int minutes = rec_first.timestamp.getMinutes();
		@SuppressWarnings("deprecation")
		int seconds = rec_first.timestamp.getSeconds();
		int timeoffset = (minutes*60+seconds);
		
		// draw markers and labels
		int x = 0;
		String timelabel;
		Date timehere = new Date();
		SimpleDateFormat f = new SimpleDateFormat ("HH:mm", Locale.US);
		for (int i=0; i<(elapsed_secs+timeoffset); i+=60 /* step by 1 minute */ ) {

			// TODO there *must* be a better way to do this...
			x = (i -timeoffset) / x_sec_per_pixel;
			if (x<0) continue; // skip if this marker would be off the chart
			
			if (i%(600) == 0) { 	// 600 = every 10 minutes
				p.setColor(Color.BLACK);	// long black marker every 10 minutes
				c.drawLine(x_origo+x, y_origo+marker_size, x_origo+x, y_origo-marker_size, p);
				p.setColor(Color.GRAY);		// ...and gray vertical line too 
				c.drawLine(x_origo+x, y_origo-2*marker_size, x_origo+x, y_origo-chart_height, p);
				// time label
				timehere.setTime( rec_first.timestamp.getTime() + (i-timeoffset)*1000 );
				timelabel = f.format(timehere);
				p.setTextSize(text_size_small);
				p.setColor(Color.BLACK);
				c.drawText(timelabel, x_origo+x-12, y_origo+y_border/2, p);
			} else {
				p.setColor(Color.BLACK);	// otherwise short black markers every minute in between 
				c.drawLine(x_origo+x, y_origo            , x_origo+x, y_origo-marker_size, p);
			}
		}
	}
	
	private void draw_y_axis() {
		p.setColor(Color.BLACK);
		c.drawLine(x_origo, y_origo, x_origo, y_origo-chart_height-y_border, p);
		
		// draw markers and labels
		for (int mind = min_bpm; mind <= max_bpm ; mind=mind+bpm_minor) {
			p.setColor(Color.BLACK);
			p.setTextSize(text_size_small);
			if (mind%bpm_major == 0) {
				c.drawLine(x_origo-2*marker_size, y_origo-mind, x_origo+2*marker_size, y_origo-mind, p);	// Y axis long marker
				c.drawText(String.valueOf(mind), 3, y_origo-mind, p);
			} else {
				c.drawLine(x_origo-marker_size, y_origo-mind, x_origo+marker_size, y_origo-mind, p);		// Y axis short marker
			}
			p.setColor(Color.GRAY);
			c.drawLine(x_origo+2*marker_size, y_origo-mind, x_origo+chart_width, y_origo-mind, p);			// horizontal line
		}
	}

	private void draw_chart() {
		int offset;
		boolean started;
		HeartRateRec r;
		ListIterator<HeartRateRec> l = hrlist.listIterator(0) ;
		Path t = new Path();
		started = false;
		while (l.hasNext() && (l.nextIndex()<max_ind)) {	// stop if no more records, or at max_ind, just in case extra records were added to the list in the meantime
			r = l.next();
			offset = (int) Math.floor( (r.timestamp.getTime() - rec_first.timestamp.getTime()) /1000.0 /x_sec_per_pixel );
			if (!started) {
				t.setLastPoint(x_origo+offset, y_origo-r.pulse);
				started = true;
			}
			t.lineTo(x_origo+offset, y_origo-r.pulse);
		}
		p.setColor(Color.argb(255, 0, 0, 170)); // dark blue, non-transparent
		p.setStyle(Paint.Style.STROKE);
		c.drawPath(t, p);
	}
	
	public Bitmap getBitmap() {
		return b;
	}
}
