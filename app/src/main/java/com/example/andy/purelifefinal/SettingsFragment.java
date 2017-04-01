package com.example.andy.purelifefinal;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        final SharedPreferences.Editor editor = sharedPref.edit();

        final EditText pvoSystemId = (EditText) view.findViewById(R.id.pvo_system_id);

        String savedSytemIdDefault = getResources().getString(R.string.saved_system_id_default);
        String savedSytemId = sharedPref.getString(getString(R.string.saved_system_id),savedSytemIdDefault);
        pvoSystemId.setText(savedSytemId);

        Button setSytemID = (Button) view.findViewById(R.id.set_system_id);
        setSytemID.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = pvoSystemId.getText().toString();
                editor.putString(getString(R.string.saved_system_id),str);
                editor.commit();
                pvoSystemId.setText(str);
            }
        });

        final EditText apiKey = (EditText) view.findViewById(R.id.api_key);
        String savedAPIKeyDefault = getResources().getString(R.string.saved_api_key_default);
        String savedAPIKey = sharedPref.getString(getString(R.string.saved_api_key),savedAPIKeyDefault);
        apiKey.setText(savedAPIKey);

        Button setAPIKey = (Button) view.findViewById(R.id.set_api_key);
        setAPIKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = apiKey.getText().toString();
                editor.putString(getString(R.string.saved_api_key),str);
                editor.commit();
                apiKey.setText(str);
            }
        });


        final EditText utcOffset = (EditText) view.findViewById(R.id.utc_offset);
        String savedUTCOffsetDefault = getResources().getString(R.string.saved_utc_offset_default);
        String savedUTCOffset = sharedPref.getString(getString(R.string.saved_utc_offset),savedUTCOffsetDefault);
        utcOffset.setText(savedUTCOffset);

        Button setUTCOffset = (Button) view.findViewById(R.id.set_utc_offset);
        setAPIKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = utcOffset.getText().toString();
                editor.putString(getString(R.string.saved_utc_offset),str);
                editor.commit();
                utcOffset.setText(str);
            }
        });



    }

}
