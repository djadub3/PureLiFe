package com.example.andy.purelifefinal;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {


    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView batteryLink = (TextView) view.findViewById(R.id.battery_link_text);
        batteryLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView chargeControllerLink = (TextView) view.findViewById(R.id.charge_controller_link);
        chargeControllerLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView solarPanelLink = (TextView) view.findViewById(R.id.solar_panel_link);
        solarPanelLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView moreInfoLink = (TextView) view.findViewById(R.id.more_info);
        moreInfoLink.setMovementMethod(LinkMovementMethod.getInstance());

        super.onViewCreated(view, savedInstanceState);
    }
}
