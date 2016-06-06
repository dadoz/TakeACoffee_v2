package com.application.material.takeacoffee.app.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.application.material.takeacoffee.app.R;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;

/**
 * Created by davide on 30/12/14.
 */
public class Utils {
    private Utils() {
    }

    /**
     * format date
     * @return
     */
    public static String getFormattedTimestamp(long timestamp) {
        return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ITALY)
                .format(new Date(timestamp * 1000));
    }

    /**
     * TODO move out
     * @return
     */
    public static Drawable getColoredDrawable(Drawable defaultIcon, int color) {
        defaultIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return defaultIcon;
    }

    /**
     *
     * @param latLng
     * @return
     */
    public static String getLatLngString(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }

    /**
     *
     * @param myText
     * @param contextWeakRefer
     * @return
     */
    public static Spannable wrapInCustomfont(String myText, WeakReference<Context> contextWeakRefer) {
        Typeface typeface = Typeface.createFromAsset(contextWeakRefer.get().getAssets(), "fonts/chimphand-regular.ttf");
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(typeface);
        SpannableString spannable = new SpannableString(myText);
        spannable.setSpan(typefaceSpan, 0, myText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     *
     * @param contextWeakRefer
     * @return
     */
    public static void hideKeyboard(WeakReference<Context> contextWeakRefer, @NonNull View view) {
        InputMethodManager imm = (InputMethodManager)contextWeakRefer.get()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     *
     * @param view
     * @param messageResourceId
     * @return
     */
    public static void showSnackbar(WeakReference<Context> contextWeakRefer, View view, int messageResourceId) {
        if (contextWeakRefer.get() == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, messageResourceId, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(contextWeakRefer.get(),
                R.color.material_red400));
//        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
//                .setTextColor(ContextCompat.getColor(contextWeakRefer.get(), R.color.material_brown900));
        snackbar.show();
    }

    /**
     *
     * @param timestampStr
     * @return
     */
    public static String convertLastUpdateFromTimestamp(String timestampStr) {
        if (timestampStr == null) {
            return " -";
        }

        return new DateTime(Long.parseLong(timestampStr))
                .toString(DateTimeFormat.forPattern("dd MMM YYYY"));
    }

//    public static SpannableString getSpannableFromString(Activity activity, String text) {
//        //TODO move on Utils
//        Typeface font = Typeface.createFromAsset(activity.getAssets(), "chimphand-regular.ttf");
//        SpannableString spannableString = new SpannableString(text);
//        spannableString.setSpan(new TypefaceSpan("", font), 0,
//                spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return spannableString;
//    }
}
