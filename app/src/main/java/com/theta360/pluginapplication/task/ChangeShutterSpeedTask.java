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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChangeShutterSpeedTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ChgSS";

    public static final String MINUS = "-";
    public static final String PLUS  = "+";

    private String setSS;

    private Consumer<String> consumer ;
    private Map<String, String>  ssApi2Cmd;
    public Map<String, String>  ssCmd2Api;

    public ChangeShutterSpeedTask(Consumer<String> consumer, String inputSS) {
        this.consumer = consumer;

        ssCmd2Api = new HashMap<String, String>();
        ssCmd2Api.put("", "");
        ssCmd2Api.put("+", "+");
        ssCmd2Api.put("-", "-");
        ssCmd2Api.put("25000", "0.00004");
        ssCmd2Api.put("20000", "0.00005");
        ssCmd2Api.put("16000", "0.0000625");
        ssCmd2Api.put("12500", "0.00008");
        ssCmd2Api.put("10000", "0.0001");
        ssCmd2Api.put("8000",  "0.000125");
        ssCmd2Api.put("6400",  "0.00015625");
        ssCmd2Api.put("5000",  "0.0002");
        ssCmd2Api.put("4000",  "0.00025");
        ssCmd2Api.put("3200",  "0.0003125");
        ssCmd2Api.put("2500",  "0.0004");
        ssCmd2Api.put("2000",  "0.0005");
        ssCmd2Api.put("1600",  "0.000625");
        ssCmd2Api.put("1250",  "0.0008");
        ssCmd2Api.put("1000",  "0.001");
        ssCmd2Api.put("800",   "0.00125");
        ssCmd2Api.put("640",   "0.0015625");
        ssCmd2Api.put("500",   "0.002");
        ssCmd2Api.put("400",   "0.0025");
        ssCmd2Api.put("320",   "0.003125");
        ssCmd2Api.put("250",   "0.004");
        ssCmd2Api.put("200",   "0.005");
        ssCmd2Api.put("160",   "0.00625");
        ssCmd2Api.put("125",   "0.008");
        ssCmd2Api.put("100",   "0.01");
        ssCmd2Api.put("80",    "0.0125");
        ssCmd2Api.put("60",    "0.01666666");
        ssCmd2Api.put("50",    "0.02");
        ssCmd2Api.put("40",    "0.025");
        ssCmd2Api.put("30",    "0.03333333");
        ssCmd2Api.put("25",    "0.04");
        ssCmd2Api.put("20",    "0.05");
        ssCmd2Api.put("15",    "0.06666666");
        ssCmd2Api.put("13",    "0.07692307");
        ssCmd2Api.put("10",    "0.1");
        ssCmd2Api.put("8",     "0.125");
        ssCmd2Api.put("6",     "0.16666666");
        ssCmd2Api.put("5",     "0.2");
        ssCmd2Api.put("4",     "0.25");
        ssCmd2Api.put("3",     "0.33333333");
        ssCmd2Api.put("2.5",   "0.4");
        ssCmd2Api.put("2",     "0.5");
        ssCmd2Api.put("1.6",   "0.625");
        ssCmd2Api.put("1.3",   "0.76923076");
        ssCmd2Api.put("1\"",   "1");
        ssCmd2Api.put("1.3\"", "1.3");
        ssCmd2Api.put("1.6\"", "1.6");
        ssCmd2Api.put("2\"",   "2");
        ssCmd2Api.put("2.5\"", "2.5");
        ssCmd2Api.put("3.2\"", "3.2");
        ssCmd2Api.put("4\"",   "4");
        ssCmd2Api.put("5\"",   "5");
        ssCmd2Api.put("6\"",   "6");
        ssCmd2Api.put("8\"",   "8");
        ssCmd2Api.put("10\"",  "10");
        ssCmd2Api.put("13\"",  "13");
        ssCmd2Api.put("15\"",  "15");
        ssCmd2Api.put("20\"",  "20");
        ssCmd2Api.put("25\"",  "25");
        ssCmd2Api.put("30\"",  "30");
        ssCmd2Api.put("60\"",  "60");

        List<String> listSsKey = new ArrayList<>( ssCmd2Api.keySet() );
        ssApi2Cmd = new HashMap<String, String>();
        for (String s: listSsKey) {
            ssApi2Cmd.put( ssCmd2Api.get(s), s);
        }
        ssApi2Cmd.put( "0", "auto");


        setSS = inputSS;
        //今回は、このタスクの引数を与える時点で、
        //呼び出し元が入力文字列をwebAPIで定義された文字列に読み替えていることを前提とする。
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    synchronized protected String doInBackground(Void... params) {
        String setValue="";

        HttpConnector camera = new HttpConnector("127.0.0.1:8080");
        String strResult = "";

        strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_STAT, "");
        
        try {
            JSONObject output2 = new JSONObject(strResult);
            JSONObject state = output2.getJSONObject("state");
            String captureStatus = state.getString("_captureStatus");
            int recordedTime = state.getInt("_recordedTime");

            if ( captureStatus.equals("idle") && (recordedTime==0) ) {

                String strJsonGetCaptureMode = "{\"name\": \"camera.getOptions\", \"parameters\": { \"optionNames\": [\"shutterSpeed\", \"shutterSpeedSupport\"] } }";
                strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonGetCaptureMode);
                Log.d(TAG, "debug result=" + strResult);

                JSONObject output = new JSONObject(strResult);
                JSONObject results = output.getJSONObject("results");
                JSONObject options = results.getJSONObject("options");
                String curShutterSpeed = options.getString("shutterSpeed");
                JSONArray shutterSpeedSupport = options.getJSONArray("shutterSpeedSupport");

                if ( (setSS.equals(MINUS)) || (setSS.equals(PLUS)) ) {
                    int curPos=-1;
                    for (int i=0; i<shutterSpeedSupport.length(); i++) {
                        if ( curShutterSpeed.equals( shutterSpeedSupport.getString(i) ) ) {
                            curPos = i;
                            break;
                        }
                    }

                    int nextPos=0;
                    if ( setSS.equals(PLUS) ) {
                        nextPos = curPos+1;
                        if ( nextPos >= shutterSpeedSupport.length()) {
                            nextPos = shutterSpeedSupport.length() - 1;
                        }
                    } else {
                        nextPos = curPos-1;
                        if ( nextPos < 0) {
                            nextPos = 0;
                        }
                    }

                    String newParam = shutterSpeedSupport.getString(nextPos);
                    setValue = newParam;
                    String strJsonSetSS = "{\"name\": \"camera.setOptions\", \"parameters\": { \"options\":{\"shutterSpeed\":\"" + setValue +"\"} } }";
                    Log.d(TAG, "strJsonSetSS=" + strJsonSetSS);
                    strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonSetSS);

                } else {
                    boolean mutchFlag = false;
                    Log.d(TAG, "setSS=" + setSS);
                    if (setSS.equals("")) {
                        Log.d(TAG, "curShutterSpeed=" + curShutterSpeed);
                        setSS=curShutterSpeed;
                    }
                    double doubleSS = Double.parseDouble(setSS);
                    for (int i=0; i<shutterSpeedSupport.length(); i++) {
                        if ( doubleSS == shutterSpeedSupport.getDouble(i) ) {
                            mutchFlag = true;
                            break;
                        }
                    }
                    if (mutchFlag){
                        //送信
                        setValue = setSS;
                        String strJsonSetSS = "{\"name\": \"camera.setOptions\", \"parameters\": { \"options\":{\"shutterSpeed\":\"" + setValue +"\"} } }";
                        strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonSetSS);
                    } else {
                        //パラメーターエラー
                        setValue = "ParamERR";
                    }
                }
            } else {
                //実行不可(BUSY)
                setValue = "BUSY";
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
            //通信エラー
            setValue = "COM ERR";
        }

        return setValue;
    }

    @Override
    protected void onPostExecute(String setValue) {
        String setResult = "";
        try {
            double chkNum = Double.parseDouble(setValue);

            List<String> listSsKey = new ArrayList<>( ssApi2Cmd.keySet() );
            for ( String s : listSsKey) {
                double listNum;
                try {
                    listNum = Double.parseDouble(s);
                    if ( chkNum == listNum ) {
                        setResult = ssApi2Cmd.get(s);
                        break;
                    }
                } catch (NumberFormatException e) {
                    // 無処理
                }
            }
        } catch (NumberFormatException e) {
            setResult = setValue;
        }
        Log.d(TAG, "result SS=" + setResult);
        consumer.accept(setResult);
    }

}
