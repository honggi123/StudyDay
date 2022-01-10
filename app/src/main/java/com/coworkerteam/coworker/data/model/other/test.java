package com.coworkerteam.coworker.data.model.other;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.coworkerteam.coworker.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

/**
 * Decorate several days with a dot
 */
public class test implements DayViewDecorator {

    private final Drawable drawable;
    private int color;
    private HashSet<CalendarDay> dates;

    public test(int color, Collection<CalendarDay> dates, Activity context) {
        drawable = context.getResources().getDrawable(R.drawable.ic_baseline_lock_24);
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
        //view.addSpan(new DotSpan(5, color)); // 날자밑에 점
    }
}