package com.example.andy.purelifefinal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    //fragments
    HomeFragment homeFragment;

    // graph variables
    private String time;

    ArrayList<Entry> inPowerVals;
    LineDataSet inPowerSet;
    ArrayList<Entry> outPowerVals;
    LineDataSet outPowerSet;


    ArrayList<Entry> generatedEnergyVals;
    LineDataSet generatedEnegySet;
    ArrayList<Entry> consumedEnergyVals;
    LineDataSet consumedEnergySet;

    ArrayList<Entry> capacityVals;
    LineDataSet capacitySet;

    ArrayList<String> xLabels;
    int count;


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;


    //PVO service variables

    PVOutputsService mPvOutputsService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

        //set up graphing lists
        inPowerVals = new ArrayList<>();
        outPowerVals = new ArrayList<>();
        xLabels = new ArrayList<>();

        // launch home fragment
        homeFragment = new HomeFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_main, homeFragment).commit();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupBt();
        }

        // set up PVOutputs Service

        Intent intent = new Intent(this, PVOutputsService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public final Handler mPVOHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.v("PVO Handler", String.valueOf(msg.obj));

            Toast.makeText(activity,String.valueOf(msg.obj)+" data points uploaded to PVO",Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupBt() {
        Log.d(TAG, "setupBt()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        final ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            Log.v(TAG, "actionbar null");
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {

        final ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            Log.v(TAG, "actionbar null");

            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    AppCompatActivity activity = this;
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            getDayData();
                            getPVOData();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.READ_LIVE:
                    JSONObject inputJson = null;
                    try {
                        inputJson = new JSONObject((String) msg.obj);           //extract JSON string from msg
                        getLiveData(inputJson);
                    } catch (Exception e) {}
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.READ_DAY:
                    JSONObject inputJson2 = null;
                    try {
                        inputJson2 = new JSONObject((String) msg.obj);           //extract JSON string from msg
                        logData(inputJson2);

                    } catch (JSONException e) {                                 // catch errors with JSON extraction
                        e.printStackTrace();
                    }
                    break;
                case Constants.READ_PVO:
                    JSONObject inputJson3 = null;
                    try {
                        inputJson3 = new JSONObject((String) msg.obj);           //extract JSON string from msg
                        mPvOutputsService.logPVOData(inputJson3);

                    } catch (JSONException e) {                                 // catch errors with JSON extraction
                        e.printStackTrace();
                    }
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBt();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(activity, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager manager = getSupportFragmentManager();
            Fragment currentFragment = manager.findFragmentById(R.id.content_main);
            if(currentFragment instanceof HomeFragment) super.onBackPressed();
            else manager.beginTransaction().replace(R.id.content_main, homeFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(activity, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.load_day_data: {
                getDayData();
                return true;
            }

            case R.id.load_PVO_data: {
                getPVOData();
                return true;
            }

            case R.id.reset_SD: {
                new AlertDialog.Builder(this)
                        .setTitle("Reset SD Card Data")
                        .setMessage("Do you really want to reset SD card")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                String outString = "4";
                                byte[] outArray = outString.getBytes(StandardCharsets.UTF_8);
                                mChatService.write(outArray);

                                inPowerSet = null;
                                outPowerSet = null;
                                consumedEnergySet = null;
                                generatedEnegySet = null;
                                capacitySet = null;

                                Toast.makeText(getApplicationContext(),"SD card deleted",Toast.LENGTH_SHORT);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_main, homeFragment).commit();
        } else if (id == R.id.nav_battery) {
            BatteryFragment batteryFragment = new BatteryFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_main, batteryFragment).commit();
        } else if (id == R.id.nav_settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_main, settingsFragment).commit();
        } else if (id == R.id.nav_info) {
            InfoFragment infoFragment = new InfoFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content_main, infoFragment).commit();
        } else if (id == R.id.nav_power) {
            PowerGraphFragment powerGraphFragment = new PowerGraphFragment();
            try {
                powerGraphFragment.populate(inPowerSet, outPowerSet, xLabels);
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main, powerGraphFragment).commit();
            } catch (Exception e) {}

        } else if (id == R.id.nav_energy) {
            EnergyGraphFragment energyGraphFragment = new EnergyGraphFragment();
            Log.v("energy graph", "selected");
            try {
                energyGraphFragment.populate(generatedEnegySet, consumedEnergySet, xLabels);
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main, energyGraphFragment).commit();
            } catch (Exception e) {
            }
        } else if (id == R.id.nav_capacity) {
            CapacityGraphFragment capacityGraphFragment = new CapacityGraphFragment();
            try {
                capacityGraphFragment.populate(capacitySet, xLabels);
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.content_main, capacityGraphFragment).commit();
            } catch (Exception e) {
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void logData(JSONObject inputJson) {

        try {
            if (inputJson.getInt("end") == 0) {
                float inPower = (float) inputJson.getDouble("inPower");
                inPowerVals.add(new Entry(inPower, count));

                float outPower = (float) inputJson.getDouble("outPower");
                outPowerVals.add(new Entry(outPower, count));

                float energyGenerated = (float) inputJson.getDouble("solarEnergy");
                generatedEnergyVals.add(new Entry(energyGenerated, count));

                float energyConsumed = (float) inputJson.getDouble("consumedEnergy");
                consumedEnergyVals.add(new Entry(energyConsumed, count));

                float capacity = (float) inputJson.getDouble("capacity");
                capacityVals.add(new Entry(capacity, count));

                count++;
                time = inputJson.getString("time");
                xLabels.add(formatTime(time));
            } else {
                inPowerSet = new LineDataSet(inPowerVals, "Power In");
                outPowerSet = new LineDataSet(outPowerVals, "Power Out");
                consumedEnergySet = new LineDataSet(consumedEnergyVals, "Consumed Energy");
                generatedEnegySet = new LineDataSet(generatedEnergyVals, "Generated Energy");
                capacitySet = new LineDataSet(capacityVals, "capacity");
                Toast.makeText(activity,String.valueOf(inPowerVals.size())+" Data points downloaded",Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(String time) {
        try {
            Date date = new Date (Long.parseLong(time)*1000);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String formattedDate = sdf.format(date);
            return formattedDate;
        } catch (Exception e) {
        }
        return "";
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PVOutputsService.PVOutputsBinder binder = (PVOutputsService.PVOutputsBinder) service;
            mPvOutputsService = binder.getService();
            mBound = true;
            mPvOutputsService.setBtChatService(mChatService);
            mPvOutputsService.setHandler(mPVOHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void getDayData(){
        inPowerVals = new ArrayList<>();
        outPowerVals = new ArrayList<>();
        generatedEnergyVals = new ArrayList<>();
        consumedEnergyVals = new ArrayList<>();
        capacityVals = new ArrayList<>();
        xLabels = new ArrayList<>();
        count = 0;

        String outString = "1";
        byte[] outArray = outString.getBytes(StandardCharsets.UTF_8);
        mChatService.write(outArray);
        Log.v("out string", outArray.toString());
    }

    private void getPVOData(){
        String outString2 = "2";
        byte[] outArray2 = outString2.getBytes(StandardCharsets.UTF_8);
        mChatService.write(outArray2);
    }

    private void getLiveData(JSONObject inputJson){

        try{
            String batteryVoltage = inputJson.getString("batVoltage");
            String inPower = inputJson.getString("inPower");
            String outPower = inputJson.getString("outPower");
            String batPower = inputJson.getString("batPower");
            String capacity = inputJson.getString("capacity");
            String solarEnergy = inputJson.getString("solarEnergy");
            String consumedEnergy = inputJson.getString("consumedEnergy");
            String ampHoursStored = inputJson.getString("ampHoursStored");
            homeFragment.update(batteryVoltage, inPower, outPower, batPower, capacity, solarEnergy, consumedEnergy, ampHoursStored);
        } catch (Exception e) {}

    }


}
