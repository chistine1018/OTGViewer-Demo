package com.asn.otgviewer_demo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Anson on 24/07/19.
 */
public class UsbReceiver extends BroadcastReceiver {
    private String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(TAG, "mUsbReceiver triggered. Action " + action);

        /*
        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            mUsbConnected=false;

            removedUSB();
        }

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            mUsbConnected = true;

        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null){
                        //call method to set up device communication
                        Log.d(TAG, "granted permission for device " + device.getDeviceName() + "!");
                        connectDevice(device);
                    }
                }
                else {
                    Log.d(TAG, "permission denied for device " + device);
                }
            }
        }
        */
    }
}
