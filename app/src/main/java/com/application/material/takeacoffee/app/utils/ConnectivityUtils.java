package com.application.material.takeacoffee.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

/**
 * Created by davide on 01/05/16.
 */
public class ConnectivityUtils {
    /**
     *
     * @param contextWeakReference
     * @return
     */
    public static boolean isConnected(WeakReference<Context> contextWeakReference) {
        ConnectivityManager connectivityManager = (ConnectivityManager) contextWeakReference.get()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null &&
                networkInfo.isConnectedOrConnecting();
    }
}
