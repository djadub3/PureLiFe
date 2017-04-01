package com.example.andy.purelifefinal;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.BatteryIndicatorGauge;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    TextView batteryVoltageText;
    TextView capacityText;
    TextView chargingText;
    TextView energyGeneratedText;
    TextView consumedEnergyText;
    TextView ampHoursStoredText;
    TextView batHealthText;

    BarChart powerChart;
    ArrayList<String> xlabels;
    YAxis leftAxis;


    private BatteryIndicatorGauge batteryGauge;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //battery gauge
        batteryGauge = (BatteryIndicatorGauge) view.findViewById(R.id.batteryGaugeView);
        batteryGauge.setMax(100);
        batteryGauge.setMin(0);
        batteryGauge.setValue(0);
        batteryGauge.setOrientation(1);

        batteryVoltageText = (TextView) view.findViewById(R.id.batteryVoltageText);
        capacityText = (TextView) view.findViewById(R.id.capacity_text);
        chargingText = (TextView) view.findViewById(R.id.charging_text);
        energyGeneratedText = (TextView) view.findViewById(R.id.energy_generated_text);
        consumedEnergyText = (TextView) view.findViewById(R.id.consumed_energy_text);
        ampHoursStoredText = (TextView) view.findViewById(R.id.amp_hours_stored_text);
        batHealthText =(TextView) view.findViewById(R.id.bat_health);


        powerChart = (BarChart) view.findViewById(R.id.chart);

        powerChart.setDrawBarShadow(false);
        powerChart.setDescription("");
        powerChart.setDrawGridBackground(false);

        XAxis xAxis = powerChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis rightAxis = powerChart.getAxisRight();
        rightAxis.setEnabled(false);

        leftAxis = powerChart.getAxisLeft();
        leftAxis.setAxisMaxValue(100);
        leftAxis.setAxisMinValue(-100);
        leftAxis.setLabelCount(9, true);
        leftAxis.setDrawZeroLine(true);

        Legend legend = powerChart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f,0));
        entries.add(new BarEntry(0f,1));
        entries.add(new BarEntry(0f,2));

        BarDataSet dataSet= new BarDataSet(entries,"Power");
        dataSet.setValueTextSize(12);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        xlabels =new ArrayList<>();
        xlabels.add("Generated");
        xlabels.add("Consumed");
        xlabels.add("To Battery");

        BarData data = new BarData(xlabels,dataSet);

        powerChart.setData(data);


        powerChart.setDrawBorders(false);
        powerChart.animateY(2000);
        powerChart.setClickable(false);


        super.onViewCreated(view, savedInstanceState);
    }

    public void update(String batteryVoltage,String inPower, String outPower, String batPower, String capacity, String solarEnergy,String consumedEnergy, String ampHoursStored, String batHealth){

        batteryVoltageText.setText(batteryVoltage+"V");
        capacityText.setText(capacity+"%");
        energyGeneratedText.setText(solarEnergy+"WH");
        consumedEnergyText.setText(consumedEnergy+"WH");
        ampHoursStoredText.setText(ampHoursStored+"AH");
        batHealthText.setText(batHealth);


        if(Float.valueOf(batPower)>0){
            chargingText.setText("Charging");
        }
        else chargingText.setText("Not Charging");


        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(Float.valueOf(inPower),0));
        entries.add(new BarEntry(Float.valueOf(outPower),1));
        entries.add(new BarEntry(Float.valueOf(batPower),2));

        leftAxis.setAxisMaxValue(100);
        leftAxis.setAxisMinValue(-100);

        if(Float.valueOf(outPower)>=100 || Float.valueOf(batPower)<=-100)
        {
            leftAxis.setAxisMaxValue(200);
            leftAxis.setAxisMinValue(-200);
        }
        if(Float.valueOf(outPower)>=200 || Float.valueOf(batPower)<=-200)
        {
            leftAxis.setAxisMaxValue(300);
            leftAxis.setAxisMinValue(-300);
        }

        if(Float.valueOf(outPower)>=300 || Float.valueOf(batPower)<=-300)
        {
            leftAxis.setAxisMaxValue(400);
            leftAxis.setAxisMinValue(-400);
        }

        BarDataSet dataSet= new BarDataSet(entries,"Power");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(xlabels,dataSet);
        dataSet.setValueTextSize(12);
        powerChart.setData(data);

        powerChart.notifyDataSetChanged();
        powerChart.invalidate();

        int capacityInt = (int) Math.ceil(Double.valueOf(capacity));
        batteryGauge.setValue(capacityInt);

    }

}
