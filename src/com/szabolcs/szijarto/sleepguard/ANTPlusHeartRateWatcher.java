package com.szabolcs.szijarto.sleepguard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.plugins.AntPluginMsgDefines;

import com.dsi.ant.plugins.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.AntPluginPcc.IPluginAccessResultReceiver;
//New API version would have these instead...
//import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
//import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;

import java.util.Date;

import static com.szabolcs.szijarto.sleepguard.R.*;


public class ANTPlusHeartRateWatcher
	extends GenericWatcher implements IDeviceStateChangeReceiver, IPluginAccessResultReceiver<AntPlusHeartRatePcc>
{
	private boolean connected = false;
	private AntPlusHeartRatePcc hrPcc;
	private Recording r;
	private RecordingFile rf;

	public ANTPlusHeartRateWatcher(Activity_Main a) {
		super(a);
        // TODO not nice but this initializes the UI state
        disconnect(true);
	}

	public void connect() {
        myact.setConnStatus("Connecting...");
        AntPlusHeartRatePcc.requestAccess(myact, myact, this, this);    // register with the ANT+ plugin
	}

    public void setConnected() {
        connected = true;
        myact.setConnStatus("Connected to " + hrPcc.getDeviceName());
        myact.connectButton.setImageResource(drawable.heart);
        myact.connectButton.setEnabled(true);
        myact.connectButton.setActivated(true);
        myact.setRecStatus(myact.getResources().getString(string.clickToRecord));
        myact.startStopButton.setImageResource(drawable.record);
        myact.startStopButton.setEnabled(true);
        myact.startStopButton.setActivated(false);
    }

    public void disconnect(boolean skipReleaseAccess) {
        connected = false;
        if (!skipReleaseAccess) { hrPcc.releaseAccess(); }
        hrPcc = null;
        myact.setConnStatus(myact.getResources().getString(R.string.clickToConnect));
        myact.connectButton.setImageResource(drawable.ant_plus);
        myact.connectButton.setEnabled(true);
        myact.connectButton.setActivated(false);
        myact.setPulse("");
        myact.setRecStatus("");
        myact.startStopButton.setImageResource(drawable.record);
        myact.startStopButton.setEnabled(false);
        myact.startStopButton.setActivated(false);
        myact.setElapsTime("");
    }

	public boolean isConnected() {
		return connected;
	}

	@Override
	public void start() {
		if (isConnected()) {
			r = new Recording();
			super.start();
            myact.startStopButton.setImageResource(drawable.stop);
            myact.startStopButton.setEnabled(true);
            myact.startStopButton.setActivated(true);
            myact.setRecStatus("Recording in progress...");
		} else {
			Toast.makeText(myact, "Press the bluetooth icon to connect to the HR belt beginner", Toast.LENGTH_SHORT).show();
		}
	}

    public RecordingListItem stop() {
		super.stop();
		// disconnect in order to allow safe saving of the recording (otherwise ConcurrentModificationException comes?)
		disconnect(false);
        r.detectPeaks();
		// now that we know the time when the recording was stopped, set file names
		rf = new RecordingFile(myact, r);
		// save recording to all output files (DAT, PNG, CSV)
		rf.save(r, true, true, true);
        RecordingListItem rli = new RecordingListItem(r,rf);
        return rli;
	}

	public void onDeviceStateChange(final int newDeviceState) {
    	// this method is called back by the ANT+ plugin to notify of device state changes
    	myact.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(this.toString(), "DEV STATE CHANGED: " + AntPlusHeartRatePcc.statusCodeToPrintableString(newDeviceState));
                if ( (newDeviceState == AntPluginMsgDefines.DeviceStateCodes.TRACKING) && !isConnected() ) setConnected();
                if (newDeviceState == AntPluginMsgDefines.DeviceStateCodes.DEAD) stop();
            }
        });
    }	

    public void onResultReceived(AntPlusHeartRatePcc result, int resultCode, int initialDeviceStateCode) {
        // this method is called back by the ANT+ plugin with connection result
    	switch(resultCode) {
    		// connected
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS:
            	hrPcc = result;
                setConnected();
                subscribeToEvents();
                break;
            // error handling
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE:
                myact.setConnStatus("Channel Not Available");
                break;
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatOTHERFAILURE:
                myact.setConnStatus("RequestAccess failed.");
                break;
            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED:
                myact.setConnStatus("ANT+ dependencies not installed");
            	AlertDialog.Builder adlgBldr = new AlertDialog.Builder(myact);
                adlgBldr.setTitle("Missing Dependency");
                adlgBldr.setMessage("The required application\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n is not installed. Do you want to launch the Play Store to search for it?");
                adlgBldr.setCancelable(true);
                adlgBldr.setPositiveButton("Go to Store", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent startStore = null;
                                startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                myact.startActivity(startStore);
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
                myact.setConnStatus("Cancelled");
                break;
            default:
                myact.setConnStatus("Unrecognized result: " + resultCode + ". Do Menu->Reset.");
                break;
        } 
    }
	
    // Subscribe to heart rate events, connecting them to display their data.
    private void subscribeToEvents() {
       hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {

           private HeartRateRec hrrec;

           @Override
           public void onNewHeartRateData(final int currentMessageCount, final int computedHeartRate, final long heartBeatCounter) {
               // TODO : this seems to cause hangups after running for a few hours?
               // TODO : why is currentMessageCount always zero?

               // save current heart rate record
              hrrec = new HeartRateRec ( currentMessageCount, new Date(), (byte) computedHeartRate, (int) heartBeatCounter );
/*
               // update UI labels
               if (isConnected()) {
                   myact.runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           myact.setPulse(hrrec.pulse+" (#"+hrrec.heartbeats+")");
                       }
                   });
               }
*/
               if (isRunning()) {
                   // add a new heart rate record to the recording
                   r.add(hrrec);

                   /*
                   // update UI labels
        	       myact.runOnUiThread(new Runnable() {
                       @Override
                	   public void run() {
                           myact.setElapsTime(getTimeElapsedString());
                       }
                   });
                   */
               }

          }
       });
   }

}