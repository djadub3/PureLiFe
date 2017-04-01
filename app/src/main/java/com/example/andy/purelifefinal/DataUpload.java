package com.example.andy.purelifefinal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class DataUpload {

    private static String apiKey;
    private static String systemID;


    public DataUpload(String apiKey, String systemID) {
        this.apiKey = apiKey;
        this.systemID = systemID;
    }

    // call upload method until no more data or rate limit is reached
    public boolean batchStatusUpload(ArrayList<Integer> unixTimes,
                                     ArrayList<Float> energyGens, ArrayList<Float> powerGens, ArrayList<Float> energyCons,
                                     ArrayList<Float> powerCons, ArrayList<Float> volts) {
        ArrayDeque<String> dataQueue = dataToBatchStatusString(unixTimes, energyGens,
                powerGens, energyCons, powerCons, volts);
        String[] responseSplit;
        String data, responseMessage;
        data = responseMessage = "";
        int startSize = dataQueue.size();
        boolean wait = startSize > 1;
        boolean success = true;
        try {
            do {
                // Wait 10 seconds between upload requests per PVOutput.org API
                if (wait && dataQueue.size() < startSize) pvWait();
                data = dataQueue.remove();
                responseMessage = upload(data);
                responseSplit = responseMessage.split("\t");
                success = responseSplit[1].equals("200") && success;
				/*if (success) {
					System.out.println(responseSplit[0] + " " + responseSplit[1] + " " +
					responseSplit[2]);
				} else {
					System.out.println(responseSplit[0] + " " + responseSplit[3]);
				}*/
            } while (!dataQueue.isEmpty() && !responseSplit[1].equals("403"));
        } catch (Exception e) {
            return false;
        }
        return success;
    }

    // perform one batch status upload and return headers response from PVOutput.org
    private String upload(String data) throws Exception {
        // Open batch status upload connection to PVOutput.org
        URL url = new URL("http://pvoutput.org/service/r2/addbatchstatus.jsp");
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        // Set HTTP method to POST, input API key and System ID and request rate
        // limit from PVOutput.org
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("X-Pvoutput-Apikey",this.apiKey);
        urlConnection.setRequestProperty("X-Pvoutput-SystemId",this.systemID);
        // Set settings for upload
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        // Upload data
        Writer out = new BufferedWriter(new OutputStreamWriter(
                urlConnection.getOutputStream()));
        out.write(data);
        out.close();
        // check response message for successful upload or error.
        String responseCode = Integer.toString(urlConnection.getResponseCode());
        String responseMessage = urlConnection.getResponseMessage();
        String messageBody = "";
		/*if (responseCode.equals("200")) {
			messageBody = readStream(urlConnection.getInputStream());
		} else {
			messageBody = readStream(urlConnection.getErrorStream());
		}*/
        StringBuilder sb = new StringBuilder();
        sb.append(getDateTime(new Date()));
        sb.append("\t");
        sb.append(responseCode);
        sb.append("\t");
		/*sb.append(responseMessage);
		sb.append("\t");
		sb.append(messageBody);*/
        urlConnection.disconnect();
        return sb.toString();
    }

    // take data ArrayLists of values from Arduino and convert to String ArrayDeque
    public ArrayDeque<String> dataToBatchStatusString(ArrayList<Integer> unixTimes,
                                                      ArrayList<Float> energyGens, ArrayList<Float> powerGens, ArrayList<Float> energyCons,
                                                      ArrayList<Float> powerCons, ArrayList<Float> volts)
    {
        ArrayDeque<String> dataQueue = new ArrayDeque<String>();
        int batchSize = 1;
        String dateTime, eGen, pGen, eCon, pCon, volt;
        dateTime = eGen = pGen = eCon = pCon = volt = "";
        StringBuilder status = new StringBuilder();
        StringBuilder batchStatus = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < unixTimes.size(); i++) {
            dateTime = sdf.format(new Date((long)unixTimes.get(i)*1000));
            eGen = String.valueOf(Math.round(energyGens.get(i)));
            pGen = String.valueOf(Math.round(powerGens.get(i)));
            eCon = String.valueOf(Math.round(energyCons.get(i)));
            pCon = String.valueOf(Math.round(powerCons.get(i)));
            volt = String.valueOf(volts.get(i));
            status.setLength(0);
            status.append(dateTime);
            status.append(",");
            status.append(eGen);
            status.append(",");
            status.append(pGen);
            status.append(",");
            status.append(eCon);
            status.append(",");
            status.append(pCon);
            status.append(",,");
            status.append(volt);
            if (batchSize == 1) {
                batchStatus.setLength(0);
                batchStatus.append("data=");
                batchStatus.append(status.toString());
                if (unixTimes.size() == 1) {
                    dataQueue.add(batchStatus.toString());
                    return dataQueue;
                }
                batchStatus.append(";");
                batchSize++;
            } else if (batchSize < 100 && i < unixTimes.size()-1) {
                batchStatus.append(status);
                batchStatus.append(";");
                batchSize++;
            } else {
                batchStatus.append(status);
                dataQueue.add(batchStatus.toString());
                batchSize = 1;
            }
        }
        return dataQueue;
    }

    public String getDateTime(Date date) {
        DateFormat dt = DateFormat.getDateTimeInstance();
        dt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dt.format(date);
    }

    private String readStream(InputStream in) throws Exception {
        if (in == null) return "";
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        br = new BufferedReader(new InputStreamReader(in));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        if (br != null) {
            br.close();
        }
        return sb.toString();
    }

    private void pvWait() throws Exception {
        Thread.sleep(10000);
    }
}



