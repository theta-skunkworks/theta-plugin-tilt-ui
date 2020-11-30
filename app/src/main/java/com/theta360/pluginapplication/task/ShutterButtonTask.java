/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.pluginapplication.task;

import android.os.AsyncTask;
import android.util.Log;

import com.theta360.pluginapplication.network.HttpConnector;

import org.json.JSONException;
import org.json.JSONObject;

public class ShutterButtonTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ShutterBtn";

    public final static int M_SHOOT_SINGLE = 0;
    public final static int M_SHOOT_TIMESHIFT = 1;
    public final static int M_SHOOT_INTERVAL = 2;
    public final static int M_SHOOT_INT_COMP = 3;
    public int multipleShooting;

    private boolean timeShift;

    public ShutterButtonTask(int inMultipleShooting) {
        multipleShooting = inMultipleShooting;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    synchronized protected String doInBackground(Void... params) {
        HttpConnector camera = new HttpConnector("127.0.0.1:8080");
        String strResult = "";

        strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_STAT, "");
        try {
            JSONObject output2 = new JSONObject(strResult);
            JSONObject state = output2.getJSONObject("state");
            String captureStatus = state.getString("_captureStatus");
            int recordedTime = state.getInt("_recordedTime");

            String strJsonGetCaptureMode = "{\"name\": \"camera.getOptions\", \"parameters\": { \"optionNames\":[\"captureMode\"] } }";
            strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonGetCaptureMode);

            JSONObject output = new JSONObject(strResult);
            JSONObject results = output.getJSONObject("results");
            JSONObject options = results.getJSONObject("options");
            String captureMode = options.getString("captureMode");

            if ( captureMode.equals(ChangeCaptureModeTask.CAPMODE_IMAGE) ) {

                if (captureStatus.equals("idle")) {

                    String strJsonTakePicture;
                    if ( multipleShooting==M_SHOOT_SINGLE ) {
                        strJsonTakePicture = "{\"name\": \"camera.takePicture\"}";
                    } else {
                        String mode;
                        switch (multipleShooting){
                            case M_SHOOT_TIMESHIFT :
                                mode = "timeShift";
                                break;
                            case M_SHOOT_INTERVAL :
                                String strJsonIntervalParam  = "{\"name\": \"camera.setOptions\", \"parameters\": { \"options\":{\"captureInterval\":\"6\", \"captureNumber\":\"0\"} } }";
                                strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonIntervalParam);
                                Log.d(TAG,"Interval param result:" + strResult);

                                mode = "interval";
                                break;
                            case M_SHOOT_INT_COMP :
                                String strJsonCompositeParam  = "{\"name\": \"camera.setOptions\", \"parameters\": { \"options\":{\"_compositeShootingOutputInterval\":\"0\", \"_compositeShootingTime\":\"86400\"} } }";
                                strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonCompositeParam);
                                Log.d(TAG,"Int Comp param result:" + strResult);

                                mode = "composite";
                                break;
                            default:
                                mode = "bracket";
                                break;
                        }
                        strJsonTakePicture = "{\"name\": \"camera.startCapture\", \"parameters\": {\"_mode\":\"" + mode + "\"} }";
                    }
                    strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonTakePicture);
                    Log.d(TAG, "ShutterButtonTask: Exec =" + strJsonTakePicture);

                } else {
                    if ( !(multipleShooting==M_SHOOT_SINGLE) ) {
                        String strJsonTakePicture = "{\"name\": \"camera.stopCapture\" }";
                        strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonTakePicture);
                        Log.d(TAG, "ShutterButtonTask: Exec =" + strJsonTakePicture);
                    }
                }

            } else {

                String strJsonStartStop;
                if ( recordedTime == 0 ) {
                    strJsonStartStop = "{\"name\": \"camera.startCapture\" }";
                } else {
                    strJsonStartStop = "{\"name\": \"camera.stopCapture\" }";
                }

                strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonStartStop);
                Log.d(TAG, "ShutterButtonTask: Exec =" + strJsonStartStop);

            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return strResult;
    }

    @Override
    protected void onPostExecute(String result) {

    }

}
