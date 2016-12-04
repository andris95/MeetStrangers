package com.soft.sanislo.meetstrangers.view;

import android.animation.TypeEvaluator;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;

import static android.R.attr.fraction;

/**
 * Created by root on 15.11.16.
 */

public class LatLngEvaluator implements TypeEvaluator<LatLng> {

    @Override
    public LatLng evaluate(float v, LatLng initial, LatLng end) {
        return SphericalUtil.interpolate(initial, end, v);
    }
}
