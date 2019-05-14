package com.example.ictucalendar.Decorator;

import android.content.Context;

import com.example.ictucalendar.Decorator.CustomDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class SubjectDecorator implements DayViewDecorator {
    private CalendarDay calendarDay;
    private int color[];
    private Context context;

    public SubjectDecorator(CalendarDay calendarDay, int color[], Context context) {
        this.calendarDay = calendarDay;
        this.color = color;
        this.context = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        // thuộc tính tháng của đối tượng CalendarDay bắt đầu từ 0 nên phải cộng tháng thêm 1
        CalendarDay cal = new CalendarDay(day.getYear(), day.getMonth() + 1, day.getDay());
        if (calendarDay.equals(cal)) {
            return true;
        } else {
            return false;
        }

    }


    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new CustomDecorator(color, context));
    }



}
