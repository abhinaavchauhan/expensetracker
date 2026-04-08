package com.expensetracker.presentation.analytics;

import android.content.Context;
import android.widget.TextView;

import com.expensetracker.R;
import com.expensetracker.core.utils.CurrencyUtils;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

public class CustomMarkerView extends MarkerView {

    private final TextView tvDate;
    private final TextView tvValue;
    private final List<String> labels;

    public CustomMarkerView(Context context, int layoutResource, List<String> labels) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tv_tooltip_date);
        tvValue = findViewById(R.id.tv_tooltip_value);
        this.labels = labels;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        if (labels != null && index >= 0 && index < labels.size()) {
            tvDate.setText(labels.get(index));
        } else {
            tvDate.setText("");
        }
        tvValue.setText(CurrencyUtils.formatAmount(e.getY()));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
