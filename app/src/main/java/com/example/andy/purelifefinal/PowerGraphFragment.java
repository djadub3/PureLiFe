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
public class PowerGraphFragment extends Fragment {
    ArrayList<ILineDataSet> dataSets;
    LineChart lineChart;
    LineData data;
    LineDataSet inPowerSet;
    LineDataSet outPowerSet;

    TextView powerInMaxText;
    TextView powerOutMaxText;


    public PowerGraphFragment() {

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
        return inflater.inflate(R.layout.fragment_power_graph, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.v("power graphs", "view created");
        super.onViewCreated(view, savedInstanceState);
        lineChart = (LineChart) view.findViewById(R.id.chart);

        powerInMaxText=(TextView) view.findViewById(R.id.power_in_max_text);
        powerInMaxText.setText(String.valueOf(inPowerSet.getYMax())+"W");

        powerOutMaxText=(TextView) view.findViewById(R.id.power_out_max_text);
        powerOutMaxText.setText(String.valueOf(outPowerSet.getYMax())+"W");

        float maxY;
        maxY= inPowerSet.getYMax();
        if(outPowerSet.getYMax()>inPowerSet.getYMax())  maxY=outPowerSet.getYMax();

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
        leftYAxis.setValueFormatter(new WattageYAxisValueFormatter());
        leftYAxis.setLabelCount(6, true);
        leftYAxis.setAxisMinValue(0);
        leftYAxis.setAxisMaxValue(maxY+10);
        leftYAxis.setTextColor(Color.BLACK);
        leftYAxis.setDrawGridLines(false);


        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false);
    }
    public void populate(LineDataSet inPowerSet,LineDataSet outPowerSet,ArrayList<String> xLabels)
    {
        this.inPowerSet=inPowerSet;
        this.outPowerSet=outPowerSet;
        inPowerSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        inPowerSet.setColor(Color.GREEN);
        inPowerSet.setDrawCubic(true);
        inPowerSet.setDrawCircles(false);
        inPowerSet.setLineWidth(3);
        inPowerSet.setDrawFilled(true);
        inPowerSet.setFillColor(Color.GREEN);

        outPowerSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        outPowerSet.setColor(Color.RED);
        outPowerSet.setDrawCubic(true);
        outPowerSet.setDrawCircles(false);
        outPowerSet.setLineWidth(3);
        outPowerSet.setDrawFilled(true);
        outPowerSet.setFillColor(Color.RED);

        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(inPowerSet);
        dataSets.add(outPowerSet);
        data = new LineData(xLabels,dataSets);
        data.setDrawValues(false);

    }

    /*
    public void removeGraphData(){
        powerVals= new ArrayList<Entry>();
        xLabels = new ArrayList<String>();
        count=0;
    }

    */

    public void updateGraph()
    {
        lineChart.invalidate();
    }


    public class WattageYAxisValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        public WattageYAxisValueFormatter () {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value) + " W"; // e.g. append a dollar-sign
        }
    }

    public class VoltageYAxisValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        public VoltageYAxisValueFormatter () {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value) + " V"; // e.g. append a dollar-sign
        }
    }

}
