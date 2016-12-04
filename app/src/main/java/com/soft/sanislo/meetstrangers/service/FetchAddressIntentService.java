package com.soft.sanislo.meetstrangers.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by root on 11.09.16.
 */
public class FetchAddressIntentService extends IntentService {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.soft.sanislo.meetstrangers";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String KEY_ERROR = PACKAGE_NAME +
            ".KEY_ERROR";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";
    public static final String RESULT_CODE = PACKAGE_NAME + ".RESULT_CODE";
    public static final String RESULT_ADDRESS = PACKAGE_NAME + ".RESULT_ADDRESS";
    private static final String TAG = FetchAddressIntentService.class.getSimpleName();

    protected ResultReceiver resultReceiver;
    private String errorMessage;

    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        resultReceiver = intent.getParcelableExtra(RECEIVER);
        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            errorMessage = "Service Not Available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid Latitude or Longitude Used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() + ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Could not find address...";
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage, null);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments), address);
        }
    }

    private void deliverResultToReceiver(int resultCode, String errorMessage, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_ADDRESS, address);
        bundle.putString(KEY_ERROR, errorMessage);
        resultReceiver.send(resultCode, bundle);
    }
}
