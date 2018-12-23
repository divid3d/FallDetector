package com.example.divided.falldetector;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ChartUtils {

    private static final int MAX_CHART_POINTS = 256;

    public static void setupChart(Context context, LineChart lineChart, boolean enableYAxis, boolean offsetDisable) {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        if (offsetDisable) {
            lineChart.setViewPortOffsets(0, 0, 0, 0);
        } else{
            lineChart.getAxisLeft().setTextColor(Color.WHITE);
            lineChart.getAxisLeft().setTypeface(ResourcesCompat.getFont(context,R.font.product_sans_regular));
        }
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(enableYAxis);
        if (enableYAxis) {
            lineChart.getAxisLeft().enableGridDashedLine(40f, 40f, 0);
            lineChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        }
        lineChart.getAxisRight().setEnabled(false);
    }

    private static LineDataSet createSet(@ColorInt int color, boolean setFill) {
        LineDataSet set = new LineDataSet(null, null);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[0]);
        set.setLineWidth(1f);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setDrawFilled(setFill);
        set.setFillAlpha(32);
        set.setFillColor(color);

        return set;
    }

    public static void clearChart(LineChart lineChart) {
        if (lineChart.getData() != null) {
            lineChart.getData().clearValues();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }

    public static void addEntry(ChartPoint point, LineChart lineChart, @ColorInt int color, boolean maxChartPointsSet, boolean setFill) {
        LineData data = lineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet(color, setFill);
                data.addDataSet(set);
            }
            final int dataCount = lineChart.getData().getEntryCount();
            if (dataCount >= MAX_CHART_POINTS && maxChartPointsSet) {
                set.removeFirst();
            }
            if (set.getEntryCount() == 0) {
                data.addEntry(new Entry(0, point.getValue()), 0);
            } else {
                data.addEntry(new Entry(set.getEntryForIndex(set.getEntryCount() - 1).getX() + 1, point.getValue()), 0);
            }
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.moveViewToX(data.getEntryCount());
        }
    }

    public static void setupData(LineChart lineChart) {
        LineData data = new LineData();
        lineChart.setData(data);
    }
}
