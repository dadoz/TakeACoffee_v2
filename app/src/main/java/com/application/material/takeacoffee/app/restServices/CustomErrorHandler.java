package com.application.material.takeacoffee.app.restServices;

import android.util.Log;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by davide on 24/12/14.
 */
public class CustomErrorHandler implements ErrorHandler {
    private String TAG = "CustomErrorHandler ";

    @Override
    public Throwable handleError(RetrofitError cause) {
        //get code from cause and return what you need
        Log.e(TAG, "retrofit request error" + cause.toString());
        if(cause.getResponse() == null) {
            return new Throwable("generic error - handleError",
                new Throwable("500"));
        }

        return new Throwable(cause.getResponse().getReason(),
                new Throwable("" + cause.getResponse().getStatus()));
    }
}