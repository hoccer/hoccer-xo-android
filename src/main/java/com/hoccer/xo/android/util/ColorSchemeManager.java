package com.hoccer.xo.android.util;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.hoccer.xo.release.R;

public abstract class ColorSchemeManager{

    public static Drawable fillBackground(Context activity, int bgId, boolean primaryColor) {

        int custom_color = (primaryColor) ? activity.getResources().getColor(R.color.xo_app_main_color) : activity.getResources().getColor(R.color.xo_app_main_second_color);

        float factor = 1f / 255f;

        float k = (Color.red(custom_color))*factor;
        float l = (Color.green(custom_color))*factor;
        float m = (Color.blue(custom_color))*factor;

        float[] colorMatrix = {
                k, 0, 0, 0, 0,	//red
                0, l, 0, 0, 0,	//green
                0, 0, m, 0, 0,	//blue
                0, 0, 0, 1, 0 	//alpha
        };

        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

        Drawable myBG = activity.getResources().getDrawable(bgId);

        myBG.setColorFilter(colorFilter);

        return myBG;
    }
}
