package com.bluetrum.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.fragment.app.Fragment;

public class DisplayUtils {

    public static int dip2px(Fragment fragment, int dp) {
        return dip2px(fragment.requireContext(), dp);
    }

    public static int dip2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
