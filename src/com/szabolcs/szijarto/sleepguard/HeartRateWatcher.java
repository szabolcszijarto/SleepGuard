package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.dsi.ant.plugins.AntPluginMsgDefines;
import com.dsi.ant.plugins.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;

public class HeartRateWatcher
	extends GenericWatcher implements IDeviceStateChangeReceiver, IPluginAccessResultReceiver<AntPlusHeartRatePcc>
{
	private static int numPeaks = 0, timePeaksSecs = 0;
	private boolean connected = false;
	private AntPlusHeartRatePcc hrPcc;
	private Recording r;
	private RecordingListItem ri;
//	IDeviceStateChangeReceiver statRcvr;
//	IPluginAccessResultReceiver<AntPlusHeartRatePcc> resRcvr;

	public HeartRateWatcher (Activity_Main a) {
		super(a);
		r = new Recording();
//		myAct.startStopButton.setEnabled(false);
	}

	public void connect() {
        myAct.setStatus("Connecting...");
        // register with the ANT+ plugin
		AntPlusHeartRatePcc.requestAccess(myAct, myAct, this, this);
		myAct.connectButton.setText("Disconnect");
	}

	public void disconnect(boolean skipRelease) {
        connected = false;
        if (!skipRelease) {
        	hrPcc.releaseAccess();
        }
        hrPcc = null;
        myAct.setStatus("Disconnected");
	//	myAct.startStopButton.setEnabled(false);
		myAct.connectButton.setText("Connect");
	}

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void start() {
		if (!isConnected()) {
			Toast.makeText(myAct, "Please connect to HR belt first", Toast.LENGTH_SHORT).show();
		} else {
			// we're connected --> initialize and start the recording
			r.init();
			numPeaks=0;
			timePeaksSecs=0;
			super.start();
		}
	}

	public void stop() {
		super.stop();
		// now that we know the time the recording was stopped, set file names 
		ri = new RecordingListItem(myAct, timeStarted,  timeStopped);
		// disconnect in order to allow safe saving of the recording (otherwise ConcurrentModificationException comes?)
		disconnect(false);
		// and now save the files
		save(true, true, true);
	}

	public void save(boolean saveDat, boolean saveCsv, boolean savePng) {
		// save the recording in 3 formats to external storage
		
		String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			Toast.makeText(myAct, "Save error: cannot write to external storage", Toast.LENGTH_LONG).show();;
			return;
		}
		String fname = null;
		// serialize the Recording object to a .DAT file
		if (saveDat) {
			try {
				fname = ri.getDatFullPath();
				FileOutputStream fout = new FileOutputStream(fname);
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(r);
				oos.close();
				fout.close();
				//Toast.makeText(myAct, "DAT saved in: "+fname, Toast.LENGTH_SHORT).show();
			} catch(IOException i) {
				Toast.makeText(myAct, "DAT save error: "+i.toString(), Toast.LENGTH_SHORT).show();
				i.printStackTrace();
			}
		};
		// save pulse data as CSV file
		if (saveCsv) {
			try {
				File f = new File(ri.getCsvDir(), ri.getCsvFileName());
				BufferedWriter w = new BufferedWriter(new FileWriter(f));
				r.dumpToCsv(w);
				w.close();
				//Toast.makeText(myAct, "CSV saved in: "+f.getAbsolutePath(), Toast.LENGTH_SHORT).show();;
			} catch (IOException i) {
				Toast.makeText(myAct, "CSV save error: "+i.toString(), Toast.LENGTH_SHORT).show();
				i.printStackTrace();
			}
		}
		// save the pulse chart as PNG file
		if (savePng) {
			try {
				File f = new File(ri.getPngDir(), ri.getPngFileName());
				FileOutputStream of = new FileOutputStream(f);
				r.drawChartBitmap();
				r.dumpToPng(of);
				of.close();
				//Toast.makeText(myAct, "PNG saved in: "+f.getAbsolutePath(), Toast.LENGTH_SHORT).show();;
			} catch (IOException i) {
				Toast.makeText(myAct, "PNG save error: "+i.toString(), Toast.LENGTH_SHORT).show();
				i.printStackTrace();
			}
		}
	}

	private void newBeat(HeartRateRec hrrec) {
		// add beat to list if recording is running
		if (isRunning()) { r.add(hrrec); };
		
		// identify peaks
		// to-do...here?
	}
	
	public int getNumPeaks() {
		return numPeaks;
	}
	
	public String getTimePeaksStr() {
	    int timePeaksMins = timePeaksSecs/60;
	    int timePeaksHours = timePeaksMins/60;
	    String timePeaks = timePeaksHours +":"+ timePeaksMins +":"+ timePeaksSecs; 
	    return timePeaks;
	}

    public void onDeviceStateChange(final int newDeviceState) {
    	// this method is called back by the ANT+ plugin to notify of device state changes
    	myAct.runOnUiThread(new Runnable() {                                            
            @Override
            public void run() {
                myAct.setStatus(hrPcc.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(newDeviceState));
                if (newDeviceState == AntPluginMsgDefines.DeviceStateCodes.DEAD) {
                 	disconnect(false);
                }
            }
        });
    }	

    public void onResultReceived(AntPlusHeartRatePcc result, int resultCode, int initialDeviceStateCode) {
        // this method is called back by the ANT+ plugin with connection result
    	switch(resultCode) {
    		// connected
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS:
                Toast.makeText(myAct, "Connected", Toast.LENGTH_SHORT).show();
            	hrPcc = result;
                connected = true;
        		//myAct.startStopButton.setEnabled(true);
                myAct.setStatus(result.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(initialDeviceStateCode));
                subscribeToEvents();
                break;
            // error handling
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE:
                Toast.makeText(myAct, "Channel Not Available", Toast.LENGTH_SHORT).show();
                myAct.setStatus("Channel Not Available");
                break;
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatOTHERFAILURE:
                Toast.makeText(myAct, "RequestAccess failed, see logcat for details", Toast.LENGTH_SHORT).show();
                myAct.setStatus("RequestAccess failed. Do Menu->Reset.");
                break;
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED:
                Toast.makeText(myAct, "Dependencies not installed", Toast.LENGTH_SHORT).show();
                myAct.setStatus("Please install dependencies!");
            	AlertDialog.Builder adlgBldr = new AlertDialog.Builder(myAct);
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("The required application\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n is not installed. Do you want to launch the Play Store to search for it?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent startStore = null;
                                startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                myAct.startActivity(startStore);
                            }
                        });
                adlgBldr.setNegativeButton("Cancel", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog waitDialog = adlgBldr.create();
                waitDialog.show();
                break;
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatUSERCANCELLED:
                Toast.makeText(myAct, "User cancelled operation", Toast.LENGTH_SHORT).show();
                myAct.setStatus("Cancelled");
                break;
            default:
                Toast.makeText(myAct, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                myAct.setStatus("Unrecognized result: " + resultCode + ". Do Menu->Reset.");
                break;
        } 
    }
	
    // Subscribe to heart rate events, connecting them to display their data.
   private void subscribeToEvents() {
       hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {
          @Override
          public void onNewHeartRateData(final int currentMessageCount, final int computedHeartRate, final long heartBeatCounter) {
        	  myAct.runOnUiThread(new Runnable() {                                            
                	@Override
                	public void run() {
                		myAct.setPulse(String.valueOf(currentMessageCount)+" | "+String.valueOf(computedHeartRate)+" | "+String.valueOf(heartBeatCounter));
                	}
                });
        	  // send a new heart rate record to the watcher
        	  HeartRateRec hrrec = new HeartRateRec ( currentMessageCount, new Date(), (short) computedHeartRate, (int) heartBeatCounter );
        	  newBeat(hrrec);
          }
       });
/*
       hrPcc.subscribeHeartRateDataTimestampEvent(new IHeartRateDataTimestampReceiver() {
    	   @Override
    	   public void onNewHeartRateDataTimestamp(final int currentMessageCount, final BigDecimal timestampOfLastEvent) {
    		   myAct.runOnUiThread(new Runnable() {                                            
    			   @Override
    			   public void run() {
//    				   tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//    				   tv_timestampOfLastEvent.setText(String.valueOf(timestampOfLastEvent));
    			   }
    		   });
    	   }
       });
       hrPcc.subscribePage4AddtDataEvent(new IPage4AddtDataReceiver() {
    	   @Override
    	   public void onNewPage4AddtData(final int currentMessageCount, final int manufacturerSpecificByte, final BigDecimal timestampOfPreviousToLastHeartBeatEvent) {
    		   myAct.runOnUiThread(new Runnable() {                                            
    			   @Override
    			   public void run() {
//    				   tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//    				   tv_manufacturerSpecificByte.setText(String.format("0x%02X", manufacturerSpecificByte));
//    				   tv_previousToLastHeartBeatEventTimeStamp.setText(String.valueOf(timestampOfPreviousToLastHeartBeatEvent));
    			   }
    		   });
    	   }
       });
       hrPcc.subscribeCumulativeOperatingTimeEvent(new ICumulativeOperatingTimeReceiver() {
    	   @Override
    	   public void onNewCumulativeOperatingTime(final int currentMessageCount, final long cumulativeOperatingTime) {
    		   myAct.runOnUiThread(new Runnable() {                                            
    			   @Override
    			   public void run() {
//    				   tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//    				   tv_cumulativeOperatingTime.setText(String.valueOf(cumulativeOperatingTime));
    			   }
    		   });
    	   }
       });
       hrPcc.subscribeManufacturerAndSerialEvent(new IManufacturerAndSerialReceiver() {
    	   @Override
    	   public void onNewManufacturerAndSerial(final int currentMessageCount, final int manufacturerID, final int serialNumber) {
    		   myAct.runOnUiThread(new Runnable() {                                            
    			   @Override
    			   public void run() {
//    				   tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//    				   tv_manufacturerID.setText(String.valueOf(manufacturerID));
//    				   tv_serialNumber.setText(String.valueOf(serialNumber));
    			   }
    		   });
    	   }
       });
       hrPcc.subscribeVersionAndModelEvent(new IVersionAndModelReceiver() {
    	   @Override
    	   public void onNewVersionAndModel(final int currentMessageCount, final int hardwareVersion, final int softwareVersion, final int modelNumber) {
    		   myAct.runOnUiThread(new Runnable() {                                            
    			   @Override
    			   public void run() {
//    				   tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//    				   tv_hardwareVersion.setText(String.valueOf(hardwareVersion));
//    				   tv_softwareVersion.setText(String.valueOf(softwareVersion));
//    				   tv_modelNumber.setText(String.valueOf(modelNumber));
    			   }
    		   });
    	   }
       });
*/
   }    	
}