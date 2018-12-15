package com.example.divided.falldetector;

import android.support.annotation.ColorInt;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ChartUtils {

    private static final int MAX_CHART_POINTS = 200;

    public static void setupChart(LineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setViewPortOffsets(0, 0, 0, 0);
        //lineChart.setAutoScaleMinMaxEnabled(true);
        //lineChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        //lineChart.getAxisLeft().setAxisMinimum(min);
        //lineChart.getAxisLeft().setAxisMaximum(max);
        //lineChart.getAxisLeft().setTypeface(Typeface.createFromAsset(this.getAssets(), "product_sans_bold.ttf"));
        //lineChart.getAxisLeft().enableGridDashedLine(8f, 8f, 0);
    }

    private static LineDataSet createSet(@ColorInt int color) {
        LineDataSet set = new LineDataSet(null, null);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[0]);
        set.setLineWidth(1.25f);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setDrawFilled(true);
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

    public static void addEntry(ChartPoint point, LineChart lineChart, @ColorInt int color) {
        LineData data = lineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet(color);
                data.addDataSet(set);
            }
            final int dataCount = lineChart.getData().getEntryCount();
            if (dataCount >= MAX_CHART_POINTS) {
                set.removeFirst();
            }
            data.addEntry(new Entry(point.getTimeStamp(), point.getValue()), 0);
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
