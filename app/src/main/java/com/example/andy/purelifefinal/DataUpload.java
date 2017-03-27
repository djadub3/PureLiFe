package com.example.andy.purelifefinal;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.Thread;
import java.lang.InterruptedException;

public class DataUpload {

    private static String apiKey;
    private static String systemID;
    private long rateLimitResetTime;

    public DataUpload(String apiKey, String systemID) {
        this.apiKey = apiKey;
        this.systemID = systemID;
        this.rateLimitResetTime = 0;
    }

    // call upload method until no more data or rate limit is reached
    public boolean batchStatusUpload(ArrayList<Integer> unixTimes,
                                     ArrayList<Float> energyGens, ArrayList<Float> powerGens, ArrayList<Float> energyCons,
                                     ArrayList<Float> powerCons, ArrayList<Float> volts) {
        ArrayDeque<String> dataQueue = dataToBatchStatusString(unixTimes, energyGens,
                powerGens, energyCons, powerCons, volts);
        ArrayList<String> responseMessages = new ArrayList<String>();
        long[] rateLimits;
        String[] responseSplit;
        String data, currentTime;
        int startSize = dataQueue.size();
        boolean wait = startSize > 1;
        boolean success = true;

        do {
            // Wait 10 seconds between upload requests per PVOutput.org API
            if (wait && dataQueue.size() < startSize) pvWait();
            data = dataQueue.remove();
            ArrayList<String>[] headers = upload(data);
            responseMessages.add(headers[2].get(0));
            responseSplit = headers[2].get(0).split("\t");
            success = responseSplit[1].equals("200") && success;
            rateLimits = getRateLimits(headers);
            this.rateLimitResetTime = rateLimits[2]*1000;
			/*if (success) {
				System.out.println(responseSplit[0] + " " + responseSplit[1] + " " +
				responseSplit[2]);
			} else {
				System.out.println(responseSplit[0] + " " + responseSplit[3]);
			}*/
        } while (!dataQueue.isEmpty() && !responseSplit[1].equals("403"));
        return success;
    }

    // perform one batch status upload and return headers response from PVOutput.org
    public ArrayList<String>[] upload(String data) {
        // Open batch status upload connection to PVOutput.org
        ArrayList<String>[] headers = null;
        try {
            URL url = new URL("http://pvoutput.org/service/r2/addbatchstatus.jsp");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            // Set HTTP method to POST, input API key and System ID and request rate
            // limit from PVOutput.org
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("X-Rate-Limit","1");
            urlConnection.setRequestProperty("X-Pvoutput-Apikey",this.apiKey);
            urlConnection.setRequestProperty("X-Pvoutput-SystemId",this.systemID);

            try {
                // Set settings for upload
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                // Upload data
                Writer out = new BufferedWriter(new OutputStreamWriter(
                        urlConnection.getOutputStream()));
                out.write(data);
                out.close();
                // get headers for rate limit check and rate limit reset time
                headers = getHeaders(urlConnection);
                // check response message for successful upload or error.
                String responseCode = Integer.toString(urlConnection.getResponseCode());
                String responseMessage = urlConnection.getResponseMessage();
                String messageBody = "";
                if (responseCode.equals("200")) {
                    messageBody = readStream(urlConnection.getInputStream());
                } else {
                    messageBody = readStream(urlConnection.getErrorStream());
                }
                responseMessage = getDateTime(new Date()) + "\t" + responseCode + "\t" +
                        responseMessage + "\t" + messageBody;
                headers[2] = new ArrayList<String>();
                headers[2].add(responseMessage);
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return headers;
    }

    // get headers from PVOutput.org after uploading data
    public ArrayList<String>[] getHeaders(HttpURLConnection urlConnection) {
        int i = 1;
        ArrayList<String> headerFields = new ArrayList<String>();
        ArrayList<String> headerKeys = new ArrayList<String>();
        String headerKey = "";
        while ((headerKey = urlConnection.getHeaderFieldKey(i)) != null) {
            headerKeys.add(headerKey);
            headerFields.add(urlConnection.getHeaderField(i));
            i++;
        }
        ArrayList<String>[] headers = new ArrayList[3];
        headers[0] = headerKeys;
        headers[1] = headerFields;
        return headers;
    }
    // get upload limit from headers returned from PVOutput.org
    public long[] getRateLimits(ArrayList<String>[] headers) {
        long[] rateLimits = new long[3];
        int check = 0;
        String headerKey, headerField;
        for (int i = 0; i < headers[0].size() && check < 3; i++) {
            headerKey = headers[0].get(i);
            headerField = headers[1].get(i);
            if (headerKey == "X-Rate-Limit-Remaining") {
                rateLimits[0] = Long.valueOf(headerField);
                check++;
            }
            if (headerKey == "X-Rate-Limit-Limit") {
                rateLimits[1] = Long.valueOf(headerField);
                check++;
            }
            if (headerKey == "X-Rate-Limit-Reset") {
                rateLimits[2] = Long.valueOf(headerField);
                check++;
            }
        }
        return rateLimits;
    }

    // take data ArrayLists of values from Arduino and convert to String ArrayDeque
    // energy used, power used, voltage
    public ArrayDeque<String> dataToBatchStatusString(ArrayList<Integer> unixTimes,
                                                      ArrayList<Float> energyGens, ArrayList<Float> powerGens, ArrayList<Float> energyCons,
                                                      ArrayList<Float> powerCons, ArrayList<Float> volts)
    {
        ArrayDeque<String> dataQueue = new ArrayDeque<String>();
        int batchSize = 1;
        String batchStatus = "";
        String dateTime, eGen, pGen, eCon, pCon, volt, status;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd,HH:mm");
        for (int i = 0; i < unixTimes.size(); i++) {
            dateTime = sdf.format(new Date((long)unixTimes.get(i)*1000));
            eGen = String.valueOf(Math.round(energyGens.get(i)));
            pGen = String.valueOf(Math.round(powerGens.get(i)));
            eCon = String.valueOf(Math.round(energyCons.get(i)));
            pCon = String.valueOf(Math.round(powerCons.get(i)));
            volt = String.valueOf(volts.get(i));
            status = dateTime + "," + eGen + "," + pGen + "," + eCon + "," + pCon
                    + ",-1," + volt;
            if (batchSize == 1) {
                batchStatus = "data=" + status;
                if (unixTimes.size() == 1) {
                    dataQueue.add(batchStatus);
                    return dataQueue;
                } else {
                    batchStatus = batchStatus + ";";
                }
                batchSize++;
            } else if (batchSize < 100 && i < unixTimes.size()-1) {
                batchStatus = batchStatus + status + ";";
                batchSize++;
            } else {
                batchStatus = batchStatus + status;
                dataQueue.add(batchStatus);
                batchSize = 1;
            }
        }
        return dataQueue;
    }

    public String getDateTime(Date date) {
        return DateFormat.getDateTimeInstance().format(date);
    }

    public String readStream(InputStream in) {
        if (in == null) return "";
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public void pvWait() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


