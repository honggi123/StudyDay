package com.coworkerteam.coworker.data.model.other;

import android.content.Context;
import android.widget.TextView;

import com.coworkerteam.coworker.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return super.getOffset();
    }
}
