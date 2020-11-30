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
import java.util.function.Consumer;


public class ChangeFormatTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "ChangeFormatTask";

    private String setType;
    private Consumer<String> consumer ;

    public ChangeFormatTask(Consumer<String> consumer, String inType) {
        this.consumer = consumer;
        setType = inType;
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

                String strJsonGetCaptureMode = "{\"name\": \"camera.getOptions\", \"parameters\": { \"optionNames\":[\"captureMode\"] } }";
                strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonGetCaptureMode);
                try {
                    JSONObject output = new JSONObject(strResult);
                    JSONObject results = output.getJSONObject("results");
                    JSONObject options = results.getJSONObject("options");
                    String captureMode = options.getString("captureMode");
                    if ( captureMode.equals("image") ) {

                        if ( setType.equals("jpeg") || setType.equals("raw+") ) {

                            String strJsonSetCaptureMode = "{\"name\": \"camera.setOptions\", \"parameters\": { \"options\":{\"fileFormat\":{\"type\":\""+ setType + "\",\"width\":6720,\"height\":3360} } } }";
                            strResult = camera.httpExec(HttpConnector.HTTP_POST, HttpConnector.API_URL_CMD_EXEC, strJsonSetCaptureMode);
                            Log.d(TAG, "Set format=" + strResult);

                        } else {
                            //パラメーターエラー
                            setValue = "ParamERR";
                        }
                    } else {
                        setValue = "ParamERR";
                        setValue = "Mode ERR";
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    setValue = "COM ERR";
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
        consumer.accept(setValue);
    }

}
