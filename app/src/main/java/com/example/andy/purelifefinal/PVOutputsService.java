package com.example.andy.purelifefinal;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PVOutputsService extends Service {

    private ArrayList<Integer> unixTimes = new ArrayList<Integer>();
    private ArrayList<Float> energyGenValues = new ArrayList<Float>();
    private ArrayList<Float> powerGenValues = new ArrayList<Float>();
    private ArrayList<Float> energyConsValues = new ArrayList<Float>();
    private ArrayList<Float> powerConsValues = new ArrayList<Float>();
    private ArrayList<Float> volatageValues = new ArrayList<Float>();

    private Thread PVOThread;
    protected Boolean status;
    private BluetoothService mChatService;

    private DataUpload dataUpload;

    private int utcOffset;


    // Binder given to clients
    private final IBinder mBinder = new PVOutputsBinder();

    private Handler mHandler;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class PVOutputsBinder extends Binder {
        PVOutputsService getService() {
            // Return this instance of LocalService so clients can call public methods
            status = false;
            return PVOutputsService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setBtChatService(BluetoothService mChatService){
        this.mChatService=mChatService;
    }

    public void setHandler(Handler mHandler) { this.mHandler = mHandler;}

    public void logPVOData(JSONObject inputJson) {
        try {
            if (inputJson.getInt("end") == 0) {

                int unixTime = Integer.valueOf(inputJson.getString("time"));
                float energyGen = Float.valueOf(inputJson.getString("solarEnergy"));
                float powerGen = Float.valueOf(inputJson.getString("inPower"));
                float energyCons = Float.valueOf(inputJson.getString("consumedEnergy"));
                float powerCons = Float.valueOf(inputJson.getString("outPower"));
                float volts = Float.valueOf(inputJson.getString("batVoltage"));

                /*
                Log.v("logPVOdata",String.valueOf(unixTime+25200));
                Log.v("logPVOdata",String.valueOf(energyGen));
                Log.v("logPVOdata",String.valueOf(powerGen));
                Log.v("logPVOdata",String.valueOf(energyCons));
                Log.v("logPVOdata",String.valueOf(powerCons));
                Log.v("logPVOdata",String.valueOf(volts));
                */

                unixTimes.add(unixTime);
                energyGenValues.add(energyGen);
                powerGenValues.add(powerGen);
                energyConsValues.add(energyCons);
                powerConsValues.add(powerCons);
                volatageValues.add(volts);

            } else {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String savedSytemIdDefault = getResources().getString(R.string.saved_system_id_default);
                String savedSytemId = sharedPref.getString(getString(R.string.saved_system_id),savedSytemIdDefault);
                String savedAPIKeyDefault = getResources().getString(R.string.saved_api_key_default);
                String savedAPIKey = sharedPref.getString(getString(R.string.saved_api_key),savedAPIKeyDefault);

                dataUpload = new DataUpload(savedAPIKey,savedSytemId);

                PVOThread mPVOThread = new PVOThread(unixTimes, energyGenValues, powerGenValues, energyConsValues, powerConsValues, volatageValues);
                mPVOThread.start();
                unixTimes = new ArrayList<Integer>();
                energyGenValues = new ArrayList<Float>();
                powerGenValues = new ArrayList<Float>();
                energyConsValues = new ArrayList<Float>();
                powerConsValues = new ArrayList<Float>();
                volatageValues = new ArrayList<Float>();
            }

        } catch (JSONException e) {}
    }

    private final class PVOThread extends Thread {
        protected ArrayList<Integer> mUnixTimes = new ArrayList<Integer>();
        protected ArrayList<Float> mEnergyGenValues = new ArrayList<Float>();
        protected ArrayList<Float> mPowerGenValues = new ArrayList<Float>();
        protected ArrayList<Float> mEnergyConsValues = new ArrayList<Float>();
        protected ArrayList<Float> mPowerConsValues = new ArrayList<Float>();
        protected ArrayList<Float> mVolatageValues = new ArrayList<Float>();



        public PVOThread(ArrayList<Integer> unixTimes,ArrayList<Float> energyGenValues,ArrayList<Float> powerGenValues,ArrayList<Float> energyConsValues,ArrayList<Float> powerConsValues,ArrayList<Float> volatageValues){
            mUnixTimes = unixTimes;
            mEnergyGenValues = energyGenValues;
            mPowerGenValues = powerGenValues;
            mEnergyConsValues = energyConsValues;
            mPowerConsValues = powerConsValues;
            mVolatageValues = volatageValues;
    }

        @Override
        public void run()
        {
            try {
                //Log.v("upload string", dataUpload.dataToBatchStatusString(mUnixTimes, mEnergyGenValues, mPowerGenValues, mEnergyConsValues, mPowerConsValues, mVolatageValues).remove());
                status = dataUpload.batchStatusUpload(mUnixTimes, mEnergyGenValues, mPowerGenValues, mEnergyConsValues, mPowerConsValues, mVolatageValues);
            }catch(Exception e){}
            Log.v("upload status in PVO", String.valueOf(status));
            if(status) {
                String outString3 = "3";
                byte[] outArray3 = outString3.getBytes(StandardCharsets.UTF_8);
                mChatService.write(outArray3);
                mHandler.obtainMessage(-1, -1, -1, mUnixTimes.size()).sendToTarget();
            }
            else{

            }

        }
    }
}