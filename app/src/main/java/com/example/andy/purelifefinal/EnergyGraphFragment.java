package com.example.andy.purelifefinal;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnergyGraphFragment extends Fragment {
    ArrayList<ILineDataSet> dataSets;
    LineChart lineChart;
    LineData data;
    LineDataSet energyGeneratedSet;
    LineDataSet energyConsumedSet;

    TextView energyGeneratedText;
    TextView energyConsumedText;


    public EnergyGraphFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem loadPVOItem = menu.findItem(R.id.load_PVO_data);
        loadPVOItem.setVisible(false);
        MenuItem loadDayItem = menu.findItem(R.id.load_day_data);
        loadDayItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_energy_graph, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.v("power graphs", "view created");
        super.onViewCreated(view, savedInstanceState);
        lineChart = (LineChart) view.findViewById(R.id.chart);

        energyGeneratedText=(TextView) view.findViewById(R.id.energy_generated_text);
        energyGeneratedText.setText(String.valueOf(energyGeneratedSet.getYMax())+"WH");

        energyConsumedText=(TextView) view.findViewById(R.id.energy_consumed_text);
        energyConsumedText.setText(String.valueOf(energyConsumedSet.getYMax())+"WH");

        float maxY;
        maxY= energyGeneratedSet.getYMax();
        if(energyConsumedSet.getYMax()>energyGeneratedSet.getYMax())  maxY=energyConsumedSet.getYMax();

        lineChart.setDescription("");
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);



        data.setDrawValues(false);
        lineChart.setData(data);
        lineChart.animateY(2500);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);


        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setValueFormatter(new WattHourYAxisValueFormatter());
        leftYAxis.setLabelCount(6, true);
        leftYAxis.setAxisMinValue(0);
        leftYAxis.setAxisMaxValue(maxY+10);
        leftYAxis.setTextColor(Color.BLACK);
        leftYAxis.setDrawGridLines(false);


        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false);
    }
    public void populate(LineDataSet energyGeneratedSet,LineDataSet energyConsumedSet, ArrayList<String> xLabels)
    {
        this.energyGeneratedSet=energyGeneratedSet;
        this.energyConsumedSet=energyConsumedSet;
        energyGeneratedSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        energyGeneratedSet.setColor(Color.GREEN);
        energyGeneratedSet.setDrawCubic(true);
        energyGeneratedSet.setDrawCircles(false);
        energyGeneratedSet.setLineWidth(3);
        energyGeneratedSet.setDrawFilled(true);
        energyGeneratedSet.setFillColor(Color.GREEN);



        energyConsumedSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        energyConsumedSet.setColor(Color.RED);
        energyConsumedSet.setDrawCubic(true);
        energyConsumedSet.setDrawCircles(false);
        energyConsumedSet.setLineWidth(3);
        energyConsumedSet.setDrawFilled(true);
        energyConsumedSet.setFillColor(Color.RED);



        dataSets = new ArrayList<ILineDataSet>();
        Log.v("energy frag","1");
        dataSets.add(energyGeneratedSet);
        Log.v("energy frag","2");
        dataSets.add(energyConsumedSet);
        Log.v("energy frag","3");
        data = new LineData(xLabels,dataSets);
        Log.v("energy frag","4");
        data.setDrawValues(false);
    }


    public class WattHourYAxisValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        public WattHourYAxisValueFormatter () {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value) + " WH"; // e.g. append a dollar-sign
        }
    }
}
