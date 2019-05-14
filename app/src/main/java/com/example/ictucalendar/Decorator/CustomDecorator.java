package com.example.ictucalendar.Decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class CustomDecorator implements LineBackgroundSpan {
    private int color[];
    private Context context;

    public final String TAG = CustomDecorator.class.getSimpleName();

    public CustomDecorator(int[] color, Context context) {
        this.color = color;
        this.context = context;
       //Log.d(TAG, "CustomDecorator: " + color.length);
    }

    @Override
    public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
        int total = color.length;
       /* if (color.length > 5) {
            total = 5;
        } else {
            total = color.length;
        }*/

        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        int radius;

        //Log.d(TAG, "drawBackground: " + dpi + " " + left + " " + right);

        if (dpi <= 420) {
            if (dpi >= 320) {
                radius = 3;
            } else {
                radius = 2;
            }

            double leftMost = (total - 1) * -(5);
            for (int i = 0; i < color.length; i++) {
                int oldColor = paint.getColor();
                if (color[i] != 0) {
                    paint.setColor(color[i]);
                }
                canvas.drawCircle((float) ((left + right) / 2 - leftMost), bottom + radius, radius, paint);
                paint.setColor(oldColor);
                leftMost = leftMost + 10;
            }
        } else if (dpi > 420) {
            if (dpi < 560) {
                radius = 4;
            } else {
                radius = 5;
            }

            double leftMost = (total - 1) * -(10);
            for (int i = 0; i < color.length; i++) {
                int oldColor = paint.getColor();
                if (color[i] != 0) {
                    paint.setColor(color[i]);
                }
                canvas.drawCircle((float) ((left + right) / 2 - leftMost), bottom + radius, radius, paint);
                paint.setColor(oldColor);
                leftMost = leftMost + 20;
            }
        }

    }
}
